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

import java.time.LocalDateTime

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import navigation.CreateNavigator
import pages.{CheckYourAnswersPage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{ClaimDateQuery, ClaimIdQuery}
import repositories.SessionRepository
import services.ClaimService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckYourAnswersHelper
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  claimService: ClaimService,
  val navigator: CreateNavigator,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = CheckYourAnswersPage

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      if (request.userAnswers.isCreateSubmitted)
        sessionRepository.resetData(request.userAnswers) map { _ =>
          Redirect(controllers.routes.IndexController.onPageLoad())
        }
      else {
        val updatedAnswers = request.userAnswers.copy(changePage = None)
        navigator.firstMissingAnswer(updatedAnswers) match {
          case Some(call) =>
            // TODO - render "missing" version of CYA page
            Future.successful(Redirect(call))
          case None =>
            sessionRepository.set(updatedAnswers) map { _ =>
              val checkYourAnswersHelper = new CheckYourAnswersHelper(updatedAnswers)
              Ok(view(checkYourAnswersHelper.getCheckYourAnswerSections, backLink(updatedAnswers)))
            }
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
        claimId          <- claimService.submitClaim(request.userAnswers)
        updatedClaimId   <- Future.fromTry(request.userAnswers.set(ClaimIdQuery, claimId))
        updatedClaimDate <- Future.fromTry(updatedClaimId.set(ClaimDateQuery, LocalDateTime.now))
        _                <- sessionRepository.set(updatedClaimDate)
      } yield Redirect(nextPage(request.userAnswers))
  }

}
