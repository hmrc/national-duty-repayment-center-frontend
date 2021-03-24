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
import models.FileUpload.Initiated
import models.{ClaimantType, FileVerificationStatus, Mode, NormalMode, S3UploadError, SessionState, UpscanNotification, UserAnswers}
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
import views.html.{FileUploadView, FileUploadedView}

import java.time.LocalDateTime
import javax.inject.{Inject, Named}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class FileUploadController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      appConfig: FrontendAppConfig,
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      sessionRepository: SessionRepository,
                                      upscanInitiateConnector: UpscanInitiateConnector,
                                      val controllerComponents: MessagesControllerComponents,
                                      additionalFileUploadFormProvider: AdditionalFileUploadFormProvider,
                                      val upscanS3ErrorFormProvider: UpscanS3ErrorFormProvider,
                                      fileUploadedView: FileUploadedView,
                                      requireData: DataRequiredAction,
                                      @Named("check-state-actor") checkStateActor: ActorRef,
                                      fileUploadView: FileUploadView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with FileUploadService with FileUploadCommons {

  final val controller = routes.FileUploadController
  val uploadAnotherFileChoiceForm = additionalFileUploadFormProvider.UploadAnotherFileChoiceForm
  val UpscanUploadErrorForm = upscanS3ErrorFormProvider()

  // GET /file-verification
  final def showWaitingForFileVerification(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    implicit val timeout = Timeout(30 seconds)
    sessionRepository.getFileUploadState(request.internalId).flatMap { ss =>
      ss.state match {
        case Some(s) =>
          (checkStateActor ? CheckState(request.internalId, LocalDateTime.now.plusSeconds(30), s)).mapTo[FileUploadState].flatMap {
            case _: FileUploaded => Future.successful(Redirect(routes.FileUploadController.showFileUploaded(mode)))
            case _: UploadFile => Future.successful(Redirect(routes.FileUploadController.showFileUpload(mode)))
            case _ => Future.successful(missingFileUploadState)
          }
        case _ => Future.successful(missingFileUploadState)
      }
    }
  }

  //GET /file-verification/:reference/status
  final def checkFileVerificationStatus(reference: String): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    renderFileVerificationStatus(reference, request.userAnswers.fileUploadState)
  }

  final def removeFileUploadByReference(reference: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionRepository.getFileUploadState(request.internalId).flatMap { ss =>
      ss.state match {
        case Some(s) => applyTransition(removeFileUploadBy(reference)(upscanRequest(request.internalId, mode))(upscanInitiateConnector.initiate(_))(_), s, ss).map {
          case _@FileUploaded(_, _) => Redirect(routes.FileUploadController.showFileUploaded(mode))
          case _@UploadFile(_, _, _, _) => Redirect(routes.FileUploadController.showFileUpload(mode))
          case s@_ => renderState(fileUploadState = s, mode = mode)
        }
        case None => Future.successful(missingFileUploadState)
      }
    }
  }

  //GET /file-upload
  def showFileUpload(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      ss <- sessionRepository.getFileUploadState(request.internalId)
      s <- Future.successful(ss.userAnswers.flatMap(_.fileUploadState))
      fs <- initiateFileUpload(upscanRequest(request.internalId, mode), Some(SupportingEvidence))(upscanInitiateConnector.initiate(_))(s)
      b <- fs match {
        case f@UploadFile(_, _, _, _) => sessionRepository.updateSession(f.copy(maybeUploadError = None), ss.userAnswers)
        case _ => sessionRepository.updateSession(fs, ss.userAnswers)
      }
      if b
    } yield renderState(fs, None, mode)
  }

  //GET /file-uploaded
  def showFileUploaded(mode : Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      ss <- sessionRepository.getFileUploadState(request.internalId)
      s <- Future.successful(ss.userAnswers.flatMap(_.fileUploadState))
      if s.nonEmpty
    } yield {
      Ok(fileUploadedView(
        uploadAnotherFileChoiceForm,
        s.get.fileUploads,
        controller.submitUploadAnotherFileChoice(mode),
        controller.removeFileUploadByReference,
        routes.FileUploadController.showFileUpload(mode),
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
        controller.showFileUpload(mode),
        mode
      ))),
      value =>
        sessionRepository.getFileUploadState(request.internalId).flatMap { ss =>
          ss.state match {
            case Some(s) if value =>
              applyTransition(submitedUploadAnotherFileChoice(upscanRequest(request.internalId, mode), Some(SupportingEvidence))(upscanInitiateConnector.initiate(_))(_), s, ss)
                .map(_ => Redirect(routes.FileUploadController.showFileUpload(mode)))
            case Some(_) => Future.successful(Redirect(additionalFileUploadRoute(request.userAnswers)))
            case None => Future.successful(missingFileUploadState)
          }
        }
    )
  }

  // GET /file-rejected
  final def markFileUploadAsRejected(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    UpscanUploadErrorForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      s3Error =>
        sessionRepository.getFileUploadState(request.internalId).flatMap { ss =>
          ss.state match {
            case Some(s) => applyTransition(fileUploadWasRejected(s3Error)(_), s, ss).map(_ => Redirect(routes.FileUploadController.showFileUpload(mode)))
            case None => Future.successful(InternalServerError("Missing file upload state"))
          }
        }
    )
  }

  // POST /ndrc/:id/callback-from-upscan
  final def callbackFromUpscan(id: String): Action[UpscanNotification] = Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>
    sessionRepository.getFileUploadState(id).flatMap { ss =>
      ss.state match {
        case Some(s) => applyTransition(upscanCallbackArrived(request.body, SupportingEvidence)(_), s, ss).map(newState => acknowledgeFileUploadRedirect(newState))
        case None => Future.successful(missingFileUploadState)
      }
    }
  }

  final def upscanRequest(id: String, mode: Mode): UpscanInitiateRequest = {
    UpscanInitiateRequest(
      callbackUrl = appConfig.baseInternalCallbackUrl + controller.callbackFromUpscan(id).url,
      successRedirect = Some(appConfig.baseExternalCallbackUrl + controller.showWaitingForFileVerification(mode)),
      errorRedirect = Some(appConfig.baseExternalCallbackUrl + controller.markFileUploadAsRejected(mode)),
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


  def backLink(mode: Mode) : Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    if(mode == NormalMode)
      Future.successful(Redirect(routes.EvidenceSupportingDocsController.onPageLoad()))
    else {
      for {
        ss <- sessionRepository.getFileUploadState(request.internalId)
        fs <- Future.successful(ss.userAnswers.flatMap(_.fileUploadState))
        res <- sessionRepository.updateSession(FileUploaded(fs.get.fileUploads.copy(files = fs.get.fileUploads.files.filterNot(_.isInstanceOf[Initiated]))), ss.userAnswers)
        if(res)
      } yield Redirect(routes.FileUploadController.showFileUploaded(mode))
    }
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
            checkStatusAction = controller.checkFileVerificationStatus(reference),
            backLink = routes.EvidenceSupportingDocsController.onPageLoad())
        )
      }

      case FileUploaded(fileUploads, _) =>
        Ok(fileUploadedView(
          formWithErrors.getOrElse(uploadAnotherFileChoiceForm),
          fileUploads,
          controller.submitUploadAnotherFileChoice(mode),
          controller.removeFileUploadByReference,
          routes.FileUploadController.showFileUpload(mode),
          mode
        ))
    }
  }

  private def applyTransition(f: ConvertStateApi, s: FileUploadState,  ss: SessionState):  Future[FileUploadState] = {
    for {
      newState <- f(s)
      res <- sessionRepository.updateSession(newState, ss.userAnswers)
      if res
    } yield newState
  }
}
