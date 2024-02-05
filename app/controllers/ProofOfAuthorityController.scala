/*
 * Copyright 2023 HM Revenue & Customs
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

import akka.actor.ActorRef
import akka.pattern.ask
import config.FrontendAppConfig
import connectors.{UpscanInitiateConnector, UpscanInitiateRequest}
import controllers.FileUploadUtils._
import controllers.actions._
import forms.UpscanS3ErrorFormProvider
import models.FileType.ProofOfAuthority
import models.SessionState
import models.requests.DataRequest
import navigation.CreateNavigator
import pages.ProofOfAuthorityPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import services.{FileUploadService, FileUploadState, FileUploaded, UploadFile}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ProofOfAuthorityView

import java.time.LocalDateTime
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class ProofOfAuthorityController @Inject() (
  override val messagesApi: MessagesApi,
  val appConfig: FrontendAppConfig,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  navigator: CreateNavigator,
  sessionRepository: SessionRepository,
  upscanInitiateConnector: UpscanInitiateConnector,
  val fileUtils: FileUploadUtils,
  val upscanS3ErrorFormProvider: UpscanS3ErrorFormProvider,
  @Named("check-state-actor") checkStateActor: ActorRef,
  val controllerComponents: MessagesControllerComponents,
  view: ProofOfAuthorityView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with FileUploadService with CheckStateSupport {

  final val controller      = routes.ProofOfAuthorityController
  val UpscanUploadErrorForm = upscanS3ErrorFormProvider()

  // GET /upload-proof-of-authority/file-verification
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
              Future.successful(Redirect(routes.ProofOfAuthorityController.showFileUpload()))
            }
          case _ =>
            Future.successful(
              redirectFileStateMissing(
                "ProofOfAuthority.showWaitingForFileVerification",
                routes.ProofOfAuthorityController.showFileUpload()
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
        fs <- initiateFileUpload(upscanRequest(request.internalId), Some(ProofOfAuthority))(
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

  // GET /upload-proof-of-authority/file-rejected
  final def markFileUploadAsRejected(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      UpscanUploadErrorForm.bindFromRequest().fold(
        _ => Future.successful(BadRequest),
        s3Error =>
          sessionRepository.getFileUploadState(request.internalId).flatMap { ss =>
            ss.state match {
              case Some(s) =>
                fileUtils.applyTransition(fileUploadWasRejected(s3Error)(_), s, ss).map(_ =>
                  Redirect(routes.ProofOfAuthorityController.showFileUpload())
                )
              case None =>
                Future.successful(
                  redirectFileStateMissing(
                    "ProofOfAuthority.markFileUploadAsRejected",
                    routes.ProofOfAuthorityController.showFileUpload()
                  )
                )
            }
          }
      )
    }

  final def upscanRequest(id: String): UpscanInitiateRequest =
    UpscanInitiateRequest(
      callbackUrl =
        appConfig.baseInternalCallbackUrl + internal.routes.ProofOfAuthorityUpscanCallbackController.callbackFromUpscan(
          id
        ).url,
      successRedirect = Some(appConfig.baseExternalCallbackUrl + controller.showWaitingForFileVerification()),
      errorRedirect = Some(appConfig.baseExternalCallbackUrl + controller.markFileUploadAsRejected()),
      minimumFileSize = Some(1),
      maximumFileSize = Some(appConfig.fileFormats.maxFileSizeMb * 1024 * 1024),
      expectedContentType = Some(appConfig.fileFormats.approvedFileTypes)
    )

  // GET /upload-proof-of-authority/file-verification/:reference/status
  final def checkFileVerificationStatus(reference: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      renderFileVerificationStatus(reference, request.userAnswers.fileUploadState)
    }

  // GET /upload-proof-of-authority/remove
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
          ).map(_ => Redirect(routes.ProofOfAuthorityController.showFileUpload()))
        case None =>
          Future.successful(
            redirectFileStateMissing("ProofOfAuthority.onRemove", routes.ProofOfAuthorityController.showFileUpload())
          )
      }
    }

  // POST /upload-proof-of-authority/continue
  def onContinue(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      if (request.userAnswers.fileUploadState.map(_.fileUploads.toFilesOfType(ProofOfAuthority)).contains(Seq.empty))
        redirectInternalError(routes.ProofOfAuthorityController.markFileUploadAsRejected(), "MissingFile")
      else
        Redirect(navigator.nextPage(ProofOfAuthorityPage, request.userAnswers))
  }

  final def renderState(fileUploadState: FileUploadState)(implicit request: DataRequest[_]): Result =
    fileUploadState match {
      case UploadFile(reference, uploadRequest, fileUploads, maybeUploadError) =>
        Ok(
          view(
            uploadRequest,
            fileUploads.toFilesOfType(ProofOfAuthority),
            maybeUploadError,
            navigator.previousPage(ProofOfAuthorityPage, request.userAnswers)
          )
        )
    }

}
