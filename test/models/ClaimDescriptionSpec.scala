/*
 * Copyright 2025 HM Revenue & Customs
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

class ClaimDescriptionSpec extends AnyWordSpec with Matchers {

  "ClaimDescription" must {

    "contain correct value" when {

      val description = "Some description"

      "constructed with only a value" in {

        val descr = ClaimDescription(description)
        descr.value mustBe description
      }

      "constructed with single ClaimReasonType" in {

        val descr = ClaimDescription(description, Set(ClaimReasonType.Preference))
        descr.value mustBe "Claim reason: Preference(06)\n\n" + description
      }

      "constructed with multiple ClaimReasonTypes" in {

        val descr = ClaimDescription(description, Set(ClaimReasonType.Preference, ClaimReasonType.Value))
        descr.value mustBe "Claim reasons: Preference(06), Value(09)\n\n" + description
      }
    }
  }

}
