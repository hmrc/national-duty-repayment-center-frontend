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
import models.{ClaimRepaymentType, NormalMode}
import pages.ClaimRepaymentTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.RepaymentAmountSummaryAnswersHelper
import views.html.RepaymentAmountSummaryView

class RepaymentAmountSummaryController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: RepaymentAmountSummaryView
                                                ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val helper = new RepaymentAmountSummaryAnswersHelper(request.userAnswers)

      val sections = Seq(
        helper.getSections(),
        Seq(helper.getTotalSection()).filter(_ => helper.getSections().size > 1)
      ).flatten

      val backLink = request.userAnswers.get(ClaimRepaymentTypePage) match {
        case _ if request.userAnswers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Other) => routes.OtherDutiesDueToHMRCController.onPageLoad(NormalMode)
        case _ if request.userAnswers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Vat) => routes.VATDueToHMRCController.onPageLoad(NormalMode)
        case _ if request.userAnswers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Customs) => routes.CustomsDutyDueToHMRCController.onPageLoad(NormalMode)
        case _ => routes.ClaimRepaymentTypeController.onPageLoad(NormalMode)
      }

      Ok(view(sections, backLink))
  }
}
