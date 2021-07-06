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
import models.ClaimRepaymentType.{Customs, Other, Vat}
import models._
import pages._
import views.behaviours.ViewBehaviours

class CreateNavigatorSpec extends SpecBase with ViewBehaviours {

  val navigator = injector.instanceOf[CreateNavigatorImpl]

  "Navigator" when {

    "in Normal mode" must {

      "go to first question from a page that doesn't exist in the route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, UserAnswers("id", None)) mustBe routes.ClaimantTypeController.onPageLoad()
      }

      "go to IndirectRepresentative after WhomToPay page when the claimant is representative and has selected representative to be paid" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(WhomToPayPage, WhomToPay.Representative).success.value

        navigator.nextPage(WhomToPayPage, answers)
          .mustBe(routes.IndirectRepresentativeController.onPageLoad())
      }

      "go to UK Regulations page after Customs Regulation Type page when selected UKCustomsCodeRegulation and one entry journey " in {
        val answers =
          emptyUserAnswers
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)).success.value
            .set(CustomsRegulationTypePage, CustomsRegulationType.UKCustomsCodeRegulation).success.value

        navigator.nextPage(CustomsRegulationTypePage, answers)
          .mustBe(routes.UkRegulationTypeController.onPageLoad())

      }

      "go to BankDetails page after WhomToPay page when the claimant is representative and has selected importer to be paid" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple, Some("2"))).success.value
            .set(WhomToPayPage, WhomToPay.Importer).success.value

        navigator.nextPage(WhomToPayPage, answers)
          .mustBe(routes.BankDetailsController.onPageLoad())
      }

      "go to BankDetails page after IndirectRepresentative page when the claimant is representative and has selected yes" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(IndirectRepresentativePage, true).success.value

        navigator.nextPage(IndirectRepresentativePage, answers)
          .mustBe(routes.BankDetailsController.onPageLoad())
      }

      "go to proofOfAuthority page after after IndirectRepresentative page when the claimant is representative and has selected no" in {

        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(IndirectRepresentativePage, false).success.value

        navigator.nextPage(IndirectRepresentativePage, answers)
          .mustBe(routes.ProofOfAuthorityController.showFileUpload())
      }

      "go to BankDetails page after the ProofOfAuthority page once the representative has uploaded their proof of authority" in {

        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(IndirectRepresentativePage, true).success.value
        navigator.nextPage(ProofOfAuthorityPage, answers)
          .mustBe(routes.BankDetailsController.onPageLoad())

      }

      "go to ArticleTypeController page after the customsRegulationType page when the UnionsCustomsCodeRegulation has been selected" in {

        val answers =
          emptyUserAnswers
            .set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation).success.value
        navigator.nextPage(CustomsRegulationTypePage, answers)
          .mustBe(routes.ArticleTypeController.onPageLoad())

      }

      "go to UkRegulationType page after the customsRegulationType page when the UKCustomsCodeRegulation has been selected" in {

        val answers =
          emptyUserAnswers
            .set(CustomsRegulationTypePage, CustomsRegulationType.UKCustomsCodeRegulation).success.value
        navigator.nextPage(CustomsRegulationTypePage, answers)
          .mustBe(routes.UkRegulationTypeController.onPageLoad())
      }

      "go to CheckYourAnswers page after the bank details has been entered " in {

        val answers =
          emptyUserAnswers
        navigator.nextPage(BankDetailsPage, answers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad)

      }

      "go to ClaimReasonType page after EntryDetails page " in {
        navigator.nextPage(EntryDetailsPage, emptyUserAnswers)
          .mustBe(routes.ClaimReasonTypeController.onPageLoad())
      }

      "go to ReasonForOverpayment page after ClaimReasonType page " in {
        navigator.nextPage(ClaimReasonTypePage, emptyUserAnswers)
          .mustBe(routes.ReasonForOverpaymentController.onPageLoad())
      }

      "go to DeclarantReferenceNumber page after EmailAddressAndPhoneNumberPage page when the Representative's multiple entry journeys is selected " in {
        val answers =
          emptyUserAnswers
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple, Some("2"))).success.value.set(
              ClaimantTypePage,
              ClaimantType.Representative
            ).success.value
        navigator.nextPage(EmailAddressAndPhoneNumberPage, answers)
          .mustBe(routes.DeclarantReferenceNumberController.onPageLoad())
      }

      "go to DeclarantReferenceNumber page after EmailAddressAndPhoneNumberPage page when Importers/Representative single entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)).success.value
        navigator.nextPage(EmailAddressAndPhoneNumberPage, answers)
          .mustBe(routes.DeclarantReferenceNumberController.onPageLoad())
      }

      "go to RepaymentType page after DeclarantReferenceNumber page when Importers/Representative single entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)).success.value
        navigator.nextPage(DeclarantReferenceNumberPage, answers)
          .mustBe(routes.RepaymentTypeController.onPageLoad())
      }

      "go to EnterAgentEORI page after agentImporterHasEORI with Yes page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(AgentImporterHasEORIPage, AgentImporterHasEORI.Yes).success.value
        navigator.nextPage(AgentImporterHasEORIPage, answers)
          .mustBe(routes.EnterAgentEORIController.onPageLoad())
      }

      "go to IsImporterVatRegistered page after agentImporterHasEORI with No page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(AgentImporterHasEORIPage, AgentImporterHasEORI.No).success.value
        navigator.nextPage(AgentImporterHasEORIPage, answers)
          .mustBe(routes.IsImporterVatRegisteredController.onPageLoad())
      }

      "go to Declarant name page after IsVatRegistered page in importer journey" in {
        val answers =
          emptyUserAnswers
            .set(IsVATRegisteredPage, IsVATRegistered.No).success.value
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
        navigator.nextPage(IsVATRegisteredPage, answers)
          .mustBe(routes.DeclarantNameController.onPageLoad())
      }

      "go to 'Do you own the goods page' after Declarant name page in importer journey" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
            .set(DeclarantNamePage, Name("Joe", "Bloggs")).success.value
        navigator.nextPage(DeclarantNamePage, answers)
          .mustBe(routes.DoYouOwnTheGoodsController.onPageLoad())
      }

      "go to Select address for importer page after 'Do you own the goods page' in importer journey if 'Yes' is selected" in {
        val answers =
          emptyUserAnswers
            .set(DoYouOwnTheGoodsPage, DoYouOwnTheGoods.Yes).success.value
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
        navigator.nextPage(DoYouOwnTheGoodsPage, answers)
          .mustBe(routes.ImporterAddressFrontendController.onPageLoad())
      }

      "go to Importer name page from 'Do you own the goods page' in importer journey if 'No' is selected" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
            .set(DoYouOwnTheGoodsPage, DoYouOwnTheGoods.No).success.value
        navigator.nextPage(DoYouOwnTheGoodsPage, answers)
          .mustBe(routes.ImporterNameController.onPageLoad())
      }

      "go to select address page after entering the importer name details" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
            .set(ImporterNamePage, UserName("Joe Bloggs")).success.value
        navigator.nextPage(ImporterNamePage, answers)
          .mustBe(routes.ImporterAddressFrontendController.onPageLoad())
      }

      "go to PhoneNumber page after importerAddressConfirmation page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value

        navigator.nextPage(AgentImporterAddressPage, answers)
          .mustBe(routes.EmailAddressAndPhoneNumberController.onPageLoad())
      }

      "go to ImporterEori page after importerHasEORI with Yes page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(ImporterHasEoriPage, true).success.value
        navigator.nextPage(ImportHasEoriOnAgentJourneyPage, answers)
          .mustBe(routes.ImporterEoriController.onPageLoad())
      }

      "go to Representative importer name page after IsVatRegistered page when Representative single/multiple entry journeys selected" in {
        val answers =
          emptyUserAnswers
            .set(IsImporterVatRegisteredPage, IsImporterVatRegistered.No).success.value
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
        navigator.nextPage(IsImporterVatRegisteredPage, answers)
          .mustBe(routes.RepresentativeImporterNameController.onPageLoad())
      }

      "go to AgentName page after importerHasEORI with No page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(ImporterHasEoriPage, false).success.value
        navigator.nextPage(ImportHasEoriOnAgentJourneyPage, answers)
          .mustBe(routes.RepresentativeDeclarantAndBusinessNameController.onPageLoad())
      }

      "go to BankDetails page after DeclarantReferenceNumberPage page when the claimant is importer and has selected multiple entries" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple, Some("2"))).success.value
        navigator.nextPage(DeclarantReferenceNumberPage, answers)
          .mustBe(routes.BankDetailsController.onPageLoad())
      }

      "go to PhoneNumber page after ImporterManualAddress page when the claimant is importer" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value

        navigator.nextPage(ImporterAddressPage, answers)
          .mustBe(routes.EmailAddressAndPhoneNumberController.onPageLoad())
      }

      "go to ImporterHasEori page after FileUploadedPage page when the claimant is importer" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value

        navigator.nextPage(FileUploadPage, answers)
          .mustBe(routes.ImporterHasEoriController.onPageLoad())
      }

      "skip ImporterHasEori page after FileUploadedPage page when the claimant is importer and has EORI" in {
        val answers =
          emptyUserAnswersWithEORI
            .set(ClaimantTypePage, ClaimantType.Importer).success.value

        navigator.nextPage(FileUploadPage, answers)
          .mustBe(routes.IsVATRegisteredController.onPageLoad())
      }

      "go to ImporterHasEori page after ImporterManualAddress page when the claimant is representative" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value

        navigator.nextPage(ImporterAddressPage, answers)
          .mustBe(routes.ImporterHasEoriController.onPageLoad())
      }

      "skip ImporterHasEori page after ImporterManualAddress page when the claimant is representative and has EORI" in {
        val answers =
          emptyUserAnswersWithEORI
            .set(ClaimantTypePage, ClaimantType.Representative).success.value

        navigator.nextPage(ImporterAddressPage, answers)
          .mustBe(routes.RepresentativeDeclarantAndBusinessNameController.onPageLoad())
      }

      "go to AgentImporterHasEORIPage page after FileUploadedPage page when the claimant is representative" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value

        navigator.nextPage(FileUploadPage, answers)
          .mustBe(routes.AgentImporterHasEORIController.onPageLoad())
      }

      "go to AgentImporterHasEORIPage page after FileUploadedPage page when the claimant is representative and has EORI" in {
        val answers =
          emptyUserAnswersWithEORI
            .set(ClaimantTypePage, ClaimantType.Representative).success.value

        navigator.nextPage(FileUploadPage, answers)
          .mustBe(routes.AgentImporterHasEORIController.onPageLoad())
      }

      "go to ClaimantType page when CreateOrAmendCase is type of Start a new application" in {
        val answers =
          emptyUserAnswers
            .set(CreateOrAmendCasePage, CreateOrAmendCase.CreateCase).success.value

        navigator.nextPage(CreateOrAmendCasePage, answers)
          .mustBe(routes.ClaimantTypeController.onPageLoad())
      }

      "go to EvidenceSupportingDocs page after the Customs Duty Paid Page with RepayNormalMode" in {
        navigator.nextPage(CustomsDutyPaidPage, UserAnswers("id", None))
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad())
      }

      "go to EvidenceSupportingDocs page after the VAT Paid Page with RepayNormalMode" in {
        navigator.nextPage(VATPaidPage, UserAnswers("id", None))
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad())
      }

      "go to EvidenceSupportingDocs page after the Other Duties Paid Page with RepayNormalMode" in {
        navigator.nextPage(OtherDutiesPaidPage, UserAnswers("id", None))
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad())
      }
    }

    "in Check mode" must {
      def changeAnswers(page: Page) = UserAnswers(userAnswersId, None, changePage = Some(page.toString))

      "go to Repayment Amount Summary page after the Customs Duty Paid page when VAT and Other Duties is not selected" in {
        val values: Seq[ClaimRepaymentType] = Seq(Customs)
        val userAnswers                     = changeAnswers(CustomsDutyPaidPage).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(CustomsDutyPaidPage, userAnswers)
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad())
      }
      "go to Vat Paid page after the Customs Duty Paid page when VAT is selected" in {
        val values: Seq[ClaimRepaymentType] = Seq(Customs, Vat)
        val userAnswers                     = changeAnswers(CustomsDutyPaidPage).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(CustomsDutyPaidPage, userAnswers)
          .mustBe(routes.VATPaidController.onPageLoad())
      }
      "go to Other Duties Paid page after the Customs Duty Paid page when VAT is not selected and Other Duties is selected" in {
        val values: Seq[ClaimRepaymentType] = Seq(Customs, Other)
        val userAnswers                     = changeAnswers(CustomsDutyPaidPage).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(CustomsDutyPaidPage, userAnswers)
          .mustBe(routes.OtherDutiesPaidController.onPageLoad())
      }
      "go to Repayment Amount Summary page after the VAT Paid page when Other Duties is not selected" in {
        val values: Seq[ClaimRepaymentType] = Seq(Vat)
        val userAnswers                     = changeAnswers(VATPaidPage).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(VATPaidPage, userAnswers)
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad())
      }
      "go to Other Duties page after the VAT Paid page when Other Duties is selected" in {
        val values: Seq[ClaimRepaymentType] = Seq(Other)
        val userAnswers                     = changeAnswers(VATPaidPage).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(VATPaidPage, userAnswers)
          .mustBe(routes.OtherDutiesPaidController.onPageLoad())
      }
      "go to Repayment Amount Summary page after the Other Duties Paid page" in {
        navigator.nextPage(OtherDutiesPaidPage, UserAnswers("id", None))
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad())
      }
      "go to Customs Duty Paid page when Customs is selected as a Claim Repayment type" in {
        val values: Seq[ClaimRepaymentType] = Seq(Customs)
        val userAnswers                     = changeAnswers(ClaimRepaymentTypePage).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(ClaimRepaymentTypePage, userAnswers)
          .mustBe(routes.CustomsDutyPaidController.onPageLoad())
      }
      "go to Vat Paid page when Vat is selected as a Claim Repayment type" in {
        val values: Seq[ClaimRepaymentType] = Seq(Vat)
        val userAnswers                     = changeAnswers(ClaimRepaymentTypePage).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(ClaimRepaymentTypePage, userAnswers)
          .mustBe(routes.VATPaidController.onPageLoad())
      }
      "go to Other Duties Paid page when Other is selected as a Claim Repayment type" in {
        val values: Seq[ClaimRepaymentType] = Seq(Other)
        val userAnswers                     = changeAnswers(ClaimRepaymentTypePage).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(ClaimRepaymentTypePage, userAnswers)
          .mustBe(routes.OtherDutiesPaidController.onPageLoad())
      }
      "go to the Indirect Representatives page after the Whom to pay page if the representative is to be paid" in {
        val userAnswers = changeAnswers(WhomToPayPage)
          .set(WhomToPayPage, WhomToPay.Representative).success.value
          .set(ClaimantTypePage, ClaimantType.Representative).success.value

        navigator.nextPage(WhomToPayPage, userAnswers)
          .mustBe(routes.IndirectRepresentativeController.onPageLoad())
      }
      "go to the Check your answers page after the whom to pay page if the importer is to be paid and bank details have already been entered" in {
        val userAnswers = changeAnswers(WhomToPayPage)
          .set(WhomToPayPage, WhomToPay.Importer).success.value
          .set(BankDetailsPage, BankDetails("name", "111111", "11111111")).success.value

        navigator.nextPage(WhomToPayPage, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }
      "go to the Bank Details page after the whom to pay page if the importer is to be paid and bank details have not been entered" in {
        val userAnswers = changeAnswers(WhomToPayPage)
          .set(ClaimantTypePage, ClaimantType.Representative).success.value
          .set(WhomToPayPage, WhomToPay.Importer).success.value

        navigator.nextPage(WhomToPayPage, userAnswers)
          .mustBe(routes.BankDetailsController.onPageLoad())
      }
      "go to Bank Details page when BACs is selected as a repayment method and bank account details are empty" in {
        val userAnswers = changeAnswers(RepaymentTypePage).set(RepaymentTypePage, RepaymentType.BACS).success.value

        navigator.nextPage(RepaymentTypePage, userAnswers)
          .mustBe(routes.BankDetailsController.onPageLoad())
      }
      "go to the Whom is to be paid page after the repayment type page when in agent journey and BACS is selected" in {
        val userAnswers = changeAnswers(RepaymentTypePage)
          .set(ClaimantTypePage, ClaimantType.Representative).success.value
          .set(RepaymentTypePage, RepaymentType.BACS).success.value

        navigator.nextPage(RepaymentTypePage, userAnswers)
          .mustBe(routes.WhomToPayController.onPageLoad())
      }
      "go to the Proof of Authority page after the Indirect Representative page when in agent journey and is not an indirect representative and BACS is selected" in {
        val userAnswers = changeAnswers(IndirectRepresentativePage)
          .set(ClaimantTypePage, ClaimantType.Representative).success.value
          .set(RepaymentTypePage, RepaymentType.BACS).success.value
          .set(IndirectRepresentativePage, false).success.value

        navigator.nextPage(IndirectRepresentativePage, userAnswers)
          .mustBe(routes.ProofOfAuthorityController.showFileUpload())
      }
      "go to the Bank Details page after the Indirect Representative page when in agent journey and is an indirect representative and BACS is selected" in {
        val userAnswers = changeAnswers(IndirectRepresentativePage)
          .set(ClaimantTypePage, ClaimantType.Representative).success.value
          .set(RepaymentTypePage, RepaymentType.BACS).success.value
          .set(IndirectRepresentativePage, true).success.value

        navigator.nextPage(IndirectRepresentativePage, userAnswers)
          .mustBe(routes.BankDetailsController.onPageLoad())
      }
      "go to the Check your answers page after the Declarant Reference page" in {
        val userAnswers = changeAnswers(DeclarantReferenceNumberPage)
          .set(
            DeclarantReferenceNumberPage,
            DeclarantReferenceNumber(DeclarantReferenceType.Yes, Some("this is a reference"))
          ).success.value

        navigator.nextPage(DeclarantReferenceNumberPage, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

    }
  }
}
