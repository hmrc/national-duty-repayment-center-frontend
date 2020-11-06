/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.RepaymentAmountSummaryAnswersHelper
import viewmodels.AnswerSection
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

      var customDutySection = AnswerSection(Some("Customs Duty"), Seq(
        helper.displayCustomsDuty("0", request.userAnswers).get,
        helper.displayCustomsDuty("1", request.userAnswers).get,
        helper.displayCustomsDuty("2", request.userAnswers).get
      ))

      var vatSection = AnswerSection(Some("VAT"), Seq(
        helper.displayVAT("0", request.userAnswers).get,
        helper.displayVAT("1", request.userAnswers).get,
        helper.displayVAT("2", request.userAnswers).get
      ))

      var otherDutiesSection = AnswerSection(Some("Other duties"), Seq(
        helper.displayOtherDuties("0", request.userAnswers).get,
        helper.displayOtherDuties("1", request.userAnswers).get,
        helper.displayOtherDuties("2", request.userAnswers).get
      ))

      var totalSection = AnswerSection(Some("Total"), Seq(
        helper.displayTotal(request.userAnswers).get
      ))

      val sections = Seq(Seq(customDutySection, vatSection, otherDutiesSection, totalSection
      )).flatten

      Ok(view(sections))
  }



}
