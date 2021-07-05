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

package models.bars

import data.BarsTestData
import org.scalatest.{MustMatchers, WordSpec}

class AssessBusinessBankDetailsResponseSpec extends WordSpec with MustMatchers with BarsTestData {

  "AssessBusinessBankDetailsResponse" should {

    "be valid with a valid response" in {
      validAssessResponse.validAccountAndSortCode mustBe true
      validAssessResponse.rollNotRequired mustBe true
      validAssessResponse.accountValid mustBe true
      validAssessResponse.companyNameValid mustBe true
    }

    "have invalid account and sortcode " in {
      validAssessResponse.copy(accountNumberWithSortCodeIsValid = "no").validAccountAndSortCode mustBe false
      validAssessResponse.copy(accountNumberWithSortCodeIsValid = "indeterminate").validAccountAndSortCode mustBe false
    }

    "require roll number " in {
      validAssessResponse.copy(nonStandardAccountDetailsRequiredForBacs = "yes").rollNotRequired mustBe false
    }

    "have non-existent account " in {
      validAssessResponse.copy(accountExists = "no").accountValid mustBe false
    }

    "have wrong account name " in {
      validAssessResponse.copy(companyNameMatches = "no").companyNameValid mustBe false
    }
  }
}
