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

package models.requests

import base.SpecBase
import data.TestData._
import models.EORI
import models.eis.QuoteFormatter
import org.mockito.Mockito.verify
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar

class CreateClaimBuilderSpec extends SpecBase with Matchers with MockitoSugar {

  "CreateClaimBuilder" must {
    "use QuoteFormatter when creating ClaimDescription" in {

      val formatter = mock[QuoteFormatter]

      val builder = new CreateClaimBuilder(formatter)

      val userAnswers = populateUserAnswersWithImporterInformation(emptyUserAnswers)

      builder.buildValidClaimRequest(userAnswers)

      verify(formatter).format(testClaimDescription.value)

    }

    val createClaimBuilder = injector.instanceOf[CreateClaimBuilder]

    "returns a valid CreateClaimRequest for a userAnswers containing a Representative journey with an Email Address" in {
      val testUserAnswers = populateUserAnswersRepresentativeWithEmail(emptyUserAnswers)

      val result = createClaimBuilder.buildValidClaimRequest(testUserAnswers)

      result mustBe Some(testCreateClaimRequestRepresentativeWithEmail)
    }

    "returns a valid CreateClaimRequest for a userAnswers containing claimantType as Representative and Multiple Entries" in {
      val testUserAnswers = populateUserAnswersWithRepresentativeAndMultipleEntries(emptyUserAnswers)

      val result = createClaimBuilder.buildValidClaimRequest(testUserAnswers)

      result mustBe Some(testCreateClaimRequestWithRepresentativeAndMultipleEntries)
    }

    "returns a valid CreateClaimRequest for a userAnswers containing claimantType as Representative and PaymentMethod as CMA" in {
      val testUserAnswers = populateUserAnswersWithCMAPaymentMethod(emptyUserAnswers)

      val result = createClaimBuilder.buildValidClaimRequest(testUserAnswers)

      result mustBe Some(testCreateClaimRequestWithCMAPaymentMethod)
    }

    "returns a valid CreateClaimRequest for a userAnswers containing claimantType as Importer and PaymentMethod as CMA" in {
      val testUserAnswers = populateUserAnswersWithCMAPaymentMethodAndClaimantImporter(emptyUserAnswers)

      val result = createClaimBuilder.buildValidClaimRequest(testUserAnswers)

      result mustBe Some(testCreateClaimRequestWithCMAPaymentMethodAndClaimantImporter)
    }

    "returns a valid CreateClaimRequest for a userAnswers containing UK Customs Regulation type" in {
      val testUserAnswers = populateUserAnswersWithUKCustomsRegulationType(emptyUserAnswers)

      val result = createClaimBuilder.buildValidClaimRequest(testUserAnswers)

      result mustBe Some(testCreateClaimRequestWithUKCustomsRegulationType)
    }

    "returns a valid CreateClaimRequest for a userAnswers containing claimant type Representative, single entry, and paying representative by bacs" in {
      val testUserAnswers = populateUserAnswersWithRepresentativeSinglePayingRepresentativeBacs(emptyUserAnswers)

      val result = createClaimBuilder.buildValidClaimRequest(testUserAnswers)

      result mustBe Some(testCreateClaimRequestWithRepresentativeSinglePayingRepresentativeBacs)
    }

    "returns a valid CreateClaimRequest for a userAnswers containing a bank account number with only 6 digits" in {
      val testUserAnswers = populateUserAnswersWithBankAccountNumberContaining6Digits(emptyUserAnswers)

      val result = createClaimBuilder.buildValidClaimRequest(testUserAnswers)

      result mustBe Some(testCreateClaimRequestWithBankAccountNumberContaining6Digits)
    }

    "use users EORI number for ImporterJourney when present" in {
      val userEori = EORI("GB91648723498651345")
      val testUserAnswers =
        populateUserAnswersWithCMAPaymentMethodAndClaimantImporter(emptyUserAnswers).copy(userEori = Some(userEori))

      val result = createClaimBuilder.buildValidClaimRequest(testUserAnswers)

      result.map(_.Content.ImporterDetails.EORI) mustBe Some(userEori)
      result.flatMap(_.Content.AgentDetails) mustBe None
    }

    "use users EORI number for AgentJourney when present" in {
      val userEori = EORI("GB91648723498651345")
      val testUserAnswers =
        populateUserAnswersWithRepresentativeAndMultipleEntries(emptyUserAnswers).copy(userEori = Some(userEori))

      val result = createClaimBuilder.buildValidClaimRequest(testUserAnswers)

      result.map(_.Content.ImporterDetails.EORI) mustBe Some(testImporterEORI)
      result.flatMap(_.Content.AgentDetails.map(_.EORI)) mustBe Some(userEori)
    }
  }
}
