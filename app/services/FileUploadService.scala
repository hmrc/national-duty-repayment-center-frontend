/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import connectors.{UpscanInitiateRequest, UpscanInitiateResponse}
import models.FileType.SupportingEvidence
import models.FileUpload.{Accepted, Initiated}
import models.requests.UploadRequest
import models.{DuplicateFileUpload, FileTransmissionFailed, FileType, FileUpload, FileUploadError, FileUploads, FileVerificationFailed, S3UploadError, UpscanFileFailed, UpscanFileReady, UpscanNotification}
import play.api.libs.Files.logger
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}


trait IsTransient

trait FileUploadState {
  def fileUploads: FileUploads
}

case class UploadFile(
                       reference: String,
                       uploadRequest: UploadRequest,
                       fileUploads: FileUploads,
                       maybeUploadError: Option[FileUploadError] = None
                     ) extends FileUploadState

object UploadFile {
  implicit val formatter: Format[UploadFile] = Json.format[UploadFile]
}

case class FileUploaded(
                         fileUploads: FileUploads,
                         acknowledged: Boolean = false
                       ) extends FileUploadState

object FileUploaded {
  implicit val formatter: Format[FileUploaded] = Json.format[FileUploaded]
}


trait FileUploadService {

  type UpscanInitiateApi = UpscanInitiateRequest => Future[UpscanInitiateResponse]

  final def fileUploadOrUploaded(
                                  upscanRequest: UpscanInitiateRequest,
                                  upscanInitiate: UpscanInitiateApi,
                                  fileUploadsOpt: Option[FileUploads],
                                  showUploadSummaryIfAny: Boolean,
                                  fileType: Option[FileType]
                                )(implicit ec: ExecutionContext): Future[FileUploadState] = {
    val fileUploads = fileUploadsOpt.getOrElse(FileUploads())
    if ((showUploadSummaryIfAny && fileUploads.nonEmpty))
      Future.successful(FileUploaded(fileUploads))
    else
      for {
        upscanResponse <- upscanInitiate(upscanRequest)
      } yield UploadFile(
        upscanResponse.reference,
        upscanResponse.uploadRequest,
        fileUploads.copy(files =
          fileUploads.files :+ FileUpload.Initiated(fileUploads.files.size + 1, upscanResponse.reference, fileType)
        )
      )
  }

  private def resetFileUploadStatusToInitiated(reference: String, fileUploads: FileUploads): FileUploads =
    fileUploads.copy(files = fileUploads.files.map {
      case f if f.reference == reference =>
        FileUpload.Initiated(f.orderNumber, f.reference, f.fileType)
      case other => other
    })

  def initiateFileUpload(
                         upscanRequest: UpscanInitiateRequest, fileType: Option[FileType]
                        )(upscanInitiate: UpscanInitiateApi)(state: Option[FileUploadState])(implicit ec: ExecutionContext): Future[FileUploadState] =

    state match {

      case Some(current@UploadFile(reference, _, fileUploads, maybeUploadError)) => {
        for {
          upscanResponse <- upscanInitiate(upscanRequest)
        } yield {
          val files = filesNotInStateInitiated(fileUploads.files)
          val refreshedCurrent = current.copy(upscanResponse.reference, upscanResponse.uploadRequest, fileUploads.copy(files =
            files :+ FileUpload.Initiated(fileUploads.files.size + 1, upscanResponse.reference, fileType)
          ))
          if (maybeUploadError.isDefined)
            current.copy(fileUploads = resetFileUploadStatusToInitiated(reference, fileUploads))
          else refreshedCurrent
        }
      }

      case Some(current@FileUploaded(fileUploads, _)) =>
        fileUploadOrUploaded(
          upscanRequest,
          upscanInitiate,
          Some(fileUploads),
          showUploadSummaryIfAny = false,
          fileType
        )
      case _ =>
        fileUploadOrUploaded(
          upscanRequest,
          upscanInitiate,
          state.map(_.fileUploads),
          showUploadSummaryIfAny = true,
          fileType)
    }

