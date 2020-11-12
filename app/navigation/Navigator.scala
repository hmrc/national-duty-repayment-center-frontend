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

package navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import pages._
import models._

import scala.collection.mutable

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case ClaimantTypePage => _ => routes.NumberOfEntriesTypeController.onPageLoad(NormalMode)
    case NumberOfEntriesTypePage  => howManyEntries
    case HowManyEntriesPage  => _ => routes.CustomsRegulationTypeController.onPageLoad(NormalMode)
    case CustomsRegulationTypePage => _ => routes.ArticleTypeController.onPageLoad(NormalMode)
    case ArticleTypePage => _ => routes.ClaimEpuController.onPageLoad(NormalMode)
    case ClaimEpuPage => _ => routes.ClaimEntryNumberController.onPageLoad(NormalMode)
    case ClaimEntryNumberPage => _ => routes.ClaimEntryDateController.onPageLoad(NormalMode)
    case ClaimEntryDatePage => _ => routes.ClaimReasonTypeController.onPageLoad(NormalMode)
    case ClaimReasonTypePage => _ => routes.ReasonForOverpaymentController.onPageLoad(NormalMode)
    case ReasonForOverpaymentPage => _ => routes. WhatAreTheGoodsController.onPageLoad(NormalMode)
    case WhatAreTheGoodsPage => _ => routes.ClaimRepaymentTypeController.onPageLoad(NormalMode)
    case ClaimRepaymentTypePage => claimRepaymentType
    case CustomsDutyPaidPage => _ => routes.CustomsDutyDueToHMRCController.onPageLoad(NormalMode)
    case CustomsDutyDueToHMRCPage => _ => routes.VATPaidController.onPageLoad(NormalMode)
    case VATPaidPage => _ => routes.VATDueToHMRCController.onPageLoad(NormalMode)
    case VATDueToHMRCPage => _ => routes.OtherDutiesPaidController.onPageLoad(NormalMode)
    case OtherDutiesPaidPage => _ => routes.OtherDutiesDueToHMRCController.onPageLoad(NormalMode)
    case OtherDutiesDueToHMRCPage => _ => routes.RepaymentAmountSummaryController.onPageLoad

  }

  private def howManyEntries(answers: UserAnswers): Call = answers.get(NumberOfEntriesTypePage) match {
    case Some(NumberOfEntriesType.Single)  => routes.CustomsRegulationTypeController.onPageLoad(NormalMode)
    case Some(NumberOfEntriesType.Multiple) => routes.HowManyEntriesController.onPageLoad(NormalMode)
    case None => routes.SessionExpiredController.onPageLoad()
  }

  private def claimRepaymentType(answers: UserAnswers): Set[Call] = answers.get(ClaimRepaymentTypePage).get.map
  { result =>
      result match {
    case ClaimRepaymentType.Customs  => routes.CustomsDutyPaidController.onPageLoad(NormalMode)
    case ClaimRepaymentType.Vat  => routes.VATPaidController.onPageLoad(NormalMode)
    case ClaimRepaymentType.Other  => routes.OtherDutiesPaidController.onPageLoad(NormalMode)
  } }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
