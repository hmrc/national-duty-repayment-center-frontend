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
    case ClaimRepaymentTypePage => getClaimRepaymentType
    case CustomsDutyPaidPage => _ => routes.CustomsDutyDueToHMRCController.onPageLoad(NormalMode)
    case CustomsDutyDueToHMRCPage => getVATRepaymentType
    case VATPaidPage => _ => routes.VATDueToHMRCController.onPageLoad(NormalMode)
    case VATDueToHMRCPage => getOtherRepaymentType
    case OtherDutiesPaidPage => _ => routes.OtherDutiesDueToHMRCController.onPageLoad(NormalMode)
    case OtherDutiesDueToHMRCPage => _ => routes.RepaymentAmountSummaryController.onPageLoad
    //case AgentImporterHasEORIPage => getEORIStatus
    case ImporterEoriPage => _ => routes.IsVatRegisteredController.onPageLoad(NormalMode)
    case IsImporterVatRegisteredPage => _ => routes.ImporterNameController.onPageLoad(NormalMode)
    case ImporterNamePage => _ => routes.ImporterAddressController.onPageLoad(NormalMode)
    case ImporterAddressPage => _ => routes.ImporterAddressConfirmationController.onPageLoad
    case ImporterManualAddressPage => _ => routes.PhoneNumberController.onPageLoad(NormalMode)
    case ImporterHasEoriPage => getEORIConfirmation
    case IsVatRegisteredPage => _ => routes.ImporterNameController.onPageLoad(NormalMode)
    case PhoneNumberPage => _ => routes.RepaymentTypeController.onPageLoad(NormalMode)
    case RepaymentTypePage => getRepaymentMethodType
    case BankDetailsPage => _ => routes.CheckYourAnswersController.onPageLoad
  }

  private def getEORIStatus(answers: UserAnswers): Call = answers.get(AgentImporterHasEORIPage) match {
    case Some(AgentImporterHasEORI.Yes)  => routes.ImporterEoriController.onPageLoad(NormalMode)
    case _ => routes.IsVatRegisteredController.onPageLoad(NormalMode)
  }

  private def getEORIConfirmation(answers: UserAnswers): Call = answers.get(ImporterHasEoriPage) match {
    case Some(true)  => routes.ImporterEoriController.onPageLoad(NormalMode)
    case _ => routes.IsVatRegisteredController.onPageLoad(NormalMode)
  }

  private def getRepaymentMethodType(answers: UserAnswers): Call = answers.get(RepaymentTypePage) match {
    case Some(RepaymentType.BACS)  => routes.BankDetailsController.onPageLoad(NormalMode)
    case _ => routes.CheckYourAnswersController.onPageLoad
  }

  private def howManyEntries(answers: UserAnswers): Call = answers.get(NumberOfEntriesTypePage) match {
    case Some(NumberOfEntriesType.Single)  => routes.CustomsRegulationTypeController.onPageLoad(NormalMode)
    case Some(NumberOfEntriesType.Multiple) => routes.HowManyEntriesController.onPageLoad(NormalMode)
    case None => routes.SessionExpiredController.onPageLoad()
  }

  private def getClaimRepaymentType(answers: UserAnswers): Call = answers.get(ClaimRepaymentTypePage) match {
      case x if (answers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Customs))  => routes.CustomsDutyPaidController.onPageLoad(NormalMode)
      case x if (answers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Vat)) => routes.VATPaidController.onPageLoad(NormalMode)
      case x if (answers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Other))  => routes.OtherDutiesPaidController.onPageLoad(NormalMode)
  }

  private def getVATRepaymentType(answers: UserAnswers): Call = answers.get(ClaimRepaymentTypePage) match {
    case x if (answers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Vat))  => routes.VATPaidController.onPageLoad(NormalMode)
    case x if (answers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Other))  => routes.OtherDutiesPaidController.onPageLoad(NormalMode)
    case _ => routes.RepaymentAmountSummaryController.onPageLoad
  }

  private def getOtherRepaymentType(answers: UserAnswers): Call = answers.get(ClaimRepaymentTypePage) match {
    case x if (answers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Other))  => routes.OtherDutiesPaidController.onPageLoad(NormalMode)
    case _ => routes.RepaymentAmountSummaryController.onPageLoad
  }

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
