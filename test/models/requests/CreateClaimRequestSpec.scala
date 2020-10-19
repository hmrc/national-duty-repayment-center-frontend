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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import models._
import base.SpecBase
import org.scalatest.MustMatchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsSuccess, Json}

class CreateClaimRequestSpec extends SpecBase with MustMatchers with MockitoSugar {

  "CreateClaimRequest" must {
    "serialise and deserialise to / from a claim period" in {

      Json.toJson(createClaimRequest) mustEqual json
      json.validate[CreateClaimRequest] mustEqual JsSuccess(createClaimRequest)
    }
  }
}
