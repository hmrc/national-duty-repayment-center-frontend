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
import forms.ClaimRepaymentTypeFormProvider
import javax.inject.Inject
import models.{ClaimRepaymentType, UserAnswers}
import navigation.CreateNavigator
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.ClaimRepaymentTypeView

import scala.concurrent.{ExecutionContext, Future}

class ClaimRepaymentTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  val navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ClaimRepaymentTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ClaimRepaymentTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = ClaimRepaymentTypePage
  val form                = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ClaimRepaymentTypePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, backLink(request.userAnswers), request.userAnswers.isImporterJourney))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(
            BadRequest(view(formWithErrors, backLink(request.userAnswers), request.userAnswers.isImporterJourney))
          ),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ClaimRepaymentTypePage, value))
            removeCustomsPaid <-
              Future.fromTry(updatedAnswers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Customs) match {
                case false => updatedAnswers.remove(CustomsDutyPaidPage)
                case true  => updatedAnswers.set(ClaimRepaymentTypePage, value)
              })
            removeVATDue <-
              Future.fromTry(removeCustomsPaid.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Vat) match {
                case false => removeCustomsPaid.remove(VATPaidPage)
                case true  => removeCustomsPaid.set(ClaimRepaymentTypePage, value)
              })
            removeVATPaid <-
              Future.fromTry(removeVATDue.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Vat) match {
                case false => removeVATDue.remove(VATPaidPage)
                case true  => removeVATDue.set(ClaimRepaymentTypePage, value)
              })
            removeOtherDutiesPaid <-
              Future.fromTry(removeVATPaid.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Other) match {
                case false => removeVATPaid.remove(OtherDutiesPaidPage)
                case true  => removeVATPaid.set(ClaimRepaymentTypePage, value)
              })
            _ <- sessionRepository.set(removeOtherDutiesPaid)
          } yield Redirect(nextPage(removeOtherDutiesPaid))
      )
  }

}
