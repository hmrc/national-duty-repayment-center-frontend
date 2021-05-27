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

        value => {
          val triedAnswers = request.userAnswers.set(BankDetailsPage, value).get
          sessionRepository.set(triedAnswers)
          Future(Redirect(navigator.nextPage(BankDetailsPage, mode, triedAnswers)))
        }









        //          for {
        //            updatedAnswers <- Future.fromTry(request.userAnswers.set(BankDetailsPage, value))
        //            removeCMA <-
        //              Future.fromTry(updatedAnswers.get(RepaymentTypePage) match {
        //                case Some(RepaymentType.CMA) =>
        //                  updatedAnswers.remove(RepaymentTypePage)
        //                case _ =>
        //                  updatedAnswers.set(BankDetailsPage, value)
        //              })
        //            _              <- sessionRepository.set(removeCMA)
        //          } yield Redirect(navigator.nextPage(BankDetailsPage, mode, removeCMA))
      )
  }

  private def processBarsFailure(bankDetails: BankDetails, barsResult: BARSResult) = {

    val formWithErrors = form.fill(bankDetails).copy(errors = barsResult match {

      case bars if !bars.sortcodeExists =>
        Seq(FormError("sortCode", "bankDetails.bars.validation.sortcodeNotFound"))

      case bars if !bars.validAccountAndSortCode =>
        Seq(FormError("accountNumber", "bankDetails.bars.validation.modCheckFailed"))

      case bars if !bars.sortcodeAcceptsDirectCredit =>
        Seq(FormError("sortCode", "bankDetails.bars.validation.bacsNotSupported"))

      case bars if !bars.rollNotRequired => Seq(FormError("sortCode", "bankDetails.bars.validation.rollRequired"))

      case bars if !bars.accountValid => Seq(FormError("accountNumber", "bankDetails.bars.validation.accountInvalid"))

      case bars if !bars.companyNameValid =>
        Seq(FormError("accountName", "bankDetails.bars.validation.companyNameInvalid"))

      case _ => Seq(FormError("", "bankDetails.bars.validation.failed"))
    })
  }
}
