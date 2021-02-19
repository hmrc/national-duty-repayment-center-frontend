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

import controllers.actions._
import forms.IndirectRepresentativeFormProvider
import models.FileType.ProofOfAuthority

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.IndirectRepresentativePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Request}
import repositories.SessionRepository
import services.{FileUploadState, FileUploaded, UploadFile}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.IndirectRepresentativeView

import scala.concurrent.{ExecutionContext, Future}

class IndirectRepresentativeController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: IndirectRepresentativeFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: IndirectRepresentativeView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  private def getBackLink(mode: Mode): Call = {
    routes.WhomToPayController.onPageLoad(mode)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(IndirectRepresentativePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, getBackLink(mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, getBackLink(mode)))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(IndirectRepresentativePage, value))
            _  <- sessionRepository.set(updatedAnswers.copy(fileUploadState = updatedFs(updatedAnswers, value)))
          } yield Redirect(navigator.nextPage(IndirectRepresentativePage, mode, updatedAnswers))
          )
  }
  private def updatedFs(ua: UserAnswers, isIndirectRepresentative: Boolean): Option[FileUploadState] = {
    if(isIndirectRepresentative) {
      ua.fileUploadState match {
        case Some(s@FileUploaded(fileUploads, _)) => Some(s.copy(fileUploads = fileUploads.copy(files = fileUploads.files.filterNot(_.fileType.contains(ProofOfAuthority)))))
        case Some(s@UploadFile(_, _, fileUploads, _)) => Some(s.copy(fileUploads = fileUploads.copy(files = fileUploads.files.filterNot(_.fileType.contains(ProofOfAuthority)))))
        case _ => ua.fileUploadState
      }
    } else ua.fileUploadState
  }
}
