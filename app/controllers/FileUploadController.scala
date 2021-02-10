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
import models.{ClaimantType, FileVerificationStatus, NormalMode, S3UploadError, UpscanNotification, UserAnswers}
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
                                      fileUploadedView: FileUploadedView,
                                      requireData: DataRequiredAction,
                                      @Named("check-state-actor") checkStateActor: ActorRef,
                                      fileUploadView: FileUploadView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with FileUploadService {

  final val COOKIE_JSENABLED = "jsenabled"
  final val controller = routes.FileUploadController
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
          (checkStateActor ? StopWaiting(LocalDateTime.now.plusSeconds(30))).recover {
            case e: AskTimeoutException => {
              //println("handling exception")
              false
            }}.mapTo[Boolean].flatMap {
            case true => {
              //println("i have received the response")
              sessionRepository.get(request.internalId).flatMap(ss => ss.flatMap(_.fileUploadState) match {
                case Some(s@FileUploaded(_, _)) => {
                  //println("Going to file uploaded")
                  Future.successful(Redirect(routes.FileUploadController.showFileUploaded()))
                }
                case Some(s@UploadFile(reference, uploadRequest, fileUploads, maybeUploadError)) => {
                  //println(s"Going to file upload $s and ${s.maybeUploadError}")
                  Future.successful(Redirect(routes.FileUploadController.showFileUpload()))
                }
              })
            }
            case false => Future.successful(Redirect(routes.FileUploadController.showFileUpload()))
          }
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
        case Some(s) =>
          for {
            newState <- removeFileUploadBy(reference)(upscanRequest(request.internalId))(upscanInitiateConnector.initiate(_))(s)
            res <- updateSession(newState, ss.userAnswers)
            if res
          } yield {
            newState match {
              case s@UploadFile(_, _, _, _) => Redirect(routes.FileUploadController.showFileUploaded())
              case s@_ => renderState(s)
            }
          }
        case None => Future.successful(fileStateError)
      }
    }
  }

  //GET /file-upload
  val showFileUpload: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val state = request.userAnswers.fileUploadState
    for {
      fileUploadState <- initiateFileUpload(upscanRequest(request.internalId))(upscanInitiateConnector.initiate(_))(state)
      res <- updateSession(fileUploadState, Some(request.userAnswers))
      if res
    } yield renderState(fileUploadState)

  }

  //GET /file-uploaded
  def showFileUploaded: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.fileUploadState match {
      case Some(s: FileUploadState) if (s.fileUploads.nonEmpty) => renderState(s)
      case _ => Redirect(routes.FileUploadController.showFileUpload()) //TODO: For future stories this might need to be conditional
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
                submitedUploadAnotherFileChoice(upscanRequest(request.internalId))(upscanInitiateConnector.initiate(_))(s).flatMap {
                  newState => updateSession(newState, ss.userAnswers).map { _ => Redirect(routes.FileUploadController.showFileUpload())}
                }
              else Future.successful(Redirect(additionalFileUploadRoute(request.userAnswers)))
            case None => Future.successful(fileStateError)
          }
        }
    )
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
                renderState(newState, Some(UpscanUploadErrorForm.fill(s3Error)))
              }
            }
            case None => Future.successful(InternalServerError("Missing file upload state"))
          }
        }
    )
  }

  // POST /ndrc/:id/callback-from-upscan
  final def callbackFromUpscan(id: String) = Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>
    implicit val timeout = Timeout(30 seconds)

    sessionState(id).flatMap { ss =>
      ss.state match {
        case Some(s) => upscanCallbackArrived(request.body)(s).flatMap { newState =>
            updateSession(newState, ss.userAnswers).map { res =>
              //println(s"result of updating session $res with state $newState" )
              checkStateActor ! CallbackArrived
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
    if (userAnswers.nonEmpty) {
      //println(s"setting state $newState")
      sessionRepository.set(userAnswers = userAnswers.get.copy(fileUploadState = Some(newState)))
    }
     else Future.successful(true)
  }

  final def successRedirect(id: String)(implicit rh: RequestHeader) = appConfig.baseExternalCallbackUrl + (rh.cookies.get(COOKIE_JSENABLED) match {
    case Some(_) => controller.showWaitingForFileVerification
    //TODO: THE ABOVE SHOULD BE CHANGED TO THIS: controller.asyncWaitingForFileVerification(id) once javascript works
    case None => controller.showWaitingForFileVerification
  })

  final def errorRedirect(id: String)(implicit rh: RequestHeader) =
    appConfig.baseExternalCallbackUrl + (rh.cookies.get(COOKIE_JSENABLED) match {
      case Some(_) => controller.markFileUploadAsRejected
      case None => controller.markFileUploadAsRejected
    })


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

  def sessionState(id: String): Future[SessionState] = {
    for {
      u <- sessionRepository.get(id)
    } yield (SessionState(u.flatMap(_.fileUploadState), u))
  }

  final def renderState(fileUploadState: FileUploadState, formWithErrors: Option[Form[_]] = None)(implicit request: Request[_]): Result = {
    fileUploadState match {
      case UploadFile(reference, uploadRequest, fileUploads, maybeUploadError) => {
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
      }

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
