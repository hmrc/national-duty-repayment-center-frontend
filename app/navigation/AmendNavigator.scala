/*
 * Copyright 2022 HM Revenue & Customs
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
import models.UserAnswers
import pages._
import play.api.mvc.Call

class AmendNavigator extends Navigator[UserAnswers] with AmendAnswerConditions with AmendHasAnsweredConditions {

  override protected def checkYourAnswersPage: Call = controllers.routes.AmendCheckYourAnswersController.onPageLoad()

  override protected lazy val pageBeforeNavigation = Some(controllers.routes.CreateOrAmendCaseController.onPageLoad())

  // @formatter:off
  override protected val pageOrder: Seq[P] = Seq(
    P(FirstPage, () => controllers.routes.ReferenceNumberController.onPageLoad(), never, always),
    P(ReferenceNumberPage, () => controllers.routes.ReferenceNumberController.onPageLoad(), always, caseReferenceAnswered),
    P(AmendCaseResponseTypePage, () => controllers.routes.AmendCaseResponseTypeController.onPageLoad(), always, caseResponseTypeAnswered),
    P(AmendFileUploadPage, () => controllers.routes.AmendCaseSendInformationController.showFileUpload(), showFileUpload, fileUploadedAnswered),
    P(FurtherInformationPage, () => controllers.routes.FurtherInformationController.onPageLoad(), showFurtherInformation, furtherInformationAnswered),
    P(AmendCheckYourAnswersPage, () => controllers.routes.AmendCheckYourAnswersController.onPageLoad(), always, never),
    P(AmendConfirmationPage, () => controllers.routes.AmendConfirmationController.onPageLoad(), always, never)
  )
  // @formatter:on

}

protected trait AmendAnswerConditions {

  protected val always: UserAnswers => Boolean = (_: UserAnswers) => true

  protected val showFileUpload: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(AmendCaseResponseTypePage).exists(_.contains(SupportingDocuments))

  protected val showFurtherInformation: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(AmendCaseResponseTypePage).exists(_.contains(FurtherInformation))

}

protected trait AmendHasAnsweredConditions {

  protected val never: UserAnswers => Boolean                      = (_: UserAnswers) => false
  protected val caseReferenceAnswered: UserAnswers => Boolean      = _.get(ReferenceNumberPage).nonEmpty
  protected val caseResponseTypeAnswered: UserAnswers => Boolean   = _.get(AmendCaseResponseTypePage).nonEmpty
  protected val furtherInformationAnswered: UserAnswers => Boolean = _.get(FurtherInformationPage).nonEmpty

  protected val fileUploadedAnswered: UserAnswers => Boolean = (answers: UserAnswers) =>
    answers.get(AmendCaseResponseTypePage).exists(_.contains(SupportingDocuments)) && answers.fileUploadState.exists(
      state => state.fileUploads.nonEmpty
    )

}
