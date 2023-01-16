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

import java.time.LocalDateTime
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import navigation.AmendNavigator
import pages.{AmendCheckYourAnswersPage, Page}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{AmendClaimDateQuery, AmendClaimIdQuery}
import repositories.SessionRepository
import services.ClaimService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckYourAnswersHelperFactory
import views.html.{AmendCheckYourAnswersView, AmendCheckYourMissingAnswersView}

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
  view: AmendCheckYourAnswersView,
  viewMissing: AmendCheckYourMissingAnswersView,
  cyaFactory: CheckYourAnswersHelperFactory
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] with Logging {

  override val page: Page = AmendCheckYourAnswersPage

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      if (request.userAnswers.isAmendSubmitted)
        sessionRepository.resetData(request.userAnswers) map { _ =>
          Redirect(controllers.routes.IndexController.onPageLoad())
        }
      else {
        val updatedAnswers         = request.userAnswers.copy(changePage = None)
        val checkYourAnswersHelper = cyaFactory.instance(updatedAnswers)
        navigator.firstMissingAnswer(updatedAnswers) match {
          case Some(_) =>
            Future.successful(
              Ok(viewMissing(checkYourAnswersHelper.getAmendCheckYourAnswerSections, backLink(updatedAnswers)))
            )
          case None =>
            sessionRepository.set(updatedAnswers) map { _ =>
              Ok(view(checkYourAnswersHelper.getAmendCheckYourAnswerSections, backLink(updatedAnswers)))
            }
        }
      }
  }

  def onResolve(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      navigator.firstMissingAnswer(request.userAnswers) match {
        case Some(call) => Redirect(call)
        case None       => Redirect(controllers.routes.AmendCheckYourAnswersController.onPageLoad())
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
      claimService.submitAmendClaim(request.userAnswers) flatMap {
        case response if response.isSuccess =>
          for {
            updatedClaimId   <- Future.fromTry(request.userAnswers.set(AmendClaimIdQuery, response.caseId.get))
            updatedClaimDate <- Future.fromTry(updatedClaimId.set(AmendClaimDateQuery, LocalDateTime.now))
            _                <- sessionRepository.set(updatedClaimDate)
          } yield Redirect(nextPage(request.userAnswers))
        case response if response.isNotFound =>
          Future.successful(Redirect(controllers.routes.AmendErrorController.onNotFound()))
        case response if response.isCaseClosed =>
          Future.successful(Redirect(controllers.routes.AmendErrorController.onClosed()))
        case _ =>
          val message = "Amend claim submission failed"
          logger.warn(message)
          throw new RuntimeException(message)
      }
  }

}
