/*
 * Copyright 2023 HM Revenue & Customs
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
import models.{RepaymentType, UserAnswers}
import navigation.CreateNavigator
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.BankAccountReputationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BankDetailsView

import scala.concurrent.{ExecutionContext, Future}

class BankDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  val navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: BankDetailsFormProvider,
  bankAccountReputationService: BankAccountReputationService,
  val controllerComponents: MessagesControllerComponents,
  view: BankDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = BankDetailsPage
  val form                = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(BankDetailsPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(
        view(
          preparedForm,
          request.userAnswers.get(ClaimantTypePage),
          request.userAnswers.get(WhomToPayPage),
          backLink(request.userAnswers)
        )
      )
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              view(
                formWithErrors,
                request.userAnswers.get(ClaimantTypePage),
                request.userAnswers.get(WhomToPayPage),
                backLink(request.userAnswers)
              )
            )
          ),
        bankDetails =>
          bankAccountReputationService.validate(bankDetails) flatMap { barsResult =>
            formProvider.processBarsResult(barsResult, bankDetails) match {
              case None =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(BankDetailsPage, bankDetails))
                  removeCMA <-
                    Future.fromTry(updatedAnswers.get(RepaymentTypePage) match {
                      case Some(RepaymentType.CMA) =>
                        updatedAnswers.remove(RepaymentTypePage)
                      case _ =>
                        updatedAnswers.set(BankDetailsPage, bankDetails)
                    })
                  _ <- sessionRepository.set(removeCMA)
                } yield Redirect(nextPage(removeCMA))
              case Some(barsErrorForm) =>
                Future.successful(
                  BadRequest(
                    view(
                      barsErrorForm,
                      request.userAnswers.get(ClaimantTypePage),
                      request.userAnswers.get(WhomToPayPage),
                      backLink(request.userAnswers)
                    )
                  )
                )
            }
          }
      )
  }

}
