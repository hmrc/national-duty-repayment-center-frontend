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
import forms.{AdditionalFileUploadFormProvider, UpscanS3ErrorFormProvider}
import models.FileType.SupportingEvidence
import models.{AmendCaseResponseType, FileVerificationStatus, Mode, NormalMode, UpscanNotification, UserAnswers}
import navigation.Navigator
import pages.AmendCaseResponseTypePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import play.mvc.Http.HeaderNames
import repositories.SessionRepository
import services._
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.{AmendCaseSendInformationView, AmendCaseUploadAnotherFileView}
import models.SessionState
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
                                                    val upscanS3ErrorFormProvider: UpscanS3ErrorFormProvider,
                                                    @Named("check-state-actor") checkStateActor: ActorRef,
                                                    fileUploadView: AmendCaseSendInformationView,
                                                    fileUploadedView: AmendCaseUploadAnotherFileView
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with FileUploadService {

  final val controller = routes.AmendCaseSendInformationController
  val uploadAnotherFileChoiceForm = additionalFileUploadFormProvider.UploadAnotherFileChoiceForm
  val UpscanUploadErrorForm = upscanS3ErrorFormProvider()
  val fileStateError = InternalServerError("Missing file upload state")

  // GET /file-verification
  final def showWaitingForFileVerification(mode: Mode) = (identify andThen getData andThen requireData).async { implicit request =>
    implicit val timeout = Timeout(30 seconds)
    sessionState(request.internalId).flatMap { ss =>
      ss.state match {
        case Some(s) =>
          (checkStateActor ? CheckState(request.internalId, LocalDateTime.now.plusSeconds(30), s)).mapTo[FileUploadState].flatMap {
            case _: FileUploaded => Future.successful(Redirect(routes.AmendCaseSendInformationController.showFileUploaded(mode)))
            case _: UploadFile => Future.successful(Redirect(routes.AmendCaseSendInformationController.showFileUpload(mode)))
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

  final def removeFileUploadByReference(reference: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionState(request.internalId).flatMap { ss =>
      ss.state match {
        case Some(s) =>
          for {
            newState <- removeFileUploadBy(reference)(upscanRequest(request.internalId, mode))(upscanInitiateConnector.initiate(_))(s)
            res <- updateSession(newState, ss.userAnswers)
            if res
          } yield {
            newState match {
              case s@FileUploaded(_, _) => Redirect(controller.showFileUploaded(mode))
              case s@UploadFile(_, _, _, _) => Redirect(controller.showFileUpload(mode))
              case s@_ => renderState(fileUploadState = s, mode = mode)
            }
          }
        case None => Future.successful(fileStateError)
      }
    }
  }

  //GET /file-upload
  def showFileUpload(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      ss <- sessionState(request.internalId)
      s <- Future.successful(ss.userAnswers.flatMap(_.fileUploadState))
      fs <- initiateFileUpload(upscanRequest(request.internalId, mode), Some(SupportingEvidence))(upscanInitiateConnector.initiate(_))(s)
      b <- fs match {
        case f@UploadFile(_, _, _, _) => updateSession(f.copy(maybeUploadError = None), ss.userAnswers)
        case _ => updateSession(fs, ss.userAnswers)
      }
      if b
    } yield renderState(fs, mode = mode)
  }

  //GET /file-uploaded
  def showFileUploaded(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      ss <- sessionState(request.internalId)
      s <- Future.successful(ss.userAnswers.flatMap(_.fileUploadState))
      if s.nonEmpty
    } yield {
      Ok(fileUploadedView(
        uploadAnotherFileChoiceForm,
        s.get.fileUploads,
        controller.submitUploadAnotherFileChoice(mode),
        controller.removeFileUploadByReference,
        mode
      ))
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
        mode
      ))),
      value =>
        sessionState(request.internalId).flatMap { ss =>
          ss.state match {
            case Some(s) if (value) =>
              for {
                newState <- submitedUploadAnotherFileChoice(upscanRequest(request.internalId, mode), Some(SupportingEvidence))(upscanInitiateConnector.initiate(_))(s)
                res <- updateSession(newState, ss.userAnswers)
                if res
              } yield Redirect(routes.AmendCaseSendInformationController.showFileUpload(mode))
            case Some(_) => Future.successful(Redirect(getAmendCaseUploadAnotherFile(request.userAnswers, mode)))
            case None => Future.successful(fileStateError)
          }
        }
    )
  }

  private def getAmendCaseUploadAnotherFile(answers: UserAnswers, mode: Mode): Call = {
    if (hasFurtherInformation(answers))
      routes.FurtherInformationController.onPageLoad(mode)
    else
      routes.AmendCheckYourAnswersController.onPageLoad()
  }

  def hasFurtherInformation(userAnswers: UserAnswers): Boolean = {
    userAnswers.get(AmendCaseResponseTypePage) match {
      case Some(s) => s.contains(AmendCaseResponseType.FurtherInformation)
      case _ => false
    }
  }

  // GET /file-rejected
  final def markFileUploadAsRejected(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>

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
              } yield Redirect(routes.AmendCaseSendInformationController.showFileUpload(mode))
            case None => Future.successful(InternalServerError("Missing file upload state"))
          }
        }
    )
  }

  // POST /ndrc/:id/callback-from-upscan
  final def callbackFromUpscan(id: String) = Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>
    sessionState(id).flatMap { ss =>
      ss.state match {
        case Some(s) =>
          for {
            newState <- upscanCallbackArrived(request.body, SupportingEvidence)(s)
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

  private def updateSession(newState: FileUploadState, userAnswers: Option[UserAnswers]): Future[Boolean] = {
    if (userAnswers.nonEmpty)
      sessionRepository.set(userAnswers = userAnswers.get.copy(fileUploadState = Some(newState)))
    else Future.successful(true)
  }

  final def upscanRequest(id: String, mode: Mode)(implicit rh: RequestHeader): UpscanInitiateRequest = {
    UpscanInitiateRequest(
      callbackUrl = appConfig.baseInternalCallbackUrl + controller.callbackFromUpscan(id).url,
      successRedirect = Some(appConfig.baseExternalCallbackUrl + controller.showWaitingForFileVerification(mode)),
      errorRedirect = Some(appConfig.baseExternalCallbackUrl + controller.markFileUploadAsRejected(mode)),
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

  final def renderState(fileUploadState: FileUploadState, formWithErrors: Option[Form[_]] = None, mode: Mode)(implicit request: Request[_]): Result = {
    fileUploadState match {
      case UploadFile(reference, uploadRequest, fileUploads, maybeUploadError) => {
        Ok(
          fileUploadView(
            uploadRequest,
            fileUploads,
            maybeUploadError,
            successAction = controller.showFileUploaded(mode),
            failureAction = controller.showFileUpload(mode),
            checkStatusAction = controller.checkFileVerificationStatus(reference))
        )
      }

      case FileUploaded(fileUploads, _) =>
        Ok(fileUploadedView(
          formWithErrors.getOrElse(uploadAnotherFileChoiceForm),
          fileUploads,
          controller.submitUploadAnotherFileChoice(mode),
          controller.removeFileUploadByReference,
          mode
        ))
    }
  }
}