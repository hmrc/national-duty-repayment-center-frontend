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

package models

import models.FileType.SupportingEvidence
import play.api.libs.json.{Format, Json}

import java.time.ZonedDateTime

case class FileUploads(files: Seq[FileUpload] = Seq.empty) {

  def isEmpty: Boolean  = acceptedCount == 0
  def nonEmpty: Boolean = !isEmpty
  def isSingle: Boolean = acceptedCount == 1

  def initiateCount: Int =
    files
      .count { case f: FileUpload.Initiated => true; case _ => false }

  def acceptedCount: Int =
    files
      .count { case f: FileUpload.Accepted if f.fileType.contains(SupportingEvidence) => true; case _ => false }

  def toUploadedFiles: Seq[UploadedFile] =
    files.collect {
      case f: FileUpload.Accepted =>
        UploadedFile(f.reference, f.url, f.uploadTimestamp, f.checksum, f.fileName, f.fileMimeType)
    }

  def toFilesOfType(fileType: FileType): Seq[UploadedFile] =
    files.collect {
      case f: FileUpload.Accepted if f.fileType.contains(fileType) =>
        UploadedFile(f.reference, f.url, f.uploadTimestamp, f.checksum, f.fileName, f.fileMimeType)
    }

  def +(file: FileUpload): FileUploads = copy(files = files :+ file)

}

object FileUploads {
  implicit val formats: Format[FileUploads] = Json.format[FileUploads]
}

sealed trait FileType

object FileType extends EnumerationFormats[FileType] {

  case object Bulk               extends FileType
  case object SupportingEvidence extends FileType
  case object ProofOfAuthority   extends FileType

  val values                = Set(Bulk, SupportingEvidence, ProofOfAuthority)
  val singleUploadFileTypes = Set(Bulk, ProofOfAuthority)
}

/** File upload status */
sealed trait FileUpload {
  def orderNumber: Int
  def reference: String
  def checksumOpt: Option[String] = None
  val fileType: Option[FileType]

}

object FileUpload extends SealedTraitFormats[FileUpload] {

  def unapply(fileUpload: FileUpload): Option[(Int, String)] =
    Some((fileUpload.orderNumber, fileUpload.reference))

  /**
    * Status when file upload attributes has been requested from upscan-initiate
    * but the file itself has not been yet transmitted to S3 bucket.
    */
  case class Initiated(orderNumber: Int, reference: String, fileType: Option[FileType] = None) extends FileUpload

  /** Status when file transmission has been rejected by AWS S3. */
  case class Rejected(orderNumber: Int, reference: String, details: S3UploadError, fileType: Option[FileType] = None)
      extends FileUpload

  /** Status when the file has been positively verified and is ready for further actions. */
  case class Accepted(
    orderNumber: Int,
    reference: String,
    url: String,
    uploadTimestamp: ZonedDateTime,
    checksum: String,
    fileName: String,
    fileMimeType: String,
    fileType: Option[FileType] = None
  ) extends FileUpload {

    override def checksumOpt: Option[String] = Some(checksum)
  }

  /** Status when the file has failed verification and may not be used. */
  case class Failed(
    orderNumber: Int,
    reference: String,
    details: UpscanNotification.FailureDetails,
    fileType: Option[FileType] = None
  ) extends FileUpload

  /** Status when the file is a duplicate of an existing upload. */
  case class Duplicate(
    orderNumber: Int,
    reference: String,
    checksum: String,
    existingFileName: String,
    duplicateFileName: String,
    fileType: Option[FileType] = None
  ) extends FileUpload

  override val formats =
    Set(
      Case[Initiated](Json.format[Initiated]),
      Case[Rejected](Json.format[Rejected]),
      Case[Accepted](Json.format[Accepted]),
      Case[Failed](Json.format[Failed]),
      Case[Duplicate](Json.format[Duplicate])
    )

}
