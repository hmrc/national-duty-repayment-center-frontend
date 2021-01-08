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
import pages._
import models._
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

      "go to BankDetails page after WhomToPay page when the claimant is representative and has selected importer to be paid" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
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
          .mustBe(routes.ProofOfAuthorityController.onPageLoad)
      }

      "go to BankDetails page after the ProofOfAuthority page once the representative has uploaded their proof of authority" in {

        val answers =
          emptyUserAnswers
        navigator.nextPage(ProofOfAuthorityPage, NormalMode, answers)
          .mustBe(routes.BankDetailsController.onPageLoad(NormalMode))

      }

      "go to BulkFileUpload page after the customsRegulationType page the  UnionsCustomsCodeRegulation has been selected" in {

        val answers =
          emptyUserAnswers
            .set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation).success.value
        navigator.nextPage(CustomsRegulationTypePage, NormalMode, answers)
          .mustBe(routes.BulkFileUploadController.onPageLoad)

      }



      "go to CheckYourAnswers page after the bank details has been entered " in {

        val answers =
          emptyUserAnswers
        navigator.nextPage(BankDetailsPage, NormalMode, answers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad)

      }

      "go to EntryDetails page after ArticleType page " in {
        navigator.nextPage(ArticleTypePage, NormalMode, emptyUserAnswers)
          .mustBe(routes.EntryDetailsController.onPageLoad(NormalMode))
      }

      "go to ClaimReasonType page after EntryDetails page " in {
        navigator.nextPage(EntryDetailsPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.ClaimReasonTypeController.onPageLoad(NormalMode))
      }

      "go to WhomToPay page after ContactByEmailPage page when the Representative's multiple entry journeys is selected " in {
        val answers =
          emptyUserAnswers
            .set(NumberOfEntriesTypePage, NumberOfEntriesType.Multiple).success.value.
            set(ClaimantTypePage, ClaimantType.Representative).success.value
        navigator.nextPage(ContactByEmailPage, NormalMode, answers)
          .mustBe(routes.WhomToPayController.onPageLoad(NormalMode))
      }

      "go to RepaymentType page after ContactByEmailPage page when Importers/Representative single entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(NumberOfEntriesTypePage, NumberOfEntriesType.Single).success.value
        navigator.nextPage(ContactByEmailPage, NormalMode, answers)
          .mustBe(routes.RepaymentTypeController.onPageLoad(NormalMode))
      }

      "go to AgentImporterHasEORI page after additionalFileUpload page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value.
            set(AdditionalFileUploadPage, AdditionalFileUpload.No).success.value
        navigator.nextPage(AdditionalFileUploadPage, NormalMode, answers)
          .mustBe(routes.AgentImporterHasEORIController.onPageLoad(NormalMode))
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

      "go to ImporterHasEori page after importerAddressConfirmation page when Representative single/multiple entry journeys selected " in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value

        navigator.nextPage(AgentImporterManualAddressPage, NormalMode, answers)
          .mustBe(routes.ImporterHasEoriController.onPageLoad(NormalMode))
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
    }


    "in Check mode" must {

      "go to CheckYourAnswers from a page that doesn't exist in the edit route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
