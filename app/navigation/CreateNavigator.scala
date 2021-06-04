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

import models.{AgentImporterHasEORI, ClaimRepaymentType, CustomsRegulationType, DoYouOwnTheGoods, NumberOfEntriesType, RepaymentType, UserAnswers, WhomToPay}
import pages._
import play.api.mvc.Call

class CreateNavigator extends Navigator2[UserAnswers] with CreateAnswerConditions with CreateHasAnsweredConditions {

  // @formatter:off
  override protected val pageOrder: Seq[P] = Seq(
    P(CreateOrAmendCasePage, controllers.routes.CreateOrAmendCaseController.onPageLoad, always, always),
    P(ClaimantTypePage, controllers.routes.ClaimantTypeController.onPageLoad, always, claimantTypeAnswered),
    P(NumberOfEntriesTypePage, controllers.routes.NumberOfEntriesTypeController.onPageLoad, always, numberOfEntriesAnswered),
    P(CustomsRegulationTypePage, controllers.routes.CustomsRegulationTypeController.onPageLoad, always, customsRegulationAnswered),
    P(UkRegulationTypePage, controllers.routes.UkRegulationTypeController.onPageLoad, showUkRegulationType, ukRegulationTypeAnswered),
    P(ArticleTypePage, controllers.routes.ArticleTypeController.onPageLoad, showArticleType, ukRegulationTypeAnswered),
    P(EntryDetailsPage, controllers.routes.EntryDetailsController.onPageLoad, showEntryDetails, entryDetailsAnswered),
    P(ClaimReasonTypePage, controllers.routes.ClaimReasonTypeController.onPageLoad, always, claimReasonAnswered),
    P(ReasonForOverpaymentPage, controllers.routes.ReasonForOverpaymentController.onPageLoad, always, reasonForOverpaymentAnswered),
    P(ClaimRepaymentTypePage, controllers.routes.ClaimRepaymentTypeController.onPageLoad, always, claimRepaymentTypeAnswered),
    P(CustomsDutyPaidPage, controllers.routes.CustomsDutyPaidController.onPageLoad, showCustomsDutyPaid, customsDutyPaidAnswered),
    P(VATPaidPage, controllers.routes.VATPaidController.onPageLoad, showVatPaid, vatPaidAnswered),
    P(OtherDutiesPaidPage, controllers.routes.OtherDutiesPaidController.onPageLoad, showOtherDutyPaid, otherDutyPaidAnswered),
    P(RepaymentSummaryPage, controllers.routes.RepaymentAmountSummaryController.onPageLoad, always, repaymentSummaryAnswered),
    P(SupportingDocumentsPage, controllers.routes.EvidenceSupportingDocsController.onPageLoad, always, fileUploadedAnswered),
    P(FileUploadPage, controllers.routes.FileUploadController.showFileUpload, showFileUpload, fileUploadedAnswered),
    P(FileUploadedPage, controllers.routes.FileUploadController.showFileUploaded, always, fileUploadedAnswered),

    P(ImporterHasEoriPage, controllers.routes.ImporterHasEoriController.onPageLoad, isImporter, importerHasEoriAnswered),
    P(ImporterEoriPage, controllers.routes.ImporterEoriController.onPageLoad, showImporterEori, importerEoriAnswered),

    P(AgentImporterHasEORIPage, controllers.routes.AgentImporterHasEORIController.onPageLoad, isAgent, agentImporterHasEoriAnswered),
    P(EnterAgentEORIPage, controllers.routes.EnterAgentEORIController.onPageLoad, showEnterAgentEori, enterAgentEoriAnswered),

    P(IsVATRegisteredPage, controllers.routes.IsVATRegisteredController.onPageLoad, isImporter, isVatRegisteredAnswered),
    P(IsImporterVatRegisteredPage, controllers.routes.IsImporterVatRegisteredController.onPageLoad, isAgent, isImporterVatRegisteredAnswered),
    P(RepresentativeImporterNamePage, controllers.routes.RepresentativeImporterNameController.onPageLoad, isAgent, representativeImporterNameAnswered),

    P(DeclarantNamePage, controllers.routes.DeclarantNameController.onPageLoad, isImporter, declarantNameAnswered),
    P(DoYouOwnTheGoodsPage, controllers.routes.DoYouOwnTheGoodsController.onPageLoad, isImporter, doYouOwnGoodsAnswered),
    P(ImporterNamePage, controllers.routes.ImporterNameController.onPageLoad, showImporterName, importerNameAnswered),

    P(ImporterAddressPage, controllers.routes.ImporterAddressController.onPageLoad, always, importerAddressAnswered),

    P(ImporterHasAgentEoriPage, controllers.routes.ImporterHasEoriController.onPageLoad, isAgent, importerHasAgentEoriAnswered),
    P(ImporterAgentEoriPage, controllers.routes.ImporterEoriController.onPageLoad, showImporterAgentEori, importerAgentEoriAnswered),

    P(RepresentativeDeclarantAndBusinessNamePage, controllers.routes.RepresentativeDeclarantAndBusinessNameController.onPageLoad, isAgent, representativeDeclarantAndBusinessNameAnswered),

    P(AgentImporterAddressPage, controllers.routes.AgentImporterAddressController.onPageLoad, isAgent, agentImporterAddressAnswered),

    P(EmailAddressAndPhoneNumberPage, controllers.routes.EmailAddressAndPhoneNumberController.onPageLoad, always, emailAndPhoneNumberAnswered),
    P(DeclarantReferenceNumberPage, controllers.routes.DeclarantReferenceNumberController.onPageLoad, always, declarantReferenceNumberAnswered),
    P(RepaymentTypePage, controllers.routes.RepaymentTypeController.onPageLoad, always, repaymentTypeAnswered),

    P(WhomToPayPage, controllers.routes.WhomToPayController.onPageLoad, showWhomToRepay, whoToRepayAnsweredAnswered),
    P(IndirectRepresentativePage, controllers.routes.IndirectRepresentativeController.onPageLoad, showIndirectRepresentative, indirectRepresentativeAnswered),


    P(BankDetailsPage, controllers.routes.BankDetailsController.onPageLoad, showBankDetails, bankDetailsAnswered),

    P(CheckYourAnswersPage, controllers.routes.CheckYourAnswersController.onPageLoad, always, never),
    P(ConfirmationPage, controllers.routes.ConfirmationController.onPageLoad, always, never)
  )
  // @formatter:off

