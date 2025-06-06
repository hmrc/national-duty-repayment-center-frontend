/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import config.FrontendAppConfig
import connectors.{UpscanInitiateConnector, UpscanInitiateRequest}
import controllers.FileUploadUtils._
import controllers.actions._
import forms.UpscanS3ErrorFormProvider
import models.FileType.Bulk
import models.requests.DataRequest
import models.{SessionState, UserAnswers}
import navigation.CreateNavigator
import org.apache.pekko.actor.ActorRef
import org.apache.pekko.pattern.ask
import pages.{BulkFileUploadPage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import services.{FileUploadService, FileUploadState, FileUploaded, UploadFile}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BulkFileUploadView

import java.time.LocalDateTime
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class BulkFileUploadController @Inject() (
  override val messagesApi: MessagesApi,
  val appConfig: FrontendAppConfig,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val navigator: CreateNavigator,
  upscanInitiateConnector: UpscanInitiateConnector,
  val fileUtils: FileUploadUtils,
  val upscanS3ErrorFormProvider: UpscanS3ErrorFormProvider,
  @Named("check-state-actor") checkStateActor: ActorRef,
  val controllerComponents: MessagesControllerComponents,
  view: BulkFileUploadView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with FileUploadService with CheckStateSupport
    with Navigation[UserAnswers] {

  override val page: Page            = BulkFileUploadPage
  final val bulkFileUploadController = routes.BulkFileUploadController
  val UpscanUploadErrorForm          = upscanS3ErrorFormProvider()

  // GET /file-verification
  final def showWaitingForFileVerification() = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.getFileUploadState(request.internalId).flatMap { ss =>
        ss.state match {
          case Some(s) =>
            (checkStateActor ? CheckState(
              request.internalId,
              LocalDateTime.now.plusSeconds(appConfig.fileUploadTimeout.toSeconds),
              s
            )).mapTo[FileUploadState].flatMap { _ =>
              Future.successful(Redirect(bulkFileUploadController.showFileUpload()))
            }
          case _ =>
            Future.successful(
              redirectFileStateMissing(
                "BulkFile.showWaitingForFileVerification",
                bulkFileUploadController.showFileUpload()
              )
            )
        }
      }
  }

  def showFileUpload(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        ss <- sessionRepository.getFileUploadState(request.internalId)
        s  <- Future.successful(ss.userAnswers.flatMap(_.fileUploadState))
        fs <- initiateFileUpload(upscanRequest(request.internalId), Some(Bulk))(upscanInitiateConnector.initiate(_))(s)
        b <- fs match {
          case f @ UploadFile(_, _, _, _) =>
            sessionRepository.updateSession(f.copy(maybeUploadError = None), ss.userAnswers)
          case _ => sessionRepository.updateSession(fs, ss.userAnswers)
        }
        if b
      } yield renderState(fs)
  }

  // GET /file-rejected
  final def markFileUploadAsRejected(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      UpscanUploadErrorForm.bindFromRequest().fold(
        _ => Future.successful(BadRequest),
        s3Error =>
          sessionRepository.getFileUploadState(request.internalId).flatMap { ss =>
            ss.state match {
              case Some(s) =>
                fileUtils.applyTransition(fileUploadWasRejected(s3Error)(_), s, ss).map(_ =>
                  Redirect(routes.BulkFileUploadController.showFileUpload())
                )
              case None =>
                Future.successful(
                  redirectFileStateMissing(
                    "BulkFile.markFileUploadAsRejected",
                    bulkFileUploadController.showFileUpload()
                  )
                )
            }
          }
      )
    }

  final def upscanRequest(id: String): UpscanInitiateRequest =
    UpscanInitiateRequest(
      callbackUrl =
        appConfig.baseInternalCallbackUrl + internal.routes.UpscanCallbackController.bulkUploadCallbackFromUpscan(
          id
        ).url,
      successRedirect =
        Some(appConfig.baseExternalCallbackUrl + bulkFileUploadController.showWaitingForFileVerification()),
      errorRedirect = Some(appConfig.baseExternalCallbackUrl + bulkFileUploadController.markFileUploadAsRejected()),
      minimumFileSize = Some(1),
      maximumFileSize = Some(appConfig.fileFormats.maxFileSizeMb * 1024 * 1024),
      expectedContentType = Some(appConfig.fileFormats.approvedFileTypes)
    )

  // GET /bulk/file-verification/:reference/status
  final def checkFileVerificationStatus(reference: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      renderFileVerificationStatus(reference, request.userAnswers.fileUploadState)
    }

  // GET /upload-multiple-entries/remove
  final def onRemove(reference: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.fileUploadState match {
        case Some(fus) =>
          val acceptedFiles =
            FileUploaded(fileUploads = fus.fileUploads.copy(files = filesInStateAccepted(fus.fileUploads.files)))
          val sessionState = SessionState(Some(acceptedFiles), Some(request.userAnswers))
          fileUtils.applyTransition(
            removeFileUploadBy(reference)(upscanRequest(request.internalId))(upscanInitiateConnector.initiate(_))(_),
            acceptedFiles,
            sessionState
          ).map(_ => Redirect(bulkFileUploadController.showFileUpload()))
        case None =>
          Future.successful(redirectFileStateMissing("BulkFile.onRemove", bulkFileUploadController.showFileUpload()))
      }
    }

  // POST /upload-multiple-entries/continue
  def onContinue(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      if (request.userAnswers.fileUploadState.map(_.fileUploads.toFilesOfType(Bulk)).contains(Seq.empty))
        redirectInternalError(bulkFileUploadController.markFileUploadAsRejected(), "MissingFile")
      else
        Redirect(navigator.nextPage(BulkFileUploadPage, request.userAnswers))
  }

  final def renderState(fileUploadState: FileUploadState)(implicit request: DataRequest[_]): Result =
    fileUploadState match {
      case UploadFile(reference, uploadRequest, fileUploads, maybeUploadError) =>
        Ok(
          view(
            uploadRequest,
            fileUploads.toFilesOfType(Bulk),
            maybeUploadError,
            successAction = routes.EntryDetailsController.onPageLoad(),
            failureAction = routes.BulkFileUploadController.showFileUpload(),
            checkStatusAction = routes.BulkFileUploadController.checkFileVerificationStatus(reference),
            backLink = backLink(request.userAnswers)
          )
        )
    }

}
