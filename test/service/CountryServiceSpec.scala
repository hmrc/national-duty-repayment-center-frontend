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

package service

import base.SpecBase
import models.Country
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import services.ForeignOfficeCountryService

class CountryServiceSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks with OptionValues {
  "CountryService" should {
    "findAll countries when welshFlag is true" in {
      ForeignOfficeCountryService.findAll(true).head mustBe Country("AF", "Affganistan")
    }

    "find countries when welshFlag is true" in {
      ForeignOfficeCountryService.find("AG", true) mustBe Country("AG", "Antigua a Barbuda")
    }
  }
}
