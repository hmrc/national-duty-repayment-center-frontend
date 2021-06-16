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

import java.time.LocalDate

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import navigation.AmendNavigator
import pages.{AmendCheckYourAnswersPage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{ClaimDateQuery, ClaimIdQuery}
import repositories.SessionRepository
import services.ClaimService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckYourAnswersHelper
import views.html.AmendCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class AmendCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  claimService: ClaimService,
  val navigator: AmendNavigator,
  val controllerComponents: MessagesControllerComponents,
  view: AmendCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = AmendCheckYourAnswersPage

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val updatedAnswers = request.userAnswers.copy(changePage = None)
      sessionRepository.clearChangePage(updatedAnswers) map { _ =>
        navigator.firstMissingAnswer(updatedAnswers) match {
          case Some(call) =>
            // TODO - render "missing" version of CYA page
            Redirect(call)
          case None =>
            val checkYourAnswersHelper = new CheckYourAnswersHelper(updatedAnswers)
            Ok(view(checkYourAnswersHelper.getAmendCheckYourAnswerSections, backLink(updatedAnswers)))
        }
      }
  }

  def onChange(page: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.set(request.userAnswers.copy(changePage = Some(page))) map { _ =>
        Redirect(navigator.gotoPage(page))
      }
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        claimId          <- claimService.submitAmendClaim(request.userAnswers)
        updatedClaimId   <- Future.fromTry(request.userAnswers.set(ClaimIdQuery, claimId))
        updatedClaimDate <- Future.fromTry(updatedClaimId.set(ClaimDateQuery, LocalDate.now))
        _                <- sessionRepository.set(updatedClaimDate)
      } yield Redirect(nextPage(request.userAnswers))
  }

}
