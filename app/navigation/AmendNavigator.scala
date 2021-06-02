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

import models.AmendCaseResponseType.{FurtherInformation, SupportingDocuments}
import models.{NormalMode, UserAnswers}
import pages._
import play.api.mvc.Call

class AmendNavigator extends Navigator2[UserAnswers] with AmendAnswerConditions with AmendHasAnsweredConditions {
  override protected val pageOrder: Seq[P] = Seq(
    P(CreateOrAmendCasePage, () => controllers.routes.CreateOrAmendCaseController.onPageLoad, always, caseReferenceAnswered),
    P(ReferenceNumberPage, () => controllers.routes.ReferenceNumberController.onPageLoad, always, caseReferenceAnswered),
    P(AmendCaseResponseTypePage, () => controllers.routes.AmendCaseResponseTypeController.onPageLoad, always, caseResponseTypeAnswered),
    P(AmendFileUploadPage, () => controllers.routes.AmendCaseSendInformationController.showFileUpload(NormalMode), showFileUpload, fileUploadedAnswered),
    P(AmendFileUploadedPage, () => controllers.routes.AmendCaseSendInformationController.showFileUploaded(NormalMode), showFileUploaded, fileUploadedAnswered),
    P(FurtherInformationPage, () => controllers.routes.FurtherInformationController.onPageLoad(), showFurtherInformation, furtherInformationAnswered),
    P(AmendCheckYourAnswersPage, controllers.routes.AmendCheckYourAnswersController.onPageLoad, always, never),
    P(AmendConfirmationPage, controllers.routes.AmendConfirmationController.onPageLoad, always, never),
  )

  override protected def checkYourAnswersPage: Call = controllers.routes.AmendCheckYourAnswersController.onPageLoad()

  override protected def pageFor: String => Option[Page] = (pageName: String) => pageOrder.find(_.page.toString == pageName).map(_.page)
}

protected trait AmendAnswerConditions {

  protected val always: UserAnswers => Boolean = (_: UserAnswers) => true
  protected val showFileUpload: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(AmendCaseResponseTypePage).exists(_.contains(SupportingDocuments)) && (answers.fileUploadState.isEmpty || answers.fileUploadState.exists(state => state.fileUploads.isEmpty))
  protected val showFileUploaded: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(AmendCaseResponseTypePage).exists(_.contains(SupportingDocuments))
  protected val showFurtherInformation: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(AmendCaseResponseTypePage).exists(_.contains(FurtherInformation))
}

protected trait AmendHasAnsweredConditions {

  protected val never: UserAnswers => Boolean = (_: UserAnswers) => false
  protected val createOrAmendAnswered: UserAnswers => Boolean = _.get(CreateOrAmendCasePage).nonEmpty
  protected val caseReferenceAnswered: UserAnswers => Boolean = _.get(ReferenceNumberPage).nonEmpty
  protected val caseResponseTypeAnswered: UserAnswers => Boolean = _.get(AmendCaseResponseTypePage).nonEmpty
  protected val furtherInformationAnswered: UserAnswers => Boolean = _.get(FurtherInformationPage).nonEmpty
  protected val fileUploadedAnswered: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(AmendCaseResponseTypePage).exists(_.contains(SupportingDocuments)) && answers.fileUploadState.exists(state => state.fileUploads.nonEmpty)

}