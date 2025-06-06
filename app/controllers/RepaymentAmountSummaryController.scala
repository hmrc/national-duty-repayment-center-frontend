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

import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import navigation.CreateNavigator
import pages.{Page, RepaymentAmountSummaryPage, RepaymentTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RepaymentAmountSummaryAnswersHelper
import views.html.RepaymentAmountSummaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepaymentAmountSummaryController @Inject() (
  override val messagesApi: MessagesApi,
  val navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  appConfig: FrontendAppConfig,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: RepaymentAmountSummaryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = RepaymentAmountSummaryPage

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val helper = new RepaymentAmountSummaryAnswersHelper(request.userAnswers)

      val sections =
        Seq(helper.getSections(), Seq(helper.getTotalSection()).filter(_ => helper.getSections().size > 1)).flatten

      Ok(view(sections, backLink(request.userAnswers)))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      if (!request.userAnswers.isCmaAllowed(appConfig))
        for {
          updatedAnswers: UserAnswers <- Future.fromTry(request.userAnswers.remove(RepaymentTypePage))
          _                           <- sessionRepository.set(updatedAnswers)

        } yield Redirect(nextPage(updatedAnswers))
      else
        Future.successful(Redirect(nextPage(request.userAnswers)))
  }

}
