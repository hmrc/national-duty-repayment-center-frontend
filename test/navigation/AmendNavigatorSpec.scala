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

import java.time.ZonedDateTime

import base.SpecBase
import controllers.routes
import models.AmendCaseResponseType._
import models.FileType.SupportingEvidence
import models.{AmendCaseResponseType, FileUpload, FileUploads}
import pages._
import services.FileUploaded

class AmendNavigatorSpec extends SpecBase {

  private val navigator = injector.instanceOf[AmendNavigator]

  val caseRefAnswer                                              = emptyUserAnswers.set(ReferenceNumberPage, "CASE-REF").get
  val documentAndInformationResponse: Set[AmendCaseResponseType] = Set(SupportingDocuments, FurtherInformation)
  val responseTypeAnswer                                         = caseRefAnswer.set(AmendCaseResponseTypePage, documentAndInformationResponse).get

  val fileUploadedState = FileUploaded(
    FileUploads(files =
      Seq(
        FileUpload.Accepted(
          1,
          "foo-bar-ref-1",
          "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
          ZonedDateTime.parse("2018-04-24T09:30:00Z"),
          "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          "test.pdf",
          "application/pdf",
          Some(SupportingEvidence)
        )
      )
    ),
    acknowledged = true
  )

  val uploadAnswers     = responseTypeAnswer.copy(fileUploadState = Some(fileUploadedState))
  val furtherInfoAnswer = uploadAnswers.set(FurtherInformationPage, "Further info").get
  val completeAnswers   = furtherInfoAnswer

  "Amend Navigator going forward" should {

    "goto first page" in {
      navigator.nextPage(FirstPage, emptyUserAnswers) mustBe routes.ReferenceNumberController.onPageLoad()
    }
    "goto next page after case reference" in {
      val answers = emptyUserAnswers.set(ReferenceNumberPage, "CASE-REF").get
      navigator.nextPage(ReferenceNumberPage, answers) mustBe routes.AmendCaseResponseTypeController.onPageLoad()
    }
    "goto next page after response type" when {
      val answers = emptyUserAnswers.set(ReferenceNumberPage, "CASE-REF").get
      "documents selected" in {
        val amendCaseResponseType: Set[AmendCaseResponseType] = Set(SupportingDocuments)
        val docAnswer                                         = answers.set(AmendCaseResponseTypePage, amendCaseResponseType).get
        navigator.nextPage(
          AmendCaseResponseTypePage,
          docAnswer
        ) mustBe routes.AmendCaseSendInformationController.showFileUpload()
      }
      "further information selected" in {
        val amendCaseResponseType: Set[AmendCaseResponseType] = Set(FurtherInformation)
        val docAnswer                                         = answers.set(AmendCaseResponseTypePage, amendCaseResponseType).get
        navigator.nextPage(AmendCaseResponseTypePage, docAnswer) mustBe routes.FurtherInformationController.onPageLoad()
      }
      "both selected" in {
        val amendCaseResponseType: Set[AmendCaseResponseType] = Set(SupportingDocuments, FurtherInformation)
        val docAnswer                                         = answers.set(AmendCaseResponseTypePage, amendCaseResponseType).get
        navigator.nextPage(
          AmendCaseResponseTypePage,
          docAnswer
        ) mustBe routes.AmendCaseSendInformationController.showFileUpload()
      }
    }
    "goto next page after upload documents" when {
      "there is further info to add" in {
        val amendCaseResponseType: Set[AmendCaseResponseType] = Set(SupportingDocuments, FurtherInformation)
        val answers                                           = emptyUserAnswers.set(AmendCaseResponseTypePage, amendCaseResponseType).get
        navigator.nextPage(AmendFileUploadPage, answers) mustBe routes.FurtherInformationController.onPageLoad()
      }
      "there is no further info to add" in {
        val amendCaseResponseType: Set[AmendCaseResponseType] = Set(SupportingDocuments)
        val answers                                           = emptyUserAnswers.set(AmendCaseResponseTypePage, amendCaseResponseType).get
        navigator.nextPage(AmendFileUploadPage, answers) mustBe routes.AmendCheckYourAnswersController.onPageLoad()
      }

    }
    "goto next page after further information" in {
      val answers = emptyUserAnswers.set(FurtherInformationPage, "Further information").get
      navigator.nextPage(FurtherInformationPage, answers) mustBe routes.AmendCheckYourAnswersController.onPageLoad()
    }
  }

  "Amend Navigator going back" should {
    "go back from check your answers" in {
      navigator.previousPage(AmendCheckYourAnswersPage, completeAnswers).maybeCall mustBe Some(
        routes.FurtherInformationController.onPageLoad()
      )
    }
    "go back from further information" in {
      navigator.previousPage(FurtherInformationPage, completeAnswers).maybeCall mustBe Some(
        routes.AmendCaseSendInformationController.showFileUpload()
      )
    }
    "go back from upload page" in {
      navigator.previousPage(AmendFileUploadPage, completeAnswers).maybeCall mustBe Some(
        routes.AmendCaseResponseTypeController.onPageLoad()
      )
    }
    "go back from response type" in {
      navigator.previousPage(AmendCaseResponseTypePage, completeAnswers).maybeCall mustBe Some(
        routes.ReferenceNumberController.onPageLoad()
      )
    }
    "go back from case reference" in {
      navigator.previousPage(ReferenceNumberPage, completeAnswers).maybeCall mustBe Some(
        routes.CreateOrAmendCaseController.onPageLoad()
      )
    }
  }
}
