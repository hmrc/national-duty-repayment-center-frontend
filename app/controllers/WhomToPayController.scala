/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.WhomToPayFormProvider
import javax.inject.Inject
import models.FileType.ProofOfAuthority
import models._
import navigation.CreateNavigator
import pages.{BankDetailsPage, IndirectRepresentativePage, Page, WhomToPayPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.WhomToPayView

import scala.concurrent.{ExecutionContext, Future}

class WhomToPayController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  val navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: WhomToPayFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhomToPayView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = WhomToPayPage
  val form                = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(WhomToPayPage) match {
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
          if (!request.userAnswers.get(WhomToPayPage).contains(value))
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(WhomToPayPage, value))
              updatedAnswers <- Future.fromTry(updatedAnswers.remove(IndirectRepresentativePage, BankDetailsPage))
              updatedAnswers <- Future.fromTry(updatedAnswers.removeFile(ProofOfAuthority))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(nextPage(updatedAnswers))
          else
            for {
              userAnswers <- Future.fromTry(request.userAnswers.set(WhomToPayPage, value))
              _           <- sessionRepository.set(userAnswers)
            } yield Redirect(nextPage(userAnswers))
      )
  }

}
