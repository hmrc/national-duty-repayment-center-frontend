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
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout
import config.FrontendAppConfig
import connectors.{UpscanInitiateConnector, UpscanInitiateRequest}
import controllers.actions._
import forms.AdditionalFileUploadFormProvider
import models.AmendCaseResponseType.Supportingdocuments
import models.FileType.SupportingEvidence
import models.{AmendCaseResponseType, AmendCaseUploadAnotherFile, CheckMode, ClaimantType, FileVerificationStatus, Mode, NormalMode, S3UploadError, UpscanNotification, UserAnswers}
import navigation.Navigator
import pages.{AmendCaseResponseTypePage, AmendCaseSendInformationPage, AmendCaseUploadAnotherFilePage, ClaimantTypePage, FurtherInformationPage}
import play.api.Logger.logger
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{request, _}
import play.mvc.Http.HeaderNames
import repositories.SessionRepository
import services._
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.{AmendCaseSendInformationView, AmendCaseUploadAnotherFileView, FileUploadView, FileUploadedView}

import java.time.LocalDateTime
import javax.inject.{Inject, Named}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class AmendCaseSendInformationController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        sessionRepository: SessionRepository,
                                        additionalFileUploadFormProvider: AdditionalFileUploadFormProvider,
                                        navigator: Navigator,
                                        appConfig: FrontendAppConfig,
                                        upscanInitiateConnector: UpscanInitiateConnector,
                                        val controllerComponents: MessagesControllerComponents,
                                        @Named("check-state-actor") checkStateActor: ActorRef,
                                        fileUploadView: AmendCaseSendInformationView,
                                        fileUploadedView: AmendCaseUploadAnotherFileView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with FileUploadService{

  final val controller = routes.AmendCaseSendInformationController
  val uploadAnotherFileChoiceForm = additionalFileUploadFormProvider.UploadAnotherFileChoiceForm
  type ConvertState = (FileUploadState) => Future[FileUploadState]
  case class SessionState(state: Option[FileUploadState], userAnswers: Option[UserAnswers])
  val fileStateError = InternalServerError("Missing file upload state")

  // GET /file-verification
  final val showWaitingForFileVerification = (identify andThen getData andThen requireData).async { implicit request =>
    implicit val timeout = Timeout(30 seconds)
    sessionState(request.internalId).flatMap { ss =>
      ss.state match {
        case Some(s) =>
          (checkStateActor ? CheckState(request.internalId, LocalDateTime.now.plusSeconds(30), s)).mapTo[FileUploadState].flatMap {
            case s: FileUploaded => Future.successful(Redirect(routes.AmendCaseSendInformationController.showFileUploaded(NormalMode)))
            case s: UploadFile => Future.successful(Redirect(routes.AmendCaseSendInformationController.showFileUpload()))
            case _ => Future.successful(fileStateError)
          }
        case _ => Future.successful(fileStateError)
      }
    }
  }

  //GET /file-verification/:reference/status
  final def checkFileVerificationStatus(reference: String): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    renderFileVerificationStatus(reference, request.userAnswers.fileUploadState)
  }

  final def removeFileUploadByReference(reference: String, mode : Mode = NormalMode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionState(request.internalId).flatMap { ss =>
      ss.state match {
        case Some(s) =>
          for {
            newState <- removeFileUploadBy(reference)(upscanRequest(request.internalId))(upscanInitiateConnector.initiate(_))(s)
            res <- updateSession(newState, ss.userAnswers)
            if res
          } yield {
            newState match {
              case s@FileUploaded(_, _) => Redirect(controller.showFileUploaded(NormalMode))
              case s@UploadFile(_, _, _, _) => Redirect(controller.showFileUpload())
              case s@_ => renderState(s)
            }
          }
        case None => Future.successful(fileStateError)
      }
    }
  }

  //GET /file-upload
  def showFileUpload: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionState(request.internalId).flatMap { ua =>
      ua.userAnswers.flatMap(_.fileUploadState) match {
        case Some(s@UploadFile(reference, uploadRequest, fileUploads, maybeUploadError)) =>
          for {
            fs <-  initiateFileUpload(upscanRequest(request.internalId), Some(SupportingEvidence))(upscanInitiateConnector.initiate(_))(Some(s.copy(maybeUploadError = None)))
            b <- updateSession(fs, ua.userAnswers)
            if b
          } yield renderState(s)
        case _ => {
          val state = request.userAnswers.fileUploadState
          for {
            fileUploadState <- initiateFileUpload(upscanRequest(request.internalId), Some(SupportingEvidence))(upscanInitiateConnector.initiate(_))(state)
            res <- updateSession(fileUploadState, Some(request.userAnswers))
            if res
          } yield renderState(fileUploadState)
        }
      }
    }
  }

  //GET /file-uploaded
  def showFileUploaded(mode: Mode = NormalMode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionState(request.internalId).flatMap { ua =>
      ua.userAnswers.flatMap(_.fileUploadState) match {
        case s@Some(FileUploaded(_, _)) => Future.successful(renderState(s.get, None, Some(mode)))
        case s => Future.successful(fileStateError)

      }
    }
  }

  // POST /file-uploaded
  final def submitUploadAnotherFileChoice(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    uploadAnotherFileChoiceForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(fileUploadedView(
        formWithErrors,
        request.userAnswers.fileUploadState.get.fileUploads,
        controller.submitUploadAnotherFileChoice(mode),
        controller.removeFileUploadByReference,
        controller.showFileUpload(),
        NormalMode
      ))),
      value =>
        sessionState(request.internalId).flatMap { ss =>
          ss.state match {
            case Some(s) =>
              if (value)
                submitedUploadAnotherFileChoice(upscanRequest(request.internalId),Some(SupportingEvidence))(upscanInitiateConnector.initiate(_))(s).flatMap {
                  newState => updateSession(newState, ss.userAnswers).map { _ => Redirect(routes.AmendCaseSendInformationController.showFileUpload())}
                }
              else Future.successful(Redirect(getAmendCaseUploadAnotherFile(request.userAnswers)))
            case None => Future.successful(fileStateError)
          }
        }
    )
  }

  private def getAmendCaseUploadAnotherFile(answers: UserAnswers): Call = {

    if (hasFurtherInformation(answers))
      routes.FurtherInformationController.onPageLoad(NormalMode)
    else
      routes.AmendCheckYourAnswersController.onPageLoad
  }

  def hasFurtherInformation(userAnswers: UserAnswers): Boolean  = {
    userAnswers.get(AmendCaseResponseTypePage) match {
      case Some(s) => s.contains(AmendCaseResponseType.Furtherinformation)
      case _ => false
    }
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
                Redirect(routes.AmendCaseSendInformationController.showFileUpload())
              }
            }
            case None => Future.successful(InternalServerError("Missing file upload state"))
          }
        }
    )
  }

  // POST /ndrc/:id/callback-from-upscan
  final def callbackFromUpscan(id: String) = Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>

    sessionState(id).flatMap { ss =>
      ss.state match {
        case Some(s) => upscanCallbackArrived(request.body, SupportingEvidence)(s).flatMap { newState =>
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

  private def updateSession(newState: FileUploadState, userAnswers: Option[UserAnswers]) = {
    if (userAnswers.nonEmpty)
      sessionRepository.set(userAnswers = userAnswers.get.copy(fileUploadState = Some(newState)))
    else Future.successful(true)
  }

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
  def sessionState(id: String): Future[SessionState] = {
    for {
      u <- sessionRepository.get(id)
    } yield (SessionState(u.flatMap(_.fileUploadState), u))
  }

  final def renderState(fileUploadState: FileUploadState, formWithErrors: Option[Form[_]] = None, mode: Option[Mode] = None)(implicit request: Request[_]): Result = {
    fileUploadState match {
      case UploadFile(reference, uploadRequest, fileUploads, maybeUploadError) => {
        Ok(
          fileUploadView(
            uploadRequest,
            fileUploads,
            maybeUploadError,
            successAction = controller.showFileUploaded(mode.getOrElse(NormalMode)),
            failureAction = controller.showFileUpload,
            checkStatusAction = controller.checkFileVerificationStatus(reference),
            backLink = routes.AmendCaseResponseTypeController.onPageLoad(mode.getOrElse(NormalMode)))
        )
      }

      case FileUploaded(fileUploads, _) =>
        Ok(fileUploadedView(
          or(formWithErrors, uploadAnotherFileChoiceForm, None),
          fileUploads,
          controller.submitUploadAnotherFileChoice(mode.getOrElse(NormalMode)),
          controller.removeFileUploadByReference,
          controller.showFileUpload(),
          NormalMode
        ))
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
}