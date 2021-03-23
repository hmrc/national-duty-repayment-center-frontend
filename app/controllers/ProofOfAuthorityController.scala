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

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import config.FrontendAppConfig
import connectors.{UpscanInitiateConnector, UpscanInitiateRequest}
import controllers.actions._
import forms.UpscanS3ErrorFormProvider
import models.FileType.ProofOfAuthority
import models.{FileVerificationStatus, NormalMode, SessionState, UpscanNotification, UserAnswers}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import play.mvc.Http.HeaderNames
import repositories.SessionRepository
import services.{FileUploadService, FileUploadState, FileUploaded, UploadFile}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.ProofOfAuthorityView

import java.time.LocalDateTime
import javax.inject.{Inject, Named}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class ProofOfAuthorityController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            appConfig: FrontendAppConfig,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            sessionRepository: SessionRepository,
                                            upscanInitiateConnector: UpscanInitiateConnector,
                                            val upscanS3ErrorFormProvider: UpscanS3ErrorFormProvider,
                                            @Named("check-state-actor") checkStateActor: ActorRef,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: ProofOfAuthorityView
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with FileUploadService {

  final val controller = routes.ProofOfAuthorityController
  val UpscanUploadErrorForm = upscanS3ErrorFormProvider()
  val fileStateError = InternalServerError("Missing file upload state")

  // GET /upload-proof-of-authority/file-verification
  final val showWaitingForFileVerification = (identify andThen getData andThen requireData).async { implicit request =>
    implicit val timeout = Timeout(10 seconds)
    sessionState(request.internalId).flatMap { ss =>
      ss.state match {
        case Some(s) =>
          (checkStateActor ? CheckState(request.internalId, LocalDateTime.now.plusSeconds(30), s)).mapTo[FileUploadState].flatMap {
            case _: FileUploaded => Future.successful(Redirect(routes.BankDetailsController.onPageLoad(NormalMode)))
            case _: UploadFile => Future.successful(Redirect(routes.ProofOfAuthorityController.showFileUpload))
            case _ => Future.successful(fileStateError)
          }
        case _ => Future.successful(fileStateError)
      }
    }
  }

  def showFileUpload: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      ss <- sessionState(request.internalId)
      s <- Future.successful(ss.userAnswers.flatMap(_.fileUploadState))
      fs <- initiateFileUpload(upscanRequest(request.internalId), Some(ProofOfAuthority))(upscanInitiateConnector.initiate(_))(s)
      b <- fs match {
        case f@UploadFile(_, _, _, _) => updateSession(f.copy(maybeUploadError = None), ss.userAnswers)
        case _ => updateSession(fs, ss.userAnswers)
      }
      if b
    } yield renderState(fs)
  }

  def sessionState(id: String): Future[SessionState] = {
    for {
      u <- sessionRepository.get(id)
    } yield (SessionState(u.flatMap(_.fileUploadState), u))
  }

  // GET /upload-proof-of-authority/file-rejected
  final def markFileUploadAsRejected: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    UpscanUploadErrorForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      s3Error =>
        sessionState(request.internalId).flatMap { ss =>
          ss.state match {
            case Some(s) =>
              for {
                newState <- Future.successful(fileUploadWasRejected(s3Error)(s))
                res <- updateSession(newState, ss.userAnswers)
                if res
              } yield Redirect(routes.ProofOfAuthorityController.showFileUpload())
            case None => Future.successful(InternalServerError("Missing file upload state"))
          }
        }
    )
  }

  private def updateSession(newState: FileUploadState, userAnswers: Option[UserAnswers]) = {
    if (userAnswers.nonEmpty)
      sessionRepository.set(userAnswers = userAnswers.get.copy(fileUploadState = Some(newState)))
    else Future.successful(true)
  }

  // POST /upload-proof-of-authority/:id/callback-from-upscan
  final def callbackFromUpscan(id: String) = Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>

    sessionState(id).flatMap { ss =>
      ss.state match {
        case Some(s) => for {
          newState <- upscanCallbackArrived(request.body, ProofOfAuthority)(s)
          res <- updateSession(newState, ss.userAnswers)
          if res
        } yield acknowledgeFileUploadRedirect(newState)
        case None => Future.successful(InternalServerError("Missing file upload state"))
      }
    }
  }

  private def acknowledgeFileUploadRedirect(state: FileUploadState)(
    implicit request: Request[_]
  ): Result =
    (state match {
      case _: FileUploaded => Created
      case _ => NoContent
    }).withHeaders(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*")


  final def upscanRequest(id: String)(implicit rh: RequestHeader): UpscanInitiateRequest = {
    UpscanInitiateRequest(
      callbackUrl = appConfig.baseInternalCallbackUrl + controller.callbackFromUpscan(id).url,
      successRedirect = Some(appConfig.baseExternalCallbackUrl + controller.showWaitingForFileVerification),
      errorRedirect = Some(appConfig.baseExternalCallbackUrl + controller.markFileUploadAsRejected),
      minimumFileSize = Some(1),
      maximumFileSize = Some(appConfig.fileFormats.maxFileSizeMb * 1024 * 1024),
      expectedContentType = Some(appConfig.fileFormats.approvedFileTypes)
    )
  }


  //GET /upload-proof-of-authority/file-verification/:reference/status
  final def checkFileVerificationStatus(reference: String): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    renderFileVerificationStatus(reference, request.userAnswers.fileUploadState)
  }

  private def renderFileVerificationStatus(
                                            reference: String, state: Option[FileUploadState])(implicit request: Request[_]
                                          ): Result = {

    state match {
      case Some(s: FileUploadState) =>
        s.fileUploads.files.find(_.reference == reference) match {
          case Some(f) => Ok(Json.toJson(FileVerificationStatus(f)))
          case None => NotFound
        }
      case _ => NotFound
    }
  }

  final def renderState(fileUploadState: FileUploadState, formWithErrors: Option[Form[_]] = None)(implicit request: Request[_]): Result = {
    fileUploadState match {
      case UploadFile(reference, uploadRequest, fileUploads, maybeUploadError) =>
        Ok(
          view(
            uploadRequest,
            fileUploads,
            maybeUploadError,
            successAction = routes.BankDetailsController.onPageLoad(NormalMode),
            failureAction = routes.ProofOfAuthorityController.showFileUpload,
            checkStatusAction = routes.ProofOfAuthorityController.checkFileVerificationStatus(reference),
            backLink = routes.IndirectRepresentativeController.onPageLoad(NormalMode))
        )
    }
  }
}