  final def upscanCallbackArrived(notification: UpscanNotification, fileType: FileType)(state: FileUploadState) = {

    def shouldReplaceExistingFile(fileUploads: FileUploads, fileType: FileType): Boolean = {
      val singleUploadFiles = fileUploads.files.filterNot(f => f.fileType.contains(SupportingEvidence))
      val acceptedSingleUploadFiles = filesInStateAccepted(singleUploadFiles)
      acceptedSingleUploadFiles.exists(_.fileType.contains(fileType))
    }

    def updateFileUploads(fileUploads: FileUploads) =
      fileUploads.copy(files = fileUploads.files.map {
        // update status of the file with matching upscan reference
        case FileUpload(orderNumber, ref) if ref == notification.reference =>
          notification match {
            case UpscanFileReady(_, url, uploadDetails) =>
              //check for existing file uploads with duplicated checksum
              val modifiedFileUpload: FileUpload = fileUploads.files
                .find(file =>
                  file.checksumOpt.contains(uploadDetails.checksum) && file.reference != notification.reference && file.fileType.contains(fileType)
                )
              match {
                case Some(existingFileUpload: FileUpload.Accepted) =>
                  FileUpload.Duplicate(
                    orderNumber,
                    ref,
                    uploadDetails.checksum,
                    existingFileName = existingFileUpload.fileName,
                    duplicateFileName = uploadDetails.fileName,
                    fileType = Some(fileType)
                  )
                case s@_ =>
                  FileUpload.Accepted(
                    orderNumber,
                    ref,
                    url,
                    uploadDetails.uploadTimestamp,
                    uploadDetails.checksum,
                    uploadDetails.fileName,
                    uploadDetails.fileMimeType,
                    Some(fileType)
                  )
              }
              modifiedFileUpload

            case UpscanFileFailed(_, failureDetails) =>

              FileUpload.Failed(
                orderNumber,
                ref,
                failureDetails
              )
          }
        case u => u
      })

    state match {
      case state: FileUploaded =>
        Future.successful(state.copy(acknowledged = true))

      case current@UploadFile(
      reference,
      uploadRequest,
      fileUploads,
      errorOpt
      ) =>
        val uploadsUpdated = if (shouldReplaceExistingFile(fileUploads, fileType)) {
          val existingSingleUploadFile = fileUploads.files.find(f => f.fileType.contains(fileType) && f.isInstanceOf[FileUpload.Accepted])
          val updatedFiles = fileUploads.files.filterNot(f => existingSingleUploadFile.map(_.reference).contains(f.reference))
          fileUploads.copy(files = updatedFiles)
        } else fileUploads

        val updatedFileUploads = updateFileUploads(uploadsUpdated)
        val currentUpload = updatedFileUploads.files.find(_.reference == reference)
        commonFileUploadStatusHandler(
          updatedFileUploads,
          reference,
          uploadRequest,
          current.copy(fileUploads = updatedFileUploads)
        )
          .apply(currentUpload)
    }
  }

  final def removeFileUploadBy(reference: String)(
    upscanRequest: UpscanInitiateRequest
  )(upscanInitiate: UpscanInitiateApi)(state: FileUploadState)(implicit ec: ExecutionContext) =
    state match {
      case current: FileUploaded =>
        val updatedFileUploads = current.fileUploads
          .copy(files = current.fileUploads.files.filterNot(_.reference == reference))
        val updatedCurrentState = current.copy(fileUploads = updatedFileUploads)
        if (updatedFileUploads.isEmpty)
          initiateFileUpload(upscanRequest, None)(upscanInitiate)(Some(updatedCurrentState))
        else
          Future.successful(updatedCurrentState)
    }

  /** Common transition helper based on the file upload status. */
  private def commonFileUploadStatusHandler(
                                             fileUploads: FileUploads,
                                             reference: String,
                                             uploadRequest: UploadRequest,
                                             fallbackState: => FileUploadState
                                           ): PartialFunction[Option[FileUpload], Future[FileUploadState]] = {
    case None => Future.successful(fallbackState)

    case Some(initiatedFile: FileUpload.Initiated) =>
      Future.successful(UploadFile(reference, uploadRequest, fileUploads))


    case Some(acceptedFile: FileUpload.Accepted) => {
      logger.info("Updated file status to FileUploaded")
      Future.successful(FileUploaded(fileUploads))
    }

    case Some(failedFile: FileUpload.Failed) =>

      Future.successful(UploadFile(
        reference,
        uploadRequest,
        fileUploads,
        Some(FileVerificationFailed(failedFile.details))
      ))


    case Some(rejectedFile: FileUpload.Rejected) =>
      Future.successful(UploadFile(
        reference,
        uploadRequest,
        fileUploads,
        Some(FileTransmissionFailed(rejectedFile.details))
      ))


    case Some(duplicatedFile: FileUpload.Duplicate) =>

      Future.successful(UploadFile(
        reference,
        uploadRequest,
        fileUploads,
        Some(
          DuplicateFileUpload(
            duplicatedFile.checksum,
            duplicatedFile.existingFileName,
            duplicatedFile.duplicateFileName
          )
        )
      ))
  }

  final def submitedUploadAnotherFileChoice(
                                            upscanRequest: UpscanInitiateRequest,
                                            fileType: Option[FileType]
                                           )(upscanInitiate: UpscanInitiateApi)(state: FileUploadState)(implicit ec: ExecutionContext) =
    state match {
      case current@FileUploaded(fileUploads, acknowledged) =>
        fileUploadOrUploaded(
          upscanRequest,
          upscanInitiate,
          Some(fileUploads),
          showUploadSummaryIfAny = false,
          fileType
        )
      case _ => Future.successful(state)
    }


  def fileUploadWasRejected(error: S3UploadError)(state: FileUploadState) = Future.successful {
    state match {
      case current@UploadFile(
      reference,
      uploadRequest,
      fileUploads,
      maybeUploadError
      ) =>
        val updatedFileUploads = fileUploads.copy(files = fileUploads.files.map {
          case FileUpload.Initiated(orderNumber, ref, fileType) if ref == error.key =>
            FileUpload.Rejected(orderNumber, reference, error, fileType)
          case u => u
        })
        current.copy(fileUploads = updatedFileUploads, maybeUploadError = Some(FileTransmissionFailed(error)))
    }
  }

  def filesNotInStateInitiated(files: Seq[FileUpload]): Seq[FileUpload] = {
    files.filter {
      case s@Initiated(_, _, _) => false
      case _ => true
    }
  }

  def filesInStateAccepted(files: Seq[FileUpload]): Seq[FileUpload] = {
    files collect { case s@Accepted(_, _, _, _, _, _, _, _)  => s }
  }
}
