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

package controllers

import java.time.LocalDateTime

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import config.FrontendAppConfig
import connectors.{UpscanInitiateConnector, UpscanInitiateRequest}
import controllers.FileUploadUtils.{fileStateErrror, _}
import controllers.actions._
import forms.UpscanS3ErrorFormProvider
import javax.inject.{Inject, Named}
import models.FileType.ProofOfAuthority
import models.UpscanNotification
import models.requests.DataRequest
import navigation.CreateNavigator
import pages.{ProofOfAuthorityPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import services.{FileUploadService, FileUploadState, FileUploaded, UploadFile}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.ProofOfAuthorityView

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class ProofOfAuthorityController @Inject() (
  override val messagesApi: MessagesApi,
  appConfig: FrontendAppConfig,
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
    extends FrontendBaseController with I18nSupport with FileUploadService {

  final val controller      = routes.ProofOfAuthorityController
  val UpscanUploadErrorForm = upscanS3ErrorFormProvider()

  // GET /upload-proof-of-authority/file-verification
  final def showWaitingForFileVerification() = (identify andThen getData andThen requireData).async {
    implicit request =>
      implicit val timeout = Timeout(10 seconds)
      sessionRepository.getFileUploadState(request.internalId).flatMap { ss =>
        ss.state match {
          case Some(s) =>
            (checkStateActor ? CheckState(request.internalId, LocalDateTime.now.plusSeconds(10), s)).mapTo[
              FileUploadState
            ].flatMap {
              case _: FileUploaded =>
                Future.successful(Redirect(navigator.nextPage(ProofOfAuthorityPage, request.userAnswers)))
              case _: UploadFile => Future.successful(Redirect(routes.ProofOfAuthorityController.showFileUpload()))
              case _             => Future.successful(fileStateErrror)
            }
          case _ => Future.successful(fileStateErrror)
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
                fileUtils.applyTransition(fileUploadWasRejected(s3Error)(_), s, ss).map(
                  _ => Redirect(routes.ProofOfAuthorityController.showFileUpload())
                )
              case None => Future.successful(fileStateErrror)
            }
          }
      )
    }

  // POST /upload-proof-of-authority/:id/callback-from-upscan
  final def callbackFromUpscan(id: String): Action[UpscanNotification] =
    Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>
      sessionRepository.getFileUploadState(id).flatMap { ss =>
        ss.state match {
          case Some(s) =>
            fileUtils.applyTransition(upscanCallbackArrived(request.body, ProofOfAuthority)(_), s, ss).map(
              newState => acknowledgeFileUploadRedirect(newState)
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

  //GET /upload-proof-of-authority/file-verification/:reference/status
  final def checkFileVerificationStatus(reference: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      renderFileVerificationStatus(reference, request.userAnswers.fileUploadState)
    }

  final def renderState(fileUploadState: FileUploadState)(implicit request: DataRequest[_]): Result =
    fileUploadState match {
      case UploadFile(reference, uploadRequest, fileUploads, maybeUploadError) =>
        Ok(
          view(
            uploadRequest,
            fileUploads,
            maybeUploadError,
            successAction = routes.BankDetailsController.onPageLoad(),
            failureAction = routes.ProofOfAuthorityController.showFileUpload(),
            checkStatusAction = routes.ProofOfAuthorityController.checkFileVerificationStatus(reference),
            navigator.previousPage(ProofOfAuthorityPage, request.userAnswers)
          )
        )
    }

}
