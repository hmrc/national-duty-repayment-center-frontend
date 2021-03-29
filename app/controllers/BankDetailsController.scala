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
import forms.BankDetailsFormProvider

import javax.inject.Inject
import models.{ClaimantType, Mode, RepaymentType, UserAnswers, WhomToPay}
import navigation.Navigator
import pages.{BankDetailsPage, ClaimantTypePage, IndirectRepresentativePage, RepaymentTypePage, WhomToPayPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.BankDetailsView

import scala.concurrent.{ExecutionContext, Future}

class BankDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: BankDetailsFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: BankDetailsView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  private def getBackLink(mode: Mode, userAnswers: UserAnswers): Call = {
    val isRepresentative = userAnswers.get(ClaimantTypePage).contains(ClaimantType.Representative)

    userAnswers.get(BankDetailsPage) match {
      case _ if isRepresentative && userAnswers.get(WhomToPayPage).contains(WhomToPay.Importer) => routes.WhomToPayController.onPageLoad(mode)
      case _ if isRepresentative && userAnswers.get(IndirectRepresentativePage).contains(true) => routes.IndirectRepresentativeController.onPageLoad(mode)
      case _ if isRepresentative && userAnswers.get(IndirectRepresentativePage).contains(false) => routes.ProofOfAuthorityController.showFileUpload(mode)
      case _ => routes.RepaymentTypeController.onPageLoad(mode)
    }
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(BankDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, getBackLink(mode, request.userAnswers)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, getBackLink(mode, request.userAnswers)))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(BankDetailsPage, value))
            removeCMA <-
              Future.fromTry(updatedAnswers.get(RepaymentTypePage) match {
                case Some(RepaymentType.CMA) =>
                  updatedAnswers.remove(RepaymentTypePage)
                case _ =>
                  updatedAnswers.set(BankDetailsPage, value)
              })
            _              <- sessionRepository.set(removeCMA)
          } yield Redirect(navigator.nextPage(BankDetailsPage, mode, removeCMA))
      )
  }
}
