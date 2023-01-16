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

package models.addresslookup

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AddressLookupConfirmationSpec extends AnyWordSpec with Matchers {

  val addCountry: AddressLookupCountry = AddressLookupCountry(code = "UK", name = "United Kingdom")

  "AddressLookupConfirmation" should {

    "extract all lines of an address" in {
      val confirmation = AddressLookupConfirmation(
        auditRef = "auditRef",
        Some("id"),
        AddressLookupAddress(List("Line1", "Line2", "Line3", "Line4 Town"), postcode = Some("AA000AA"), addCountry)
      )
      val el = confirmation.extractAddressLines()

      el._1 mustBe "Line1"
      el._2 mustBe Some("Line2")
      el._3 mustBe Some("Line3")
      el._4 mustBe "Line4 Town"
    }

    "extract town as the last line from a two line address" in {
      val confirmation = AddressLookupConfirmation(
        auditRef = "auditRef",
        Some("id"),
        AddressLookupAddress(List("Line1", "Line4 Town"), postcode = Some("AA000AA"), addCountry)
      )
      val el = confirmation.extractAddressLines()

      el._1 mustBe "Line1"
      el._2 mustBe None
      el._3 mustBe None
      el._4 mustBe "Line4 Town"
    }

    "extract lines should work with an empty address" in {
      val confirmation = AddressLookupConfirmation(
        auditRef = "auditRef",
        Some("id"),
        AddressLookupAddress(List.empty[String], postcode = None, addCountry)
      )

      val el = confirmation.extractAddressLines()

      el._1 mustBe ""
      el._2 mustBe None
      el._3 mustBe None
      el._4 mustBe ""
    }
  }
}