  override protected def checkYourAnswersPage: Call = controllers.routes.CheckYourAnswersController.onPageLoad()

  override protected def pageFor: String => Option[Page] = (pageName: String) =>
    pageOrder.find(_.page.toString == pageName).map(_.page)

}

protected trait CreateAnswerConditions {

  protected val always: UserAnswers => Boolean = (_: UserAnswers) => true

  protected val isImporter: UserAnswers => Boolean = _.isImporterJourney
  protected val isAgent: UserAnswers => Boolean = _.isAgentJourney

  protected val showUkRegulationType: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(CustomsRegulationTypePage).contains(CustomsRegulationType.UKCustomsCodeRegulation)

  protected val showArticleType: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(CustomsRegulationTypePage).contains(CustomsRegulationType.UnionsCustomsCodeRegulation)

  protected val showEntryDetails: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(NumberOfEntriesTypePage).exists(entries => entries.numberOfEntriesType == NumberOfEntriesType.Single)

  protected val showCustomsDutyPaid: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(ClaimRepaymentTypePage).exists(_.contains(ClaimRepaymentType.Customs))

  protected val showVatPaid: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(ClaimRepaymentTypePage).exists(_.contains(ClaimRepaymentType.Vat))

  protected val showOtherDutyPaid: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(ClaimRepaymentTypePage).exists(_.contains(ClaimRepaymentType.Other))

  protected val showFileUpload: UserAnswers => Boolean = (answers: UserAnswers) =>
   answers.fileUploadState.isEmpty || answers.fileUploadState.exists(state => state.fileUploads.isEmpty)

  protected val showImporterEori: UserAnswers => Boolean = (answers: UserAnswers) =>
   answers.isImporterJourney && answers.get(ImporterHasEoriPage).contains(true)

  protected val showImporterAgentEori: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.isAgentJourney && answers.get(ImporterHasEoriPage).contains(true)

  protected val showEnterAgentEori: UserAnswers => Boolean = (answers: UserAnswers) =>
   answers.isAgentJourney && answers.get(AgentImporterHasEORIPage).contains(AgentImporterHasEORI.Yes)

  protected val showImporterName: UserAnswers => Boolean = (answers: UserAnswers) =>
   answers.isImporterJourney && answers.get(DoYouOwnTheGoodsPage).contains(DoYouOwnTheGoods.No)

  protected val showWhomToRepay: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.isAgentJourney && answers.get(RepaymentTypePage).contains(RepaymentType.BACS)

  protected val showIndirectRepresentative: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.isAgentJourney && answers.get(WhomToPayPage).contains(WhomToPay.Representative)

  protected val showBankDetails: UserAnswers => Boolean = (answers: UserAnswers) =>
      answers.get(RepaymentTypePage).contains(RepaymentType.BACS)
}

protected trait CreateHasAnsweredConditions {

