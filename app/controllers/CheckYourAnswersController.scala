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
import models.{Mode, NormalMode, RepaymentType, UserAnswers}
import navigation.Navigator
import pages.{CheckYourAnswersPage, RepaymentTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import queries.{ClaimDateQuery, ClaimIdQuery}
import repositories.SessionRepository
import services.ClaimService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.CheckYourAnswersHelper
import viewmodels.AnswerSection
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            sessionRepository: SessionRepository,
                                            claimService: ClaimService,
                                            navigator: Navigator,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def getBackLink(mode: Mode, userAnswers: UserAnswers): Call = {
    userAnswers.get(RepaymentTypePage).contains(RepaymentType.BACS) match {
      case true=> routes.BankDetailsController.onPageLoad(mode)
      case _ => routes.RepaymentTypeController.onPageLoad(mode)
    }
  }

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val checkYourAnswersHelper = new CheckYourAnswersHelper(request.userAnswers)

      val sections = Seq(checkYourAnswersHelper.getImportantInformationAnswerSection,
            checkYourAnswersHelper.getEntryDetailsAnswerSection,
        checkYourAnswersHelper.getApplicationInformationAnswerSection,
        checkYourAnswersHelper.getYourDetailsAnswerSection,
        checkYourAnswersHelper.getContactDetailsAnswerSection,
      checkYourAnswersHelper.getPaymentInformationAnswerSection)

      Ok(view(sections, getBackLink(NormalMode, request.userAnswers)))
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        claimId <- claimService.submitClaim(request.userAnswers)
        updatedClaimId <- Future.fromTry(request.userAnswers.set(ClaimIdQuery, claimId))
        updatedClaimDate <- Future.fromTry(updatedClaimId.set(ClaimDateQuery, LocalDate.now))
        _ <- sessionRepository.set(updatedClaimDate)
      } yield Redirect(navigator.nextPage(CheckYourAnswersPage, NormalMode, request.userAnswers))
  }
}

