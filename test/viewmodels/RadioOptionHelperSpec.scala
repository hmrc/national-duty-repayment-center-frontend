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

package viewmodels

import base.SpecBase
import forms.ClaimReasonTypeFormProvider
import models.WithName

class RadioOptionHelperSpec extends SpecBase {

  "Radio Option Helper" must {
    case object Value1 extends WithName("01")
    case object Value2 extends WithName("02")

    val formProvider      = new ClaimReasonTypeFormProvider()
    val form              = formProvider()
    val radioOptionHelper = new RadioOptionHelper(Seq(Value1, Value2))

    "correctly create a list of radios from values" in {
      val options = radioOptionHelper.options(form)

      options(0).value mustEqual Some("01")
      options(1).value mustEqual Some("02")
    }

    "correctly create a list of radios from values with a divider" in {
      val options = radioOptionHelper.optionsWithDivider(form, "or", Value1)

      options(0).value mustEqual Some("01")
      options(1).divider mustEqual Some("or")
      options(2).value mustEqual Some("02")
    }
  }
}
