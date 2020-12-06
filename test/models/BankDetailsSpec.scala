/*
 * Copyright 2020 HM Revenue & Customs
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

import generators.Generators
import play.api.libs.json.{JsError, JsString, Json}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class BankDetailsSpec extends WordSpec with MustMatchers with Generators with ScalaCheckPropertyChecks  with OptionValues {


  "Bank Details" must {
    "must serialise" in {

      val bnkDtls = stringsWithMaxLength(40)
      forAll(bnkDtls) {
        bankDetails =>

          Json.toJson(bankDetails) mustEqual JsString(bankDetails)
      }

    }


  }




}
