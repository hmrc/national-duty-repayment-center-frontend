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

package models.requests

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, JsSuccess, Json}

import models.AcknowledgementReference

class AcknowledgementReferenceSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with Generators {

  "AcknowledgementReference mode" - {
    "must serialise" in {
      forAll(arbitrary[AcknowledgementReference]) {
        acknowledgementReference =>

          val json = generateSubmissionJson(acknowledgementReference)
          (json \ "acknowledgementReference").validate[AcknowledgementReference] mustEqual JsSuccess(acknowledgementReference)
      }
    }
  }

  def generateSubmissionJson(cmr: AcknowledgementReference) : JsObject =
    Json.obj("acknowledgementReference" -> cmr.value)
}
