/*
 * Copyright 2023 HM Revenue & Customs
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

package models.requests

import base.SpecBase
import data.TestData._
import models.eis.QuoteFormatter
import models.{AmendCaseResponseType, FileUploads}
import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import pages.{AmendCaseResponseTypePage, FurtherInformationPage, ReferenceNumberPage}
import services.FileUploaded

class AmendClaimBuilderSpec extends SpecBase with Matchers with MockitoSugar {

  "AmendClaimBuilder" must {
    "use QuoteFormatter when creating FurtherInformation" in {

      val formatter = mock[QuoteFormatter]

      val builder = new AmendClaimBuilder(formatter)

      val userAnswers = populateUserAnswersWithAmendData(emptyUserAnswers)

      builder.buildValidAmendRequest(userAnswers)

      verify(formatter).format(furtherInformation)

    }

    "prepend users EORI number to FurtherInformation" when {

      val formatter = injector.instanceOf[QuoteFormatter]
      val builder   = new AmendClaimBuilder(formatter)

      "Further information is submitted" in {

        val responseType: Set[AmendCaseResponseType] =
          Set(AmendCaseResponseType.FurtherInformation)

        val userAnswers = emptyUserAnswersWithEORI("GB1234567890")
          .set(AmendCaseResponseTypePage, responseType)
          .flatMap(_.set(ReferenceNumberPage, referenceNumber))
          .flatMap(_.set(FurtherInformationPage, "Further information provided"))
          .get

        val request = builder.buildValidAmendRequest(userAnswers)

        request.map(_.Content.Description) mustBe Some(s"[EORINumber=GB1234567890]\n\nFurther information provided")

      }

      "Further information is not submitted" in {

        val responseType: Set[AmendCaseResponseType] =
          Set(AmendCaseResponseType.SupportingDocuments)

        val userAnswers = emptyUserAnswersWithEORI("GB534535623443523").copy(fileUploadState =
          Some(FileUploaded(fileUploads = FileUploads(Seq(fileUploaded))))
        )
          .set(AmendCaseResponseTypePage, responseType)
          .flatMap(_.set(ReferenceNumberPage, referenceNumber))
          .get

        val request = builder.buildValidAmendRequest(userAnswers)

        request.map(_.Content.Description) mustBe Some(s"[EORINumber=GB534535623443523]\n\nFiles Uploaded")

      }

      "User does not have EORI number" in {

        val responseType: Set[AmendCaseResponseType] =
          Set(AmendCaseResponseType.SupportingDocuments)

        val userAnswers = emptyUserAnswers.copy(fileUploadState =
          Some(FileUploaded(fileUploads = FileUploads(Seq(fileUploaded))))
        )
          .set(AmendCaseResponseTypePage, responseType)
          .flatMap(_.set(ReferenceNumberPage, referenceNumber))
          .get

        val request = builder.buildValidAmendRequest(userAnswers)

        request.map(_.Content.Description) mustBe Some(s"[EORINumber=GBPR]\n\nFiles Uploaded")

      }
    }
  }
}
