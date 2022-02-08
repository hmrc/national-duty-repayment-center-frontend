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

package models

import java.time.Instant

import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.libs.json.Json

class MongoFormatsSpec extends FreeSpec with MustMatchers with OptionValues with MongoFormats {

  "an Instant" - {

    val epochMillis = 1517443200000L
    val instant     = Instant.ofEpochMilli(epochMillis)

    val json = Json.obj("$date" -> epochMillis)

    "must serialise to json" in {
      val result = Json.toJson(instant)
      result mustEqual json
    }

    "must deserialise from json" in {
      val result = json.as[Instant]
      result mustEqual instant
    }

    "must serialise/deserialise to the same value" in {
      val result = Json.toJson(instant).as[Instant]
      result mustEqual instant
    }
  }
}
