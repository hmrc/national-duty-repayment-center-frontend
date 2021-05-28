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

import akka.actor.Status.Success
import controllers.actions._
import forms.BankDetailsFormProvider
import models.bars.BARSResult

import javax.inject.Inject
import models.{BankDetails, ClaimantType, Mode, RepaymentType, UserAnswers, WhomToPay}
import navigation.Navigator
import pages.{BankDetailsPage, ClaimantTypePage, IndirectRepresentativePage, RepaymentTypePage, WhomToPayPage}
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.BankAccountReputationService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.BankDetailsView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class BankDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        bankAccountReputationService: BankAccountReputationService,
                                        requireData: DataRequiredAction,
                                        formProvider: BankDetailsFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: BankDetailsView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(BankDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
            bankAccountReputationService.validate(value) map {

              case barsResult if barsResult.isValid => {

                val triedAnswers = request.userAnswers.set(BankDetailsPage, value).get
                sessionRepository.set(triedAnswers)
                Redirect(navigator.nextPage(BankDetailsPage, mode, triedAnswers))
              }

            case barsResult if !barsResult.sortcodeExists =>
              BadRequest(view(form.fill(value).copy(errors = Seq(FormError("SortCode", "Sort Code does not exist"))), mode))

            case barsResult if !barsResult.validAccountAndSortCode =>
              BadRequest(view(form.fill(value).copy(errors = Seq(FormError("AccountNumber", "Sort Code does not match Account Code"))), mode))

            case barsResult if !barsResult.isValid =>
              BadRequest(view(form.fill(value).copy(errors = Seq(FormError("SortCode", "BARS Assesment failed"))), mode))
          }
      )
  }
}
