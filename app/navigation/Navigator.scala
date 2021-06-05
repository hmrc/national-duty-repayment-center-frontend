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

import controllers.routes
import javax.inject.{Inject, Singleton}
import models.ClaimantType.Representative
import models.RepaymentType.BACS
import models._
import pages._
import play.api.mvc.Call

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case ArticleTypePage                            => getReasonForRepayment
    case UkRegulationTypePage                       => getReasonForRepayment
    case AgentImporterHasEORIPage                   => getAgentEORIStatus
    case ImporterEoriPage                           => getEORIPage
    case IsImporterVatRegisteredPage                => _ => routes.RepresentativeImporterNameController.onPageLoad()
    case ImporterNamePage                           => _ => routes.ImporterAddressController.onPageLoad()
    case RepresentativeImporterNamePage             => _ => routes.ImporterAddressController.onPageLoad()
    case DeclarantNamePage                          => _ => routes.DoYouOwnTheGoodsController.onPageLoad()
    case DoYouOwnTheGoodsPage                       => doYouOwnTheGoods
    case ImporterManualAddressPage                  => getImporterManualAddress
    case ImporterHasEoriPage                        => getEORIConfirmation
    case IsVATRegisteredPage                        => _ => routes.DeclarantNameController.onPageLoad()
    case EmailAddressAndPhoneNumberPage             => _ => routes.DeclarantReferenceNumberController.onPageLoad()
    case DeclarantReferenceNumberPage               => getRepaymentType
    case RepaymentTypePage                          => getRepaymentMethodType
    case BankDetailsPage                            => _ => routes.CheckYourAnswersController.onPageLoad
    case EnterAgentEORIPage                         => _ => routes.IsImporterVatRegisteredController.onPageLoad()
    case RepresentativeImporterNamePage             => _ => routes.ImporterAddressController.onPageLoad()
    case AgentImporterManualAddressPage             => _ => routes.EmailAddressAndPhoneNumberController.onPageLoad()
    case WhomToPayPage                              => whomToPayRoute
    case IndirectRepresentativePage                 => indirectRepresentativeRoute
    case ProofOfAuthorityPage                       => _ => routes.BankDetailsController.onPageLoad()
    case CheckYourAnswersPage                       => _ => routes.ConfirmationController.onPageLoad()
    case RepresentativeDeclarantAndBusinessNamePage => _ => routes.AgentImporterAddressController.onPageLoad()
    case _                                          => _ => routes.IndexController.onPageLoad()
  }

  private def getImporterManualAddress(answers: UserAnswers): Call = answers.get(ClaimantTypePage) match {
    case Some(ClaimantType.Importer) => routes.EmailAddressAndPhoneNumberController.onPageLoad()
    case _                           => routes.ImporterHasEoriController.onPageLoad()
  }

  private def doYouOwnTheGoods(answers: UserAnswers): Call = answers.get(DoYouOwnTheGoodsPage) match {
    case Some(DoYouOwnTheGoods.Yes) => routes.ImporterAddressController.onPageLoad()
    case _                          => routes.ImporterNameController.onPageLoad()
  }

  private def getRepaymentType(answers: UserAnswers): Call =
    (answers.get(NumberOfEntriesTypePage).get.numberOfEntriesType, answers.get(ClaimantTypePage)) match {
      case (NumberOfEntriesType.Multiple, Some(ClaimantType.Representative)) =>
        routes.WhomToPayController.onPageLoad()
      case (NumberOfEntriesType.Multiple, Some(ClaimantType.Importer)) =>
        routes.BankDetailsController.onPageLoad()
      case _ => routes.RepaymentTypeController.onPageLoad()
    }

  private def getReasonForRepayment(answers: UserAnswers): Call =
    answers.get(NumberOfEntriesTypePage).get.numberOfEntriesType match {
      case NumberOfEntriesType.Multiple => routes.BulkFileUploadController.showFileUpload()
      case NumberOfEntriesType.Single   => routes.EntryDetailsController.onPageLoad()
    }

  private def whomToPayRoute(answers: UserAnswers): Call = answers.get(WhomToPayPage) match {
    case Some(WhomToPay.Importer)       => routes.BankDetailsController.onPageLoad()
    case Some(WhomToPay.Representative) => routes.IndirectRepresentativeController.onPageLoad()
    case None                           => routes.SessionExpiredController.onPageLoad()
  }

  private def indirectRepresentativeRoute(answers: UserAnswers): Call = answers.get(IndirectRepresentativePage) match {
    case Some(true)  => routes.BankDetailsController.onPageLoad()
    case Some(false) => routes.ProofOfAuthorityController.showFileUpload()
    case None        => routes.SessionExpiredController.onPageLoad()
  }

  private def getEORIPage(answers: UserAnswers): Call = answers.get(ClaimantTypePage) match {
    case Some(ClaimantType.Representative) =>
      if (answers.get(RepresentativeImporterNamePage).isEmpty)
        routes.RepresentativeImporterNameController.onPageLoad()
      else routes.RepresentativeDeclarantAndBusinessNameController.onPageLoad()
    case _ => routes.IsVATRegisteredController.onPageLoad()
  }

  private def getAgentEORIStatus(answers: UserAnswers): Call = answers.get(AgentImporterHasEORIPage) match {
    case Some(AgentImporterHasEORI.Yes) => routes.EnterAgentEORIController.onPageLoad()
    case _                              => routes.IsImporterVatRegisteredController.onPageLoad()
  }

  private def getAgentEORIStatusWithCheckMode(answers: UserAnswers): Call =
    answers.get(AgentImporterHasEORIPage) match {
      case Some(AgentImporterHasEORI.Yes) => routes.EnterAgentEORIController.onPageLoad()
      case _                              => routes.IsImporterVatRegisteredController.onPageLoad()
    }

  private def getEORIConfirmation(answers: UserAnswers): Call = answers.get(ImporterHasEoriPage) match {
    case Some(true) => routes.ImporterEoriController.onPageLoad()
    case _ =>
      answers.get(ClaimantTypePage).contains(ClaimantType.Importer) match {
        case true => routes.IsVATRegisteredController.onPageLoad()
        case _    => routes.RepresentativeDeclarantAndBusinessNameController.onPageLoad()
      }
  }

  private def getEORIConfirmationWithCheckMode(answers: UserAnswers): Call = answers.get(ImporterHasEoriPage) match {
    case Some(true) => routes.ImporterEoriController.onPageLoad()
    case _ =>
      answers.get(ClaimantTypePage).contains(ClaimantType.Importer) match {
        case true => routes.IsVATRegisteredController.onPageLoad()
        case _    => routes.ImporterNameController.onPageLoad()
      }
  }

  private def getRepaymentMethodType(answers: UserAnswers): Call =
    (answers.get(RepaymentTypePage), answers.get(ClaimantTypePage)) match {
      case (Some(RepaymentType.BACS), Some(ClaimantType.Representative)) =>
        routes.WhomToPayController.onPageLoad()
      case (Some(RepaymentType.BACS), Some(ClaimantType.Importer)) =>
        routes.BankDetailsController.onPageLoad()
      case _ => routes.CheckYourAnswersController.onPageLoad
    }

  private def getWhomToPayCheckMode(answers: UserAnswers): Call = answers.get(WhomToPayPage) match {
    case Some(WhomToPay.Representative) => routes.IndirectRepresentativeController.onPageLoad()
    case Some(WhomToPay.Importer) =>
      answers.get(BankDetailsPage).isEmpty match {
        case false => routes.CheckYourAnswersController.onPageLoad()
        case true  => routes.BankDetailsController.onPageLoad()
      }
  }

  private def getRepaymentTypeWithCheckMode(answers: UserAnswers): Call =
    (answers.get(RepaymentTypePage), answers.get(ClaimantTypePage)) match {
      case (Some(BACS), Some(Representative))                      => routes.WhomToPayController.onPageLoad()
      case (Some(BACS), _) if answers.get(BankDetailsPage).isEmpty => routes.BankDetailsController.onPageLoad()
      case (Some(RepaymentType.CMA), _)                            => routes.CheckYourAnswersController.onPageLoad()
    }

  private def getIndirectRepresentativeWithCheckMode(answers: UserAnswers): Call =
    answers.get(IndirectRepresentativePage) match {
      case Some(false) => routes.ProofOfAuthorityController.showFileUpload()
      case Some(true) =>
        answers.get(BankDetailsPage).isEmpty match {
          case false => routes.CheckYourAnswersController.onPageLoad()
          case true  => routes.BankDetailsController.onPageLoad()
        }
    }

  private def doYouOwnTheGoodsWithCheckMode(answers: UserAnswers): Call =
    answers.get(DoYouOwnTheGoodsPage).contains(DoYouOwnTheGoods.No) match {
      case true  => routes.ImporterNameController.onPageLoad()
      case false => routes.CheckYourAnswersController.onPageLoad()
    }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case ImporterHasEoriPage        => getEORIConfirmationWithCheckMode
    case AgentImporterHasEORIPage   => getAgentEORIStatusWithCheckMode
    case WhomToPayPage              => getWhomToPayCheckMode
    case IndirectRepresentativePage => getIndirectRepresentativeWithCheckMode
    case RepaymentTypePage          => getRepaymentTypeWithCheckMode
    case DoYouOwnTheGoodsPage       => doYouOwnTheGoodsWithCheckMode
    case _                          => _ => routes.CheckYourAnswersController.onPageLoad()
  }


  def nextPage(page: Page, userAnswers: UserAnswers): Call =
//    mode match {
//      case _ =>
        normalRoutes(page)(userAnswers)
//      case CheckMode =>
//        checkRouteMap(page)(userAnswers)
//      case RepayNormalMode =>
//        repayRouteMap(NormalMode)(page)(userAnswers)
//      case RepayCheckMode =>
//        repayRouteMap(CheckMode)(page)(userAnswers)
//    }

}
