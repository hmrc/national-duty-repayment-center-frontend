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

package models

import java.time.LocalDate

import base.SpecBase
import pages.{ClaimRepaymentTypePage, CustomsDutyPaidPage, EntryDetailsPage, NumberOfEntriesTypePage}

class UserAnswersSpec extends SpecBase {

  implicit val config = frontendAppConfig

  val duties: Set[ClaimRepaymentType] = Set(ClaimRepaymentType.Customs)

  val answers =
    emptyUserAnswers
      .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)).success.value
      .set(ClaimRepaymentTypePage, duties).success.value
      .set(CustomsDutyPaidPage, RepaymentAmounts("1000", "0")).success.value
      .set(EntryDetailsPage, EntryDetails("123", "123456Q", LocalDate.now().minusDays(1))).success.value

  "UserAnswers" must {

    "allow CMA" when {
      "for base test answers" in {
        answers.isCmaAllowed mustBe true
      }

      "when claim amount equals limit" in {
        val answersWithMinimumClaim = answers
          .set(
            CustomsDutyPaidPage,
            RepaymentAmounts(config.allowCmaThresholds.reclaimTotal.toString(), "0")
          ).success.value
        answersWithMinimumClaim.isCmaAllowed mustBe true
      }

      "when entry age equals limit" in {
        val answersWithMinimumClaim = answers
          .set(
            EntryDetailsPage,
            EntryDetails("123", "123456Q", LocalDate.now().minusDays(config.allowCmaThresholds.entryAgeDays))
          ).success.value
        answersWithMinimumClaim.isCmaAllowed mustBe true
      }
    }

    "disallows CMA" when {

      "when claim is for multiple entries" in {
        val answersWithMinimumClaim = answers
          .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple, Some("2"))).success.value
        answersWithMinimumClaim.isCmaAllowed mustBe false
      }

      "when claim amount is less than limit" in {
        val answersWithMinimumClaim = answers
          .set(
            CustomsDutyPaidPage,
            RepaymentAmounts((config.allowCmaThresholds.reclaimTotal - 0.01).toString(), "0")
          ).success.value
        answersWithMinimumClaim.isCmaAllowed mustBe false
      }

      "when entry age is older than limit" in {
        val answersWithMinimumClaim = answers
          .set(
            EntryDetailsPage,
            EntryDetails("123", "123456Q", LocalDate.now().minusDays(config.allowCmaThresholds.entryAgeDays + 1))
          ).success.value
        answersWithMinimumClaim.isCmaAllowed mustBe false
      }
    }
  }
}
