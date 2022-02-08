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

package models.bars

import data.BarsTestData
import org.scalatest.{MustMatchers, WordSpec}

class BARSResultSpec extends WordSpec with MustMatchers with BarsTestData {

  "BARSResult" should {

    "be valid if assess call is valid" in {

      BARSResult(validAssessResponse).isValid mustBe true
    }

    "be invalid if sort code does not match account" in {

      BARSResult(validAssessResponse.copy(accountNumberIsWellFormatted = "no")).isValid mustBe false
    }

    "be valid if it is indeterminate if sort code and account match" in {

      BARSResult(validAssessResponse.copy(accountNumberIsWellFormatted = "indeterminate")).isValid mustBe true
    }

    "be invalid if roll IS required" in {

      BARSResult(validAssessResponse.copy(nonStandardAccountDetailsRequiredForBacs = "yes")).isValid mustBe false
    }

    "be invalid if bacs credits is not supported" in {

      BARSResult(validAssessResponse.copy(sortCodeSupportsDirectCredit = "no")).isValid mustBe false
    }
  }

}
