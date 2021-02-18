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
import models.FileType.Bulk
import models.{CustomsRegulationType, FileVerificationStatus, NormalMode, S3UploadError, UpscanNotification, UserAnswers}
import pages.CustomsRegulationTypePage
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import play.mvc.Http.HeaderNames
import repositories.SessionRepository
import services.{FileUploadService, FileUploadState, FileUploaded, UploadFile}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.BulkFileUploadView

import java.time.LocalDateTime
import javax.inject.{Inject, Named}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class BulkFileUploadController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          appConfig: FrontendAppConfig,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          sessionRepository: SessionRepository,
                                          upscanInitiateConnector: UpscanInitiateConnector,
                                          @Named("check-state-actor") checkStateActor: ActorRef,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: BulkFileUploadView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with FileUploadService {

  final val bulkFileUploadController = routes.BulkFileUploadController
  case class SessionState(state: Option[FileUploadState], userAnswers: Option[UserAnswers])
  val fileStateError = InternalServerError("Missing file upload state")

  // GET /file-verification
  final val showWaitingForFileVerification = (identify andThen getData andThen requireData).async { implicit request =>
    implicit val timeout = Timeout(10 seconds)
    sessionState(request.internalId).flatMap { ss =>
      ss.state match {
        case Some(s) =>
          (checkStateActor ? CheckState(request.internalId, LocalDateTime.now.plusSeconds(30), s)).mapTo[FileUploadState].flatMap {
            case s: FileUploaded => Future.successful(Redirect(getBulkEntryDetails(Some(request.userAnswers))))
            case s: UploadFile => Future.successful(Redirect(routes.BulkFileUploadController.showFileUpload))
            case _ => Future.successful(fileStateError)
          }
        case _ => Future.successful(fileStateError)
      }
    }
  }

  val showFileUpload: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionState(request.internalId).flatMap { ua =>
      ua.userAnswers.flatMap(_.fileUploadState) match {
        case Some(s@UploadFile(reference, uploadRequest, fileUploads, maybeUploadError)) =>
          for {
            fs <-  initiateFileUpload(upscanRequest(request.internalId), Some(Bulk))(upscanInitiateConnector.initiate(_))(Some(s.copy(maybeUploadError = None)))
            b <- updateSession(fs, ua.userAnswers)
            if b
          } yield renderState(ua.userAnswers, s)
        case _ => {
          val state = request.userAnswers.fileUploadState
          for {
            fileUploadState <- initiateFileUpload(upscanRequest(request.internalId), Some(Bulk))(upscanInitiateConnector.initiate(_))(state)
            res <- updateSession(fileUploadState, Some(request.userAnswers))
            if res
          } yield renderState(ua.userAnswers, fileUploadState)
        }
      }
    }
  }

  def sessionState(id: String): Future[SessionState] = {
    for {
      u <- sessionRepository.get(id)
    } yield (SessionState(u.flatMap(_.fileUploadState), u))
  }

  // GET /file-rejected
  final def markFileUploadAsRejected: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>

    UpscanUploadErrorForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      s3Error =>
        sessionState(request.internalId).flatMap { ss =>
          ss.state match {
            case Some(s) => fileUploadWasRejected(s3Error)(s).flatMap { newState =>
              updateSession(newState, ss.userAnswers).map { res =>
                Redirect(routes.BulkFileUploadController.showFileUpload())
              }
            }
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

  // POST /bulk/:id/callback-from-upscan
  final def callbackFromUpscan(id: String) = Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>

    sessionState(id).flatMap { ss =>
      ss.state match {
        case Some(s) => upscanCallbackArrived(request.body, Bulk)(s).flatMap { newState =>
          updateSession(newState, ss.userAnswers).map { res =>
            acknowledgeFileUploadRedirect(newState)
          }
        }
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
      callbackUrl = appConfig.baseInternalCallbackUrl + bulkFileUploadController.callbackFromUpscan(id).url,
      successRedirect = Some(appConfig.baseExternalCallbackUrl + bulkFileUploadController.showWaitingForFileVerification),
      errorRedirect = Some(appConfig.baseExternalCallbackUrl + bulkFileUploadController.markFileUploadAsRejected),
      minimumFileSize = Some(1),
      maximumFileSize = Some(appConfig.fileFormats.maxFileSizeMb * 1024 * 1024),
      expectedContentType = Some(appConfig.fileFormats.approvedFileTypes)
    )
  }


  //GET /bulk/file-verification/:reference/status
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

  final def renderState(userAnswers: Option[UserAnswers], fileUploadState: FileUploadState, formWithErrors: Option[Form[_]] = None)(implicit request: Request[_]): Result = {
    fileUploadState match {
      case UploadFile(reference, uploadRequest, fileUploads, maybeUploadError) =>
        Ok(
          view(
            uploadRequest,
            fileUploads,
            maybeUploadError,
            successAction = getBulkEntryDetails(userAnswers),
            failureAction = routes.BulkFileUploadController.showFileUpload,
            checkStatusAction = routes.BulkFileUploadController.checkFileVerificationStatus(reference),
            backLink = routes.CustomsRegulationTypeController.onPageLoad(NormalMode)) //TODO: for more than one entry the back link should be diff. Make this method conditional when we get there
        )
    }
  }

  def or[T](formWithErrors: Option[Form[_]], emptyForm: Form[T], maybeFillWith: Option[T])(implicit request: Request[_]): Form[T] =
    formWithErrors
      .map(_.asInstanceOf[Form[T]])
      .getOrElse {
        if (request.flash.isEmpty) maybeFillWith.map(emptyForm.fill).getOrElse(emptyForm)
        else emptyForm.bind(request.flash.data)
      }

  val UpscanUploadErrorForm = Form[S3UploadError](
    mapping(
      "key" -> nonEmptyText,
      "errorCode" -> text,
      "errorMessage" -> text,
      "errorRequestId" -> optional(text),
      "errorResource" -> optional(text)
    )(S3UploadError.apply)(S3UploadError.unapply)
  )
  private def getBulkEntryDetails(answers: Option[UserAnswers]): Call = answers.flatMap(_ .get(CustomsRegulationTypePage)) match {
    case Some(CustomsRegulationType.UnionsCustomsCodeRegulation)  => routes.ArticleTypeController.onPageLoad(NormalMode)
    case _ => routes.EntryDetailsController.onPageLoad(NormalMode)
  }
}