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

import config.FrontendAppConfig
import connectors.{UpscanInitiateConnector, UpscanInitiateRequest}
import controllers.actions._
import forms.AdditionalFileUploadFormProvider
import models.{ClaimantType, FileVerificationStatus, NormalMode, S3UploadError, UpscanNotification, UserAnswers}
import navigation.Navigator
import pages.ClaimantTypePage
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import play.mvc.Http.HeaderNames
import repositories.SessionRepository
import services._
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.{FileUploadView, FileUploadedView, WaitingForFileVerificationView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileUploadController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      appConfig: FrontendAppConfig,
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      sessionRepository: SessionRepository,
                                      upscanInitiateConnector: UpscanInitiateConnector,
                                      val controllerComponents: MessagesControllerComponents,
                                      waitingForFileVerificationView: WaitingForFileVerificationView,
                                      additionalFileUploadFormProvider: AdditionalFileUploadFormProvider,
                                      fileUploadedView: FileUploadedView,
                                      requireData: DataRequiredAction,
                                      fileUploadView: FileUploadView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with FileUploadService {


  final val COOKIE_JSENABLED = "jsenabled"
  final val controller = routes.FileUploadController
  final val INITIAL_CALLBACK_WAIT_TIME_MILLIS = 2000
  val uploadAnotherFileChoiceForm = additionalFileUploadFormProvider.UploadAnotherFileChoiceForm
  type ConvertState = (FileUploadState) => Future[FileUploadState]

  case class SessionState(state: Option[FileUploadState], userAnswers: Option[UserAnswers])

  // GET /file-verification
  final def showWaitingForFileVerification = {
    (identify andThen getData andThen requireData).async { implicit request =>
      val answers = request.userAnswers
      answers.fileUploadState match {
        case Some(s) => applyTransition(s, Some(answers), waitForFileVerification).map(ns => renderState(ns))
        case None => Future.successful(InternalServerError("Missing file upload state"))
      }
    }
  }

  // GET/async/file-verification
  final def asyncWaitingForFileVerification(id: String): Action[AnyContent] = Action.async { implicit request =>
    Thread.sleep(INITIAL_CALLBACK_WAIT_TIME_MILLIS)
    sessionState(id).flatMap { ss =>
      ss.state match {
        case Some(s@UploadFile(_, _, _, _)) => applyTransition(s, ss.userAnswers, waitForFileVerification).map(newState => acknowledgeFileUploadRedirect(newState))
        case Some(s@WaitingForFileVerification(_, _, _, _)) => applyTransition(s, ss.userAnswers, waitForFileVerification).map(newState => acknowledgeFileUploadRedirect(newState))
        case Some(s@FileUploaded(_, _)) => Future.successful(renderState(s))
        case _ => Future.successful(InternalServerError("Missing file upload state"))
      }
    }
  }

  // GET /file-rejected
  final def markFileUploadAsRejected: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    UpscanUploadErrorForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      s3Error =>
        request.userAnswers.fileUploadState match {
          case Some(s) => applyTransition(s, Some(request.userAnswers), fileUploadWasRejected(s3Error)(_)).map(newState => renderState(newState, Some(UpscanUploadErrorForm.fill(s3Error))))
          case None => Future.successful(InternalServerError("Missing file upload state"))
        }
    )
  }

  //GET /file-rejected/:id
  final def asyncMarkFileUploadAsRejected(id: String): Action[AnyContent] = Action.async { implicit request =>
    UpscanUploadErrorForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      s3Error =>
        sessionState(id).flatMap { ss =>
          ss.state match {
            case Some(s) => applyTransition(s, ss.userAnswers, fileUploadWasRejected(s3Error)(_)).map(newState => acknowledgeFileUploadRedirect(newState))
            case None => Future.successful(InternalServerError("Missing file upload state"))
          }
        }
    )
  }

  // POST /ndrc/:id/callback-from-upscan
  final def callbackFromUpscan(id: String) = Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>
    sessionState(id).flatMap { ss =>
      ss.state match {
        case Some(s) => applyTransition(s, ss.userAnswers, upscanCallbackArrived(request.body)(_)).map(newState => acknowledgeFileUploadRedirect(newState))
        case None => Future.successful(InternalServerError("Missing file upload state"))
      }
    }
  }

  //GET /file-verification/:reference/status
  final def checkFileVerificationStatus(reference: String): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    renderFileVerificationStatus(reference, request.userAnswers.fileUploadState)
  }

  final def removeFileUploadByReference(reference: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionState(request.internalId).flatMap { ss =>
      ss.state match {
        case Some(s) => applyTransition(s, ss.userAnswers,
          removeFileUploadBy(reference)(upscanRequest(request.internalId))(upscanInitiateConnector.initiate(_))(_)).map(newState => renderState(newState))
        case None => Future.successful(InternalServerError("Missing file upload state"))
      }
    }
  }

  //GET /file-upload
  val showFileUpload: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val state = request.userAnswers.fileUploadState
    for {
      fileUploadState <- initiateFileUpload(upscanRequest(request.internalId))(upscanInitiateConnector.initiate(_))(state)
      res <- updateSession(fileUploadState, Some(request.userAnswers))
      if (res)
    } yield renderState(fileUploadState)
  }

  //GET /file-uploaded
  def showFileUploaded: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.fileUploadState match {
      case Some(s: FileUploadState) if (s.fileUploads.nonEmpty) => renderState(s)
      case _ => Redirect(routes.EvidenceSupportingDocsController.onPageLoad()) //TODO: For future stories this might need to be conditional
    }
  }

  // POST /file-uploaded
  final def submitUploadAnotherFileChoice: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    uploadAnotherFileChoiceForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(fileUploadedView(
        formWithErrors,
        request.userAnswers.fileUploadState.get.fileUploads,
        controller.submitUploadAnotherFileChoice(),
        controller.removeFileUploadByReference,
        controller.showFileUpload()
      ))),
      value =>
        sessionState(request.internalId).flatMap { ss =>
          ss.state match {
            case Some(s) =>
              if (value)
                applyTransition(s, ss.userAnswers, submitedUploadAnotherFileChoice(upscanRequest(request.internalId))(upscanInitiateConnector.initiate(_))(_))
                  .map(newState => renderState(newState))
              else Future.successful(Redirect(additionalFileUploadRoute(request.userAnswers)))
            case None => Future.successful(InternalServerError("Missing file upload state"))
          }
        }
    )
  }

  final def successRedirect(id: String)(implicit rh: RequestHeader) = appConfig.baseExternalCallbackUrl + (rh.cookies.get(COOKIE_JSENABLED) match {
    case Some(_) => controller.asyncWaitingForFileVerification(id)
    case None => controller.showWaitingForFileVerification
  })

  final def errorRedirect(id: String)(implicit rh: RequestHeader) =
    appConfig.baseExternalCallbackUrl + (rh.cookies.get(COOKIE_JSENABLED) match {
      case Some(_) => controller.asyncMarkFileUploadAsRejected(id)
      case None => controller.markFileUploadAsRejected
    })

  def applyTransition(state: FileUploadState, userAnswers: Option[UserAnswers], cs: ConvertState) = {
    for {
      newState <- cs(state)
      res <- updateSession(newState, userAnswers)
      if (res)
    } yield newState
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

  private def updateSession(newState: FileUploadState, userAnswers: Option[UserAnswers]) = {
    if (userAnswers.nonEmpty)
      sessionRepository.set(userAnswers = userAnswers.get.copy(fileUploadState = Some(newState)))
    else Future.successful(true)
  }

  private def acknowledgeFileUploadRedirect(state: FileUploadState)(
    implicit request: Request[_]
  ): Result =
    (state match {
      case _: FileUploaded => Created
      case _: WaitingForFileVerification => Accepted
      case _ => NoContent
    }).withHeaders(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*")


  final def upscanRequest(id: String)(implicit rh: RequestHeader): UpscanInitiateRequest = {
    UpscanInitiateRequest(
      callbackUrl = appConfig.baseInternalCallbackUrl + controller.callbackFromUpscan(id).url,
      successRedirect = Some(successRedirect(id)),
      errorRedirect = Some(errorRedirect(id)),
      minimumFileSize = Some(1),
      maximumFileSize = Some(appConfig.fileFormats.maxFileSizeMb * 1024 * 1024),
      expectedContentType = Some(appConfig.fileFormats.approvedFileTypes)
    )
  }

  private def additionalFileUploadRoute(answers: UserAnswers): Call = {
    if (answers.get(ClaimantTypePage).contains(ClaimantType.Importer))
      routes.ImporterHasEoriController.onPageLoad(NormalMode)
    else
      routes.AgentImporterHasEORIController.onPageLoad(NormalMode)
  }

  private def sessionState(id: String): Future[SessionState] = {
    for {
      u <- sessionRepository.get(id)
    } yield (SessionState(u.flatMap(_.fileUploadState), u))
  }

  final def renderState(fileUploadState: FileUploadState, formWithErrors: Option[Form[_]] = None)(implicit request: Request[_]): Result = {
    fileUploadState match {
      case UploadFile(reference, uploadRequest, fileUploads, maybeUploadError) =>
        Ok(
          fileUploadView(
            uploadRequest,
            fileUploads,
            maybeUploadError,
            successAction = controller.showFileUploaded,
            failureAction = controller.showFileUpload,
            checkStatusAction = controller.checkFileVerificationStatus(reference),
            backLink = routes.EvidenceSupportingDocsController.onPageLoad()) //TODO: for more than one entry the back link should be diff. Make this method conditional when we get there
        )

      case WaitingForFileVerification(reference, _, _, _) =>
        Ok(
          waitingForFileVerificationView(
            successAction = controller.showFileUploaded,
            failureAction = controller.showFileUpload,
            checkStatusAction = controller.checkFileVerificationStatus(reference),
            backLink = controller.showFileUpload
          )
        )
      case FileUploaded(fileUploads, _) =>
        Ok(fileUploadedView(
          or(formWithErrors, uploadAnotherFileChoiceForm, None),
          fileUploads,
          controller.submitUploadAnotherFileChoice(),
          controller.removeFileUploadByReference,
          controller.showFileUpload()
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