  protected val never: UserAnswers => Boolean                        = (_: UserAnswers) => false
  protected val claimantTypeAnswered: UserAnswers => Boolean         = _.get(ClaimantTypePage).nonEmpty
  protected val numberOfEntriesAnswered: UserAnswers => Boolean      = _.get(NumberOfEntriesTypePage).nonEmpty
  protected val customsRegulationAnswered: UserAnswers => Boolean    = _.get(CustomsRegulationTypePage).nonEmpty
  protected val ukRegulationTypeAnswered: UserAnswers => Boolean     = _.get(UkRegulationTypePage).nonEmpty
  protected val articleTypeAnswered: UserAnswers => Boolean          = _.get(ArticleTypePage).nonEmpty
  protected val entryDetailsAnswered: UserAnswers => Boolean         = _.get(EntryDetailsPage).nonEmpty
  protected val claimReasonAnswered: UserAnswers => Boolean          = _.get(ClaimReasonTypePage).nonEmpty
  protected val reasonForOverpaymentAnswered: UserAnswers => Boolean = _.get(ReasonForOverpaymentPage).nonEmpty
  protected val claimRepaymentTypeAnswered: UserAnswers => Boolean   = _.get(ClaimRepaymentTypePage).nonEmpty
  protected val customsDutyPaidAnswered: UserAnswers => Boolean      = _.get(CustomsDutyPaidPage).nonEmpty
  protected val vatPaidAnswered: UserAnswers => Boolean              = _.get(VATPaidPage).nonEmpty
  protected val otherDutyPaidAnswered: UserAnswers => Boolean        = _.get(OtherDutiesPaidPage).nonEmpty
  private val dutyPages: Set[String] =
    Set(ClaimRepaymentTypePage, CustomsDutyPaidPage, VATPaidPage, OtherDutiesPaidPage, RepaymentSummaryPage)

  protected val repaymentSummaryAnswered: UserAnswers => Boolean = (answers: UserAnswers) =>
    !answers.changePage.exists(page => dutyPages.contains(page))

  protected val fileUploadedAnswered: UserAnswers => Boolean = (answers: UserAnswers) =>
     answers.fileUploadState.exists(state => state.fileUploads.nonEmpty)

  protected val importerHasEoriAnswered: UserAnswers => Boolean        =   (answers: UserAnswers) =>
    answers.isImporterJourney && answers.get(ImporterHasEoriPage).nonEmpty

  protected val importerHasAgentEoriAnswered: UserAnswers => Boolean        =   (answers: UserAnswers) =>
    answers.isAgentJourney && answers.get(ImporterHasEoriPage).nonEmpty


  protected val importerEoriAnswered: UserAnswers => Boolean        = (answers: UserAnswers) =>
    answers.isImporterJourney && answers.get(ImporterEoriPage).nonEmpty

  protected val importerAgentEoriAnswered: UserAnswers => Boolean        = (answers: UserAnswers) =>
    answers.isAgentJourney && answers.get(ImporterEoriPage).nonEmpty

  protected val agentImporterHasEoriAnswered: UserAnswers => Boolean        = _.get(AgentImporterHasEORIPage).nonEmpty
  protected val enterAgentEoriAnswered: UserAnswers => Boolean        = _.get(EnterAgentEORIPage).nonEmpty
  protected val isVatRegisteredAnswered: UserAnswers => Boolean        = _.get(IsVATRegisteredPage).nonEmpty
  protected val isImporterVatRegisteredAnswered: UserAnswers => Boolean        = _.get(IsImporterVatRegisteredPage).nonEmpty
  protected val representativeImporterNameAnswered: UserAnswers => Boolean        = _.get(RepresentativeImporterNamePage).nonEmpty
  protected val declarantNameAnswered: UserAnswers => Boolean        = _.get(DeclarantNamePage).nonEmpty
  protected val doYouOwnGoodsAnswered: UserAnswers => Boolean        = _.get(DoYouOwnTheGoodsPage).nonEmpty
  protected val importerNameAnswered: UserAnswers => Boolean        = _.get(ImporterNamePage).nonEmpty
  protected val importerAddressAnswered: UserAnswers => Boolean        = _.get(ImporterAddressPage).nonEmpty
  protected val emailAndPhoneNumberAnswered: UserAnswers => Boolean        = _.get(EmailAddressAndPhoneNumberPage).nonEmpty
  protected val declarantReferenceNumberAnswered: UserAnswers => Boolean        = _.get(DeclarantReferenceNumberPage).nonEmpty
  protected val repaymentTypeAnswered: UserAnswers => Boolean        = _.get(RepaymentTypePage).nonEmpty
  protected val bankDetailsAnswered: UserAnswers => Boolean        = _.get(BankDetailsPage).nonEmpty
  protected val representativeDeclarantAndBusinessNameAnswered: UserAnswers => Boolean        = _.get(RepresentativeDeclarantAndBusinessNamePage).nonEmpty
  protected val agentImporterAddressAnswered: UserAnswers => Boolean        = _.get(AgentImporterAddressPage).nonEmpty
  protected val whoToRepayAnsweredAnswered: UserAnswers => Boolean        = _.get(WhomToPayPage).nonEmpty
  protected val indirectRepresentativeAnswered: UserAnswers => Boolean        = _.get(IndirectRepresentativePage).nonEmpty
}
