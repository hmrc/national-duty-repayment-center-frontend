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
import javax.inject.Inject
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.ClaimIdQuery
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckYourAnswersHelper
import views.html.{ClaimSummaryView, ConfirmationView}

import scala.concurrent.ExecutionContext

class ConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  confirmationView: ConfirmationView,
  reviewView: ClaimSummaryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(ClaimIdQuery) match {
        case Some(claimId) =>
          val checkYourAnswersHelper = new CheckYourAnswersHelper(request.userAnswers)
          Ok(confirmationView(claimId, checkYourAnswersHelper.getCreateConfirmationSections))
        case None => Redirect(controllers.routes.IndexController.onPageLoad())
      }
  }

  def onSummary: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(ClaimIdQuery) match {
        case Some(_) =>
          val checkYourAnswersHelper = new CheckYourAnswersHelper(request.userAnswers)
          Ok(reviewView(checkYourAnswersHelper.getCreateConfirmationSections, "confirmation.summary.title"))
        case None => Redirect(controllers.routes.IndexController.onPageLoad())
      }
  }

  def onStartNewApplication: Action[AnyContent] = identify.async {
    implicit request =>
      sessionRepository.set(UserAnswers(request.identification)) map { _ =>
        Redirect(controllers.routes.IndexController.onPageLoad())
      }
  }

}
