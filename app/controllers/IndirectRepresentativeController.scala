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
import javax.inject.Inject
import models.FileType.ProofOfAuthority
import models.UserAnswers
import navigation.CreateNavigator
import pages.{IndirectRepresentativePage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{FileUploadState, FileUploaded, UploadFile}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IndirectRepresentativeView

import scala.concurrent.{ExecutionContext, Future}

class IndirectRepresentativeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  val navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: IndirectRepresentativeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IndirectRepresentativeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = IndirectRepresentativePage
  val form                = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(IndirectRepresentativePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, backLink(request.userAnswers)))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, backLink(request.userAnswers)))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(IndirectRepresentativePage, value))
            _              <- sessionRepository.set(updatedAnswers.copy(fileUploadState = updatedFs(updatedAnswers, value)))
          } yield Redirect(nextPage(updatedAnswers))
      )
  }

  private def updatedFs(ua: UserAnswers, isIndirectRepresentative: Boolean): Option[FileUploadState] =
    if (isIndirectRepresentative)
      ua.fileUploadState match {
        case Some(s @ FileUploaded(fileUploads, _)) =>
          Some(
            s.copy(fileUploads =
              fileUploads.copy(files = fileUploads.files.filterNot(_.fileType.contains(ProofOfAuthority)))
            )
          )
        case Some(s @ UploadFile(_, _, fileUploads, _)) =>
          Some(
            s.copy(fileUploads =
              fileUploads.copy(files = fileUploads.files.filterNot(_.fileType.contains(ProofOfAuthority)))
            )
          )
        case _ => ua.fileUploadState
      }
    else ua.fileUploadState

}
