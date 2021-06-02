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
import forms.EmailAndPhoneNumber
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
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)).success.value
            .set(CustomsRegulationTypePage, CustomsRegulationType.UKCustomsCodeRegulation).success.value

        navigator.nextPage(CustomsRegulationTypePage, NormalMode, answers)
          .mustBe(routes.UkRegulationTypeController.onPageLoad(NormalMode))

      }

      "go to BankDetails page after WhomToPay page when the claimant is representative and has selected importer to be paid" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple, Some("2"))).success.value
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
          .mustBe(routes.ProofOfAuthorityController.showFileUpload(NormalMode))
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

      "go to DeclarantReferenceNumber page after EmailAddressAndPhoneNumberPage page when the Representative's multiple entry journeys is selected " in {
        val answers =
          emptyUserAnswers
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple, Some("2"))).success.value.set(
              ClaimantTypePage,
              ClaimantType.Representative
            ).success.value
        navigator.nextPage(EmailAddressAndPhoneNumberPage, NormalMode, answers)
          .mustBe(routes.DeclarantReferenceNumberController.onPageLoad(NormalMode))
      }

      "go to DeclarantReferenceNumber page after EmailAddressAndPhoneNumberPage page when Importers/Representative single entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)).success.value
        navigator.nextPage(EmailAddressAndPhoneNumberPage, NormalMode, answers)
          .mustBe(routes.DeclarantReferenceNumberController.onPageLoad(NormalMode))
      }

      "go to RepaymentType page after DeclarantReferenceNumber page when Importers/Representative single entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)).success.value
        navigator.nextPage(DeclarantReferenceNumberPage, NormalMode, answers)
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

      "go to Declarant name page after IsVatRegistered page in importer journey" in {
        val answers =
          emptyUserAnswers
            .set(IsVATRegisteredPage, IsVATRegistered.No).success.value
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
        navigator.nextPage(IsVATRegisteredPage, NormalMode, answers)
          .mustBe(routes.DeclarantNameController.onPageLoad(NormalMode))
      }

      "go to 'Do you own the goods page' after Declarant name page in importer journey" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
            .set(DeclarantNamePage, Name("Joe", "Bloggs")).success.value
        navigator.nextPage(DeclarantNamePage, NormalMode, answers)
          .mustBe(routes.DoYouOwnTheGoodsController.onPageLoad(NormalMode))
      }

      "go to Select address for importer page after 'Do you own the goods page' in importer journey if 'Yes' is selected" in {
        val answers =
          emptyUserAnswers
            .set(DoYouOwnTheGoodsPage, DoYouOwnTheGoods.Yes).success.value
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
        navigator.nextPage(DoYouOwnTheGoodsPage, NormalMode, answers)
          .mustBe(routes.ImporterAddressController.onPageLoad(NormalMode))
      }

      "go to Importer name page from 'Do you own the goods page' in importer journey if 'No' is selected" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
            .set(DoYouOwnTheGoodsPage, DoYouOwnTheGoods.No).success.value
        navigator.nextPage(DoYouOwnTheGoodsPage, NormalMode, answers)
          .mustBe(routes.ImporterNameController.onPageLoad(NormalMode))
      }

      "go to select address page after entering the importer name details" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
            .set(ImporterNamePage, UserName("Joe Bloggs")).success.value
        navigator.nextPage(ImporterNamePage, NormalMode, answers)
          .mustBe(routes.ImporterAddressController.onPageLoad(NormalMode))
      }

      "go to PhoneNumber page after importerAddressConfirmation page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value

        navigator.nextPage(AgentImporterManualAddressPage, NormalMode, answers)
          .mustBe(routes.EmailAddressAndPhoneNumberController.onPageLoad(NormalMode))
      }

      "go to ImporterEori page after importerHasEORI with Yes page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ImporterHasEoriPage, true).success.value
        navigator.nextPage(ImporterHasEoriPage, NormalMode, answers)
          .mustBe(routes.ImporterEoriController.onPageLoad(NormalMode))
      }

      "go to Representative importer name page after IsVatRegistered page when Representative single/multiple entry journeys selected" in {
        val answers =
          emptyUserAnswers
            .set(IsImporterVatRegisteredPage, IsImporterVatRegistered.No).success.value
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
        navigator.nextPage(IsImporterVatRegisteredPage, NormalMode, answers)
          .mustBe(routes.RepresentativeImporterNameController.onPageLoad(NormalMode))
      }

      "go to AgentName page after importerHasEORI with No page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ImporterHasEoriPage, false).success.value
        navigator.nextPage(ImporterHasEoriPage, NormalMode, answers)
          .mustBe(routes.RepresentativeDeclarantAndBusinessNameController.onPageLoad(NormalMode))
      }

      "go to AmendCheckYourAnswers page after FurtherInformation page " in {
        navigator.nextPage(FurtherInformationPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.AmendCheckYourAnswersController.onPageLoad)
      }

      "go to AmendConfirmationAnswers page after AmendCheckYourAnswersPage page " in {
        navigator.nextPage(AmendCheckYourAnswersPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.AmendConfirmationController.onPageLoad)
      }

      "go to BankDetails page after DeclarantReferenceNumberPage page when the claimant is importer and has selected multiple entries" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value
            .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple, Some("2"))).success.value
        navigator.nextPage(DeclarantReferenceNumberPage, NormalMode, answers)
          .mustBe(routes.BankDetailsController.onPageLoad(NormalMode))
      }

      "go to PhoneNumber page after ImporterManualAddress page when the claimant is importer" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Importer).success.value

        navigator.nextPage(ImporterManualAddressPage, NormalMode, answers)
          .mustBe(routes.EmailAddressAndPhoneNumberController.onPageLoad(NormalMode))
      }

      "go to ImporterHasEori page after ImporterManualAddress page when the claimant is representative" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value

        navigator.nextPage(ImporterManualAddressPage, NormalMode, answers)
          .mustBe(routes.ImporterHasEoriController.onPageLoad(NormalMode))
      }

      "go to ClaimantType page when CreateOrAmendCase is type of Start a new application" in {
        val answers =
          emptyUserAnswers
            .set(CreateOrAmendCasePage, CreateOrAmendCase.CreateCase).success.value

        navigator.nextPage(CreateOrAmendCasePage, NormalMode, answers)
          .mustBe(routes.ClaimantTypeController.onPageLoad(NormalMode))
      }

      "go to ReferenceNumber page when CreateOrAmendCase is type of Amend an existing application" in {
        val answers =
          emptyUserAnswers
            .set(CreateOrAmendCasePage, CreateOrAmendCase.AmendCase).success.value

        navigator.nextPage(CreateOrAmendCasePage, NormalMode, answers)
          .mustBe(routes.ReferenceNumberController.onPageLoad())
      }

      "go to EvidenceSupportingDocs page after the Customs Duty Paid Page with RepayNormalMode" in {
        navigator.nextPage(CustomsDutyPaidPage, RepayNormalMode, UserAnswers("id"))
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad(NormalMode))
      }

      "go to EvidenceSupportingDocs page after the VAT Paid Page with RepayNormalMode" in {
        navigator.nextPage(VATPaidPage, RepayNormalMode, UserAnswers("id"))
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad(NormalMode))
      }

      "go to EvidenceSupportingDocs page after the Other Duties Paid Page with RepayNormalMode" in {
        navigator.nextPage(OtherDutiesPaidPage, RepayNormalMode, UserAnswers("id"))
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad(NormalMode))
      }
    }

    "in Check mode" must {
      "go to CheckYourAnswers from a page that doesn't exist in the edit route map and it's not an amend journey" in {
        case object UnknownPage extends Page
        val values: Seq[AmendCaseResponseType] = Seq(FurtherInformation)
        val userAnswers                        = UserAnswers(userAnswersId).set(AmendCaseResponseTypePage, values.toSet).success.value

        navigator.nextPage(
          UnknownPage,
          CheckMode,
          userAnswers
        ) mustBe routes.AmendCheckYourAnswersController.onPageLoad()
      }
      "go to Amend Check your answers page when Further information is added no Documents selected" in {
        val values: Seq[AmendCaseResponseType] = Seq(FurtherInformation)
        val userAnswers                        = UserAnswers(userAnswersId).set(AmendCaseResponseTypePage, values.toSet).success.value

        navigator.nextPage(FurtherInformationPage, CheckMode, userAnswers)
          .mustBe(routes.AmendCheckYourAnswersController.onPageLoad())
      }
      "go to Repayment Amount Summary page after the Customs Duty Paid page when VAT and Other Duties is not selected" in {
        val values: Seq[ClaimRepaymentType] = Seq(Customs)
        val userAnswers                     = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(CustomsDutyPaidPage, CheckMode, userAnswers)
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad(CheckMode))
      }
      "go to Vat Paid page after the Customs Duty Paid page when VAT is selected" in {
        val values: Seq[ClaimRepaymentType] = Seq(Customs, Vat)
        val userAnswers                     = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(CustomsDutyPaidPage, CheckMode, userAnswers)
          .mustBe(routes.VATPaidController.onPageLoad(CheckMode))
      }
      "go to Other Duties Paid page after the Customs Duty Paid page when VAT is not selected and Other Duties is selected" in {
        val values: Seq[ClaimRepaymentType] = Seq(Customs, Other)
        val userAnswers                     = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(CustomsDutyPaidPage, CheckMode, userAnswers)
          .mustBe(routes.OtherDutiesPaidController.onPageLoad(CheckMode))
      }
      "go to Repayment Amount Summary page after the VAT Paid page when Other Duties is not selected" in {
        val values: Seq[ClaimRepaymentType] = Seq(Vat)
        val userAnswers                     = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(VATPaidPage, CheckMode, userAnswers)
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad(CheckMode))
      }
      "go to Other Duties page after the VAT Paid page when Other Duties is selected" in {
        val values: Seq[ClaimRepaymentType] = Seq(Other)
        val userAnswers                     = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(VATPaidPage, CheckMode, userAnswers)
          .mustBe(routes.OtherDutiesPaidController.onPageLoad(CheckMode))
      }
      "go to Repayment Amount Summary page after the Other Duties Paid page" in {
        navigator.nextPage(OtherDutiesPaidPage, CheckMode, UserAnswers("id"))
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad(CheckMode))
      }
      "go to Customs Duty Paid page when Customs is selected as a Claim Repayment type" in {
        val values: Seq[ClaimRepaymentType] = Seq(Customs)
        val userAnswers                     = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(ClaimRepaymentTypePage, CheckMode, userAnswers)
          .mustBe(routes.CustomsDutyPaidController.onPageLoad(CheckMode))
      }
      "go to Vat Paid page when Vat is selected as a Claim Repayment type" in {
        val values: Seq[ClaimRepaymentType] = Seq(Vat)
        val userAnswers                     = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(ClaimRepaymentTypePage, CheckMode, userAnswers)
          .mustBe(routes.VATPaidController.onPageLoad(CheckMode))
      }
      "go to Other Duties Paid page when Other is selected as a Claim Repayment type" in {
        val values: Seq[ClaimRepaymentType] = Seq(Other)
        val userAnswers                     = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, values.toSet).success.value

        navigator.nextPage(ClaimRepaymentTypePage, CheckMode, userAnswers)
          .mustBe(routes.OtherDutiesPaidController.onPageLoad(CheckMode))
      }
      "go to the Indirect Representatives page after the Whom to pay page if the representative is to be paid" in {
        val userAnswers = UserAnswers(userAnswersId).set(WhomToPayPage, WhomToPay.Representative).success.value

        navigator.nextPage(WhomToPayPage, CheckMode, userAnswers)
          .mustBe(routes.IndirectRepresentativeController.onPageLoad(CheckMode))
      }
      "go to the Check your answers page after the whom to pay page if the importer is to be paid and bank details have already been entered" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(WhomToPayPage, WhomToPay.Importer).success.value
          .set(BankDetailsPage, BankDetails("name", "111111", "11111111")).success.value

        navigator.nextPage(WhomToPayPage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }
      "go to the Bank Details page after the whom to pay page if the importer is to be paid and bank details have not been entered" in {
        val userAnswers = UserAnswers(userAnswersId).set(WhomToPayPage, WhomToPay.Importer).success.value

        navigator.nextPage(WhomToPayPage, CheckMode, userAnswers)
          .mustBe(routes.BankDetailsController.onPageLoad(CheckMode))
      }
      "go to Bank Details page when BACs is selected as a repayment method and bank account details are empty" in {
        val userAnswers = UserAnswers(userAnswersId).set(RepaymentTypePage, RepaymentType.BACS).success.value

        navigator.nextPage(RepaymentTypePage, CheckMode, userAnswers)
          .mustBe(routes.BankDetailsController.onPageLoad(CheckMode))
      }
      "go to the Whom is to be paid page after the repayment type page when in agent journey and BACS is selected" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(ClaimantTypePage, ClaimantType.Representative).success.value
          .set(RepaymentTypePage, RepaymentType.BACS).success.value

        navigator.nextPage(RepaymentTypePage, CheckMode, userAnswers)
          .mustBe(routes.WhomToPayController.onPageLoad(CheckMode))
      }
      "go to the Proof of Authority page after the Indirect Representative page when in agent journey and is not an indirect representative and BACS is selected" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(ClaimantTypePage, ClaimantType.Representative).success.value
          .set(RepaymentTypePage, RepaymentType.BACS).success.value
          .set(IndirectRepresentativePage, false).success.value

        navigator.nextPage(IndirectRepresentativePage, CheckMode, userAnswers)
          .mustBe(routes.ProofOfAuthorityController.showFileUpload(CheckMode))
      }
      "go to the Bank Details page after the Indirect Representative page when in agent journey and is an indirect representative and BACS is selected" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(ClaimantTypePage, ClaimantType.Representative).success.value
          .set(RepaymentTypePage, RepaymentType.BACS).success.value
          .set(IndirectRepresentativePage, true).success.value

        navigator.nextPage(IndirectRepresentativePage, CheckMode, userAnswers)
          .mustBe(routes.BankDetailsController.onPageLoad(CheckMode))
      }
      "go to the Check your answers page after the Declarant Reference page" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(
            DeclarantReferenceNumberPage,
            DeclarantReferenceNumber(DeclarantReferenceType.Yes, Some("this is a reference"))
          ).success.value

        navigator.nextPage(DeclarantReferenceNumberPage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }
      "go to the Check your answers page after the How can we contact you" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(
            EmailAddressAndPhoneNumberPage,
            EmailAndPhoneNumber(
              Set(IsContactProvided.Email, IsContactProvided.Phone),
              Some("abc@gmail.com"),
              Some("01632 960 001")
            )
          ).success.value

        navigator.nextPage(EmailAddressAndPhoneNumberPage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "go to Check Your Answers page after the Customs Duty Paid Page with RepayCheckMode" in {
        navigator.nextPage(CustomsDutyPaidPage, RepayCheckMode, UserAnswers("id"))
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad(CheckMode))
      }

      "go to Check Your Answers page after the VAT Paid Page with RepayCheckMode" in {
        navigator.nextPage(VATPaidPage, RepayCheckMode, UserAnswers("id"))
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad(CheckMode))
      }

      "go to Check Your Answers page after the Other Duties Paid Page with RepayCheckMode" in {
        navigator.nextPage(OtherDutiesPaidPage, RepayCheckMode, UserAnswers("id"))
          .mustBe(routes.RepaymentAmountSummaryController.onPageLoad(CheckMode))
      }

      "go to Check your answers page after the Declarant name page" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(DeclarantNamePage, Name("Joe", "Bloggs")).success.value
        navigator.nextPage(DeclarantNamePage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "go to Check your answers page when Do You own the goods answer is 'Yes'" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(DoYouOwnTheGoodsPage, DoYouOwnTheGoods.Yes).success.value
        navigator.nextPage(DeclarantNamePage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "go to Check your answers page after Importer name page" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(ImporterNamePage, UserName("Joe Bloggs")).success.value
        navigator.nextPage(DeclarantNamePage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }
    }
  }
}
