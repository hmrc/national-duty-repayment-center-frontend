/*
 * Copyright 2024 HM Revenue & Customs
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

import java.time.LocalDateTime

import org.apache.pekko.actor.ActorRef
import org.apache.pekko.pattern.ask
import config.FrontendAppConfig
import connectors.{UpscanInitiateConnector, UpscanInitiateRequest}
import controllers.FileUploadUtils._
import controllers.actions._
import forms.UpscanS3ErrorFormProvider
import javax.inject.{Inject, Named}
import models.FileType.SupportingEvidence
import models.requests.DataRequest
import models.{S3UploadError, SessionState, UpscanNotification}
import navigation.CreateNavigator
import pages.{ClaimReasonTypeMultiplePage, FileUploadPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import services._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.FileUploadView

import scala.concurrent.{ExecutionContext, Future}

class FileUploadController @Inject() (
  override val messagesApi: MessagesApi,
  val appConfig: FrontendAppConfig,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  sessionRepository: SessionRepository,
  navigator: CreateNavigator,
  upscanInitiateConnector: UpscanInitiateConnector,
  val controllerComponents: MessagesControllerComponents,
  val upscanS3ErrorFormProvider: UpscanS3ErrorFormProvider,
  val fileUtils: FileUploadUtils,
  requireData: DataRequiredAction,
  @Named("check-state-actor") checkStateActor: ActorRef,
  fileUploadView: FileUploadView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with FileUploadService with CheckStateSupport {

  final val controller                           = routes.FileUploadController
  val UpscanUploadErrorForm: Form[S3UploadError] = upscanS3ErrorFormProvider()

  // GET /file-verification
  final def showWaitingForFileVerification(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      sessionRepository.getFileUploadState(request.internalId).flatMap { ss =>
        ss.state match {
          case Some(s) =>
            (checkStateActor ? CheckState(
              request.internalId,
              LocalDateTime.now.plusSeconds(appConfig.fileUploadTimeout.toSeconds),
              s
            )).mapTo[FileUploadState].flatMap { _ =>
              Future.successful(Redirect(routes.FileUploadController.showFileUpload()))
            }
          case _ =>
            Future.successful(
              redirectFileStateMissing(
                "FileUpload.showWaitingForFileVerification",
                routes.FileUploadController.showFileUpload()
              )
            )
        }
      }
    }

  // GET /file-verification/:reference/status
  final def checkFileVerificationStatus(reference: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      renderFileVerificationStatus(reference, request.userAnswers.fileUploadState)
    }

  final def onRemove(reference: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.fileUploadState match {
        case Some(s) =>
          val acceptedFiles =
            FileUploaded(fileUploads = s.fileUploads.copy(files = filesInStateAccepted(s.fileUploads.files)))
          val sessionState = SessionState(Some(acceptedFiles), Some(request.userAnswers))
          fileUtils.applyTransition(
            removeFileUploadBy(reference)(upscanRequest(request.internalId))(upscanInitiateConnector.initiate(_))(_),
            acceptedFiles,
            sessionState
          ).map(_ => Redirect(routes.FileUploadController.showFileUpload()))
        case None =>
          Future.successful(
            redirectFileStateMissing("FileUpload.onRemove", routes.FileUploadController.showFileUpload())
          )
      }

    }

  // POST /file-upload
  def onContinue(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      if (request.userAnswers.fileUploadState.map(_.fileUploads.toFilesOfType(SupportingEvidence)).contains(Seq.empty))
        redirectInternalError(routes.FileUploadController.markFileUploadAsRejected(), "MissingFile")
      else
        Redirect(navigator.nextPage(FileUploadPage, request.userAnswers))
  }

  // GET /file-upload
  def showFileUpload(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        ss <- sessionRepository.getFileUploadState(request.internalId)
        s  <- Future.successful(ss.userAnswers.flatMap(_.fileUploadState))
        fs <- initiateFileUpload(upscanRequest(request.internalId), Some(SupportingEvidence))(
          upscanInitiateConnector.initiate(_)
        )(s)
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
                if (s3Error.isMissingFileError && s.fileUploads.nonEmpty)
                  Future.successful(Redirect(navigator.nextPage(FileUploadPage, request.userAnswers)))
                else
                  fileUtils.applyTransition(fileUploadWasRejected(s3Error)(_), s, ss).map(_ =>
                    Redirect(routes.FileUploadController.showFileUpload())
                  )
              case None =>
                Future.successful(
                  redirectFileStateMissing(
                    "FileUpload.markFileUploadAsRejected",
                    routes.FileUploadController.showFileUpload()
                  )
                )
            }
          }
      )
    }

  // POST /callback-from-upscan/ndrc/:id
  final def callbackFromUpscan(id: String): Action[UpscanNotification] =
    Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>
      sessionRepository.getFileUploadState(id).flatMap { ss =>
        ss.state match {
          case Some(s) =>
            fileUtils.applyTransition(upscanCallbackArrived(request.body, SupportingEvidence)(_), s, ss).map(newState =>
              acknowledgeFileUploadRedirect(newState)
            )
          case None => Future.successful(fileStateErrror)
        }
      }
    }

  final def upscanRequest(id: String): UpscanInitiateRequest =
    UpscanInitiateRequest(
      callbackUrl = appConfig.baseInternalCallbackUrl + controller.callbackFromUpscan(id).url,
      successRedirect = Some(appConfig.baseExternalCallbackUrl + controller.showWaitingForFileVerification()),
      errorRedirect = Some(appConfig.baseExternalCallbackUrl + controller.markFileUploadAsRejected()),
      minimumFileSize = Some(1),
      maximumFileSize = Some(appConfig.fileFormats.maxFileSizeMb * 1024 * 1024),
      expectedContentType = Some(appConfig.fileFormats.approvedFileTypes)
    )

  final def renderState(fileUploadState: FileUploadState)(implicit request: DataRequest[_]): Result =
    fileUploadState match {
      case UploadFile(_, uploadRequest, fileUploads, maybeUploadError) =>
        Ok(
          fileUploadView(
            uploadRequest,
            fileUploads.toFilesOfType(SupportingEvidence),
            maybeUploadError,
            request.userAnswers.get(ClaimReasonTypeMultiplePage).getOrElse(Set.empty),
            navigator.previousPage(FileUploadPage, request.userAnswers)
          )
        )
      case _ => fileStateErrror
    }

}
