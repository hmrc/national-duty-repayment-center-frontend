/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.ClaimReasonTypeFormProvider
import models.UserAnswers
import navigation.CreateNavigator
import pages.{ClaimReasonTypeMultiplePage, ClaimReasonTypePage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ClaimReasonTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimReasonTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  val navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ClaimReasonTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ClaimReasonTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = ClaimReasonTypePage
  val form                = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(ClaimReasonTypeMultiplePage) match {
        case Some(reasons) if reasons.size > 1 =>
          val preparedForm = request.userAnswers.get(ClaimReasonTypePage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, reasons, backLink(request.userAnswers)))
        case _ => Redirect(nextPage(request.userAnswers))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(ClaimReasonTypeMultiplePage) match {
        case Some(reasons) if reasons.size > 1 =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, reasons, backLink(request.userAnswers)))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ClaimReasonTypePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(nextPage(updatedAnswers))
          )

        case _ => Future.successful(Redirect(nextPage(request.userAnswers)))
      }

  }

}
