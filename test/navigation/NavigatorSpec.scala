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

import base.SpecBase
import controllers.routes
import models.AmendCaseResponseType.FurtherInformation
import models.ClaimRepaymentType.{Customs, Other, Vat}
import models._
import pages._
import views.behaviours.ViewBehaviours


class NavigatorSpec extends SpecBase with ViewBehaviours {

  val navigator = new Navigator


  "Navigator" when {

    "in Normal mode" must {

      "go to Index from a page that doesn't exist in the route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "go to IndirectRepresentative after WhomToPay page when the claimant is representative and has selected representative to be paid" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(WhomToPayPage, WhomToPay.Representative).success.value

        navigator.nextPage(WhomToPayPage, NormalMode, answers)
          .mustBe(routes.IndirectRepresentativeController.onPageLoad(NormalMode))
      }

      "go to UK Regulations page after Customs Regulation Type page when selected UKCustomsCodeRegulation and one entry journey " in {
        val answers =
          emptyUserAnswers
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single,None)).success.value
            .set(CustomsRegulationTypePage, CustomsRegulationType.UKCustomsCodeRegulation).success.value

        navigator.nextPage(CustomsRegulationTypePage, NormalMode, answers)
          .mustBe(routes.UkRegulationTypeController.onPageLoad(NormalMode))

      }

      "go to BankDetails page after WhomToPay page when the claimant is representative and has selected importer to be paid" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple,Some("2"))).success.value
            .set(WhomToPayPage, WhomToPay.Importer).success.value

        navigator.nextPage(WhomToPayPage, NormalMode, answers)
          .mustBe(routes.BankDetailsController.onPageLoad(NormalMode))
      }

      "go to BankDetails page after IndirectRepresentative page when the claimant is representative and has selected yes" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(IndirectRepresentativePage, true).success.value

        navigator.nextPage(IndirectRepresentativePage, NormalMode, answers)
          .mustBe(routes.BankDetailsController.onPageLoad(NormalMode))
      }

      "go to proofOfAuthority page after after IndirectRepresentative page when the claimant is representative and has selected no" in {

        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(IndirectRepresentativePage, false).success.value

        navigator.nextPage(IndirectRepresentativePage, NormalMode, answers)
          .mustBe(routes.ProofOfAuthorityController.showFileUpload())
      }

      "go to BankDetails page after the ProofOfAuthority page once the representative has uploaded their proof of authority" in {

        val answers =
          emptyUserAnswers
        navigator.nextPage(ProofOfAuthorityPage, NormalMode, answers)
          .mustBe(routes.BankDetailsController.onPageLoad(NormalMode))

      }

      "go to ArticleTypeController page after the customsRegulationType page when the UnionsCustomsCodeRegulation has been selected" in {

        val answers =
          emptyUserAnswers
            .set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation).success.value
        navigator.nextPage(CustomsRegulationTypePage, NormalMode, answers)
          .mustBe(routes.ArticleTypeController.onPageLoad(NormalMode))

      }

      "go to UkRegulationType page after the customsRegulationType page when the UKCustomsCodeRegulation has been selected" in {

        val answers =
          emptyUserAnswers
            .set(CustomsRegulationTypePage, CustomsRegulationType.UKCustomsCodeRegulation).success.value
        navigator.nextPage(CustomsRegulationTypePage, NormalMode, answers)
          .mustBe(routes.UkRegulationTypeController.onPageLoad(NormalMode))

      }



      "go to CheckYourAnswers page after the bank details has been entered " in {

        val answers =
          emptyUserAnswers
        navigator.nextPage(BankDetailsPage, NormalMode, answers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad)

      }

      "go to ClaimReasonType page after EntryDetails page " in {
        navigator.nextPage(EntryDetailsPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.ClaimReasonTypeController.onPageLoad(NormalMode))
      }

      "go to ReasonForOverpayment page after ClaimReasonType page " in {
        navigator.nextPage(ClaimReasonTypePage, NormalMode, emptyUserAnswers)
          .mustBe(routes.ReasonForOverpaymentController.onPageLoad(NormalMode))
      }

      "go to WhomToPay page after EmailAddressAndPhoneNumberPage page when the Representative's multiple entry journeys is selected " in {
        val answers =
          emptyUserAnswers
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple,Some("2"))).success.value.
            set(ClaimantTypePage, ClaimantType.Representative).success.value
        navigator.nextPage(EmailAddressAndPhoneNumberPage, NormalMode, answers)
          .mustBe(routes.WhomToPayController.onPageLoad(NormalMode))
      }

      "go to RepaymentType page after EmailAddressAndPhoneNumberPage page when Importers/Representative single entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single,None)).success.value
        navigator.nextPage(EmailAddressAndPhoneNumberPage, NormalMode, answers)
          .mustBe(routes.RepaymentTypeController.onPageLoad(NormalMode))
      }

      "go to EnterAgentEORI page after agentImporterHasEORI with Yes page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(AgentImporterHasEORIPage, AgentImporterHasEORI.Yes).success.value
        navigator.nextPage(AgentImporterHasEORIPage, NormalMode, answers)
          .mustBe(routes.EnterAgentEORIController.onPageLoad(NormalMode))
      }

      "go to IsImporterVatRegistered page after agentImporterHasEORI with No page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(AgentImporterHasEORIPage, AgentImporterHasEORI.No).success.value
        navigator.nextPage(AgentImporterHasEORIPage, NormalMode, answers)
          .mustBe(routes.IsImporterVatRegisteredController.onPageLoad(NormalMode))
      }

      "go to PhoneNumber page after importerAddressConfirmation page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value

        navigator.nextPage(AgentImporterManualAddressPage, NormalMode, answers)
          .mustBe(routes.PhoneNumberController.onPageLoad(NormalMode))
      }

      "go to ImporterEori page after importerHasEORI with Yes page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ImporterHasEoriPage, true).success.value
        navigator.nextPage(ImporterHasEoriPage, NormalMode, answers)
          .mustBe(routes.ImporterEoriController.onPageLoad(NormalMode))
      }

      "go to ImporterName page after importerHasEORI with No page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ImporterHasEoriPage, false).success.value
        navigator.nextPage(ImporterHasEoriPage, NormalMode, answers)
          .mustBe(routes.ImporterNameController.onPageLoad(NormalMode))
      }

      "go to AmendCaseResponseType page after ReferenceNumber page " in {
        navigator.nextPage(ReferenceNumberPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.AmendCaseResponseTypeController.onPageLoad(NormalMode))
      }

      "go to FurtherInformation page after ReferenceType page as Further Information " in {
        val values: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.FurtherInformation)
        val answers =
          emptyUserAnswers
            .set(AmendCaseResponseTypePage, values).success.value
        navigator.nextPage(AmendCaseResponseTypePage, NormalMode, answers)
          .mustBe(routes.FurtherInformationController.onPageLoad(NormalMode))
      }

      "go to SupportingDocuments page after ReferenceType page as Supporting documents " in {
        val values: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.SupportingDocuments)
        val answers =
          emptyUserAnswers
            .set(AmendCaseResponseTypePage, values).success.value
        navigator.nextPage(AmendCaseResponseTypePage, NormalMode, answers)
          .mustBe(routes.AmendCaseSendInformationController.showFileUpload(NormalMode))
      }

      "go to AmendCheckYourAnswers page after FurtherInformation page " in {
        navigator.nextPage(FurtherInformationPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.AmendCheckYourAnswersController.onPageLoad)
      }

      "go to AmendConfirmationAnswers page after AmendCheckYourAnswersPage page " in {
        navigator.nextPage(AmendCheckYourAnswersPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.AmendConfirmationController.onPageLoad)
      }

      "go to BankDetails page after EmailAddress page when the claimant is importer and has selected multiple entries" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple,Some("2"))).success.value

        navigator.nextPage(EmailAddressAndPhoneNumberPage, NormalMode, answers)
          .mustBe(routes.BankDetailsController.onPageLoad(NormalMode))
      }

      "go to PhoneNumber page after ImporterManualAddress page when the claimant is importer" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value

        navigator.nextPage(ImporterManualAddressPage, NormalMode, answers)
          .mustBe(routes.PhoneNumberController.onPageLoad(NormalMode))
      }

      "go to ImporterHasEori page after ImporterManualAddress page when the claimant is representative" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value

        navigator.nextPage(ImporterManualAddressPage, NormalMode, answers)
          .mustBe(routes.ImporterHasEoriController.onPageLoad(NormalMode))
      }
    }


    "in Check mode" must {
      "go to CheckYourAnswers from a page that doesn't exist in the edit route map" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.AmendCheckYourAnswersController.onPageLoad()
      }
      "go to File upload page" in {
        val userAnswers = UserAnswers(userAnswersId).set(AmendCaseResponseTypePage, AmendCaseResponseType.values.toSet).success.value
        navigator.nextPage(AmendCaseResponseTypePage, CheckMode, userAnswers)
          .mustBe(routes.AmendCaseSendInformationController.showFileUpload(CheckMode))
      }
      "go to Further Information page" in {
        val values: Seq[AmendCaseResponseType] = Seq(FurtherInformation)
        val userAnswers = UserAnswers(userAnswersId).set(AmendCaseResponseTypePage, values.toSet).success.value
        navigator.nextPage(AmendCaseResponseTypePage, CheckMode, userAnswers)
          .mustBe(routes.FurtherInformationController.onPageLoad(CheckMode))
      }
      "go to Check your answers page when Further information is added no Documents selected" in {
        val values: Seq[AmendCaseResponseType] = Seq(FurtherInformation)
        val userAnswers = UserAnswers(userAnswersId).set(AmendCaseResponseTypePage, values.toSet).success.value
        navigator.nextPage(FurtherInformationPage, CheckMode, userAnswers)
          .mustBe(routes.AmendCheckYourAnswersController.onPageLoad())
      }
      "go to Repayment Amount Summary page after the Customs Duty Paid page" in {
        navigator.nextPage(CustomsDutyPaidPage, CheckMode, UserAnswers("id")) mustBe routes.RepaymentAmountSummaryController.onPageLoad()
      }
      "go to Repayment Amount Summary page after the VAT Paid page" in {
        navigator.nextPage(VATPaidPage, CheckMode, UserAnswers("id")) mustBe routes.RepaymentAmountSummaryController.onPageLoad()
      }
      "go to Repayment Amount Summary page after the Other Duties Paid page" in {
        navigator.nextPage(OtherDutiesPaidPage, CheckMode, UserAnswers("id")) mustBe routes.RepaymentAmountSummaryController.onPageLoad()
      }
      "go to Customs Duty Paid page when Customs is selected as a Claim Repayment type" in {
        val values: Seq[ClaimRepaymentType] = Seq(Customs)
        val userAnswers = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, values.toSet).success.value
        navigator.nextPage(ClaimRepaymentTypePage, CheckMode, userAnswers)
          .mustBe(routes.CustomsDutyPaidController.onPageLoad(NormalMode))
      }
      "go to Vat Paid page when Vat is selected as a Claim Repayment type" in {
        val values: Seq[ClaimRepaymentType] = Seq(Vat)
        val userAnswers = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, values.toSet).success.value
        navigator.nextPage(ClaimRepaymentTypePage, CheckMode, userAnswers)
          .mustBe(routes.VATPaidController.onPageLoad(NormalMode))
      }
      "go to Other Duties Paid page when Other is selected as a Claim Repayment type" in {
        val values: Seq[ClaimRepaymentType] = Seq(Other)
        val userAnswers = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, values.toSet).success.value
        navigator.nextPage(ClaimRepaymentTypePage, CheckMode, userAnswers)
          .mustBe(routes.OtherDutiesPaidController.onPageLoad(NormalMode))
      }
    }
  }
}
