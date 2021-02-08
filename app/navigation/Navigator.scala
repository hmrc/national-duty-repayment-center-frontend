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

package navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import pages._
import models._

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case ClaimantTypePage => _ => routes.NumberOfEntriesTypeController.onPageLoad(NormalMode)
    case NumberOfEntriesTypePage  => howManyEntries
    case HowManyEntriesPage  => _ => routes.CustomsRegulationTypeController.onPageLoad(NormalMode)
    case CustomsRegulationTypePage => getEntryDetails
    case ArticleTypePage => _ => routes.EntryDetailsController.onPageLoad(NormalMode)
    case EntryDetailsPage => _ => routes.ClaimReasonTypeController.onPageLoad(NormalMode)
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
    case AgentImporterHasEORIPage => getAgentEORIStatus
    case ImporterEoriPage => getEORIPage
    case IsImporterVatRegisteredPage => _ => routes.AgentNameImporterController.onPageLoad(NormalMode)
    case ImporterNamePage => getImporterName
    case ImporterManualAddressPage => getImporterManualAddress
    case ImporterHasEoriPage => getEORIConfirmation
    case IsVATRegisteredPage => _ => routes.ImporterNameController.onPageLoad(NormalMode)
    case PhoneNumberPage => _ => routes.EmailAddressController.onPageLoad(NormalMode)
    case EmailAddressPage => getRepaymentType
    case RepaymentTypePage => getRepaymentMethodType
    case BankDetailsPage => _ => routes.CheckYourAnswersController.onPageLoad
    case EnterAgentEORIPage => _ => routes.IsImporterVatRegisteredController.onPageLoad(NormalMode)
    case AgentNameImporterPage => _ => routes.ImporterAddressController.onPageLoad(NormalMode)
    case AgentImporterManualAddressPage => _ => routes.PhoneNumberController.onPageLoad(NormalMode)
    case WhomToPayPage => whomToPayRoute
    case IndirectRepresentativePage => indirectRepresentativeRoute
    case ProofOfAuthorityPage => _ => routes.BankDetailsController.onPageLoad(NormalMode)
    case BulkFileUploadPage => getBulkEntryDetails
    case CheckYourAnswersPage => _ => routes.ConfirmationController.onPageLoad()
    case ReferenceNumberPage => _ => routes.AmendCaseResponseTypeController.onPageLoad(NormalMode)
    case AmendCaseResponseTypePage => getAmendCaseResponseType
    case AmendCaseSendInformationPage => _ => routes.AmendCaseUploadAnotherFileController.onPageLoad(NormalMode)
    case AmendCaseUploadAnotherFilePage => getAmendCaseUploadAnotherFile
    case FurtherInformationPage => _ => routes.AmendCheckYourAnswersController.onPageLoad
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private def getImporterManualAddress(answers: UserAnswers): Call = answers.get(ClaimantTypePage) match {
    case Some(ClaimantType.Importer)  => routes.PhoneNumberController.onPageLoad(NormalMode)
    case _ => routes.ImporterHasEoriController.onPageLoad(NormalMode)
  }

  private def getImporterName(answers: UserAnswers): Call = answers.get(ClaimantTypePage) match {
    case Some(ClaimantType.Representative)  => routes.AgentImporterAddressController.onPageLoad(NormalMode)
    case _ => routes.ImporterAddressController.onPageLoad(NormalMode)
  }

  private def getAmendCaseUploadAnotherFile(answers: UserAnswers): Call = answers.get(AmendCaseUploadAnotherFilePage) match {
    case Some(AmendCaseUploadAnotherFile.Yes)  => routes.AmendCaseSendInformationController.onPageLoad(NormalMode)
    case Some(AmendCaseUploadAnotherFile.No) => {
      answers.get(AmendCaseResponseTypePage).get.contains(AmendCaseResponseType.Furtherinformation) match {
        case true => routes.FurtherInformationController.onPageLoad(NormalMode)
        case _ => routes.AmendCheckYourAnswersController.onPageLoad
      }
    }
  }

  private def getAmendCaseResponseType(answers: UserAnswers): Call =
    answers.get(AmendCaseResponseTypePage).get.contains(AmendCaseResponseType.Supportingdocuments) match {
      case true => routes.AmendCaseSendInformationController.onPageLoad(NormalMode)
      case  _  => routes.FurtherInformationController.onPageLoad(NormalMode)
    }

  private def getRepaymentType(answers: UserAnswers): Call =
    (answers.get(NumberOfEntriesTypePage), answers.get(ClaimantTypePage)) match {
      case (Some(NumberOfEntriesType.Multiple),Some(ClaimantType.Representative)) => routes.WhomToPayController.onPageLoad(NormalMode)
      case (Some(NumberOfEntriesType.Multiple),Some(ClaimantType.Importer)) => routes.BankDetailsController.onPageLoad(NormalMode)
      case  _  => routes.RepaymentTypeController.onPageLoad(NormalMode)
    }

  private def getBulkEntryDetails(answers: UserAnswers): Call = answers.get(CustomsRegulationTypePage) match {
    case Some(CustomsRegulationType.UnionsCustomsCodeRegulation)  => routes.ArticleTypeController.onPageLoad(NormalMode)
    case _ => routes.EntryDetailsController.onPageLoad(NormalMode)
  }

  private def getEntryDetails(answers: UserAnswers): Call = answers.get(CustomsRegulationTypePage) match {
    case Some(CustomsRegulationType.UnionsCustomsCodeRegulation)  => {
      answers.get(NumberOfEntriesTypePage).contains(NumberOfEntriesType.Single) match {
        case true => routes.ArticleTypeController.onPageLoad (NormalMode)
        case _=> routes.BulkFileUploadController.showFileUpload
      }
    }
    case _ => {
      answers.get(NumberOfEntriesTypePage).contains(NumberOfEntriesType.Single) match {
        case true => routes.EntryDetailsController.onPageLoad (NormalMode)
        case _=> routes.BulkFileUploadController.showFileUpload
      }
    }
  }

  private def whomToPayRoute(answers: UserAnswers): Call = answers.get(WhomToPayPage) match {
    case Some(WhomToPay.Importer) => routes.BankDetailsController.onPageLoad(NormalMode)
    case Some(WhomToPay.Representative) => routes.IndirectRepresentativeController.onPageLoad(NormalMode)
    case None => routes.SessionExpiredController.onPageLoad()
  }

  private def indirectRepresentativeRoute(answers: UserAnswers): Call = answers.get(IndirectRepresentativePage) match {
    case Some(true)  => routes.BankDetailsController.onPageLoad(NormalMode)
    case Some(false) => routes.ProofOfAuthorityController.onPageLoad()
    case None        => routes.SessionExpiredController.onPageLoad()
  }

  private def getEORIPage(answers: UserAnswers): Call = answers.get(ClaimantTypePage) match {
    case Some(ClaimantType.Representative)  => routes.ImporterNameController.onPageLoad(NormalMode)
    case _ => routes.IsVATRegisteredController.onPageLoad(NormalMode)
  }

  private def getAgentEORIStatus(answers: UserAnswers): Call = answers.get(AgentImporterHasEORIPage) match {
    case Some(AgentImporterHasEORI.Yes)  => routes.EnterAgentEORIController.onPageLoad(NormalMode)
    case _ => routes.IsImporterVatRegisteredController.onPageLoad(NormalMode)
  }

  private def getEORIConfirmation(answers: UserAnswers): Call = answers.get(ImporterHasEoriPage) match {
    case Some(true)  => routes.ImporterEoriController.onPageLoad(NormalMode)
    case _ => {
      answers.get(ClaimantTypePage).contains(ClaimantType.Importer) match {
        case true => routes.IsVATRegisteredController.onPageLoad(NormalMode)
        case _ => routes.ImporterNameController.onPageLoad(NormalMode)
      }
    }
  }

  private def getRepaymentMethodType(answers: UserAnswers): Call =
    (answers.get(RepaymentTypePage), answers.get(ClaimantTypePage)) match {
      case (Some(RepaymentType.BACS),Some(ClaimantType.Representative)) => routes.WhomToPayController.onPageLoad(NormalMode)
      case (Some(RepaymentType.BACS), Some(ClaimantType.Importer)) => routes.BankDetailsController.onPageLoad(NormalMode)
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

  private def getOtherRepaymentType(answers: UserAnswers): Call = answers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Other) match {
    case true => routes.OtherDutiesPaidController.onPageLoad(NormalMode)
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
