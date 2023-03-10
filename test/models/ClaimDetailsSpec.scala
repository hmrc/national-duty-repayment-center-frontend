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

package models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsNumber, JsString, JsSuccess}

import java.time.LocalDate

class ClaimDetailsSpec extends AnyWordSpec with Matchers {
  "ClaimDetails" must {
    "de-serialise local date" in {
      val json = JsString("20200805")
      ClaimDetails.dateFormat.reads(json) mustBe JsSuccess(LocalDate.parse("2020-08-05"))
    }

    "return error on failing to read as LocalDate with date format as 'yyyyMMdd'" in {
      val json = JsString("20-02-2023")
      ClaimDetails.dateFormat.reads(json).isError mustBe true
    }

    "return error on failing to read as LocalDate" in {
      val json = JsNumber(20200805)
      ClaimDetails.dateFormat.reads(json).isError mustBe true
    }
  }
}
