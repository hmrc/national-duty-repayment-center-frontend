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

package models.responses

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class AddressLookupResponseModelSpec extends AnyFreeSpec with Matchers {
  "AddressLookupResponseModel" - {
    "deserialises correctly from a JSON array" in {
      val testJson = s"""[
                        |${testAddressJson("GB200000706253")},
                        |${testAddressJson("GB200000706254")},
                        |${testAddressJson("GB200000706255")}
                        |]""".stripMargin

      val expectedAddress = LookedUpAddress(Seq("line 1", "line 2"), "ABC Town", Some("ABC County"), "ZZ99 1AA")

      val expectedAddressLookupResponseModel = AddressLookupResponseModel(
        Seq(
          LookedUpAddressWrapper(
            "GB200000706253",
            Uprn(200000706253L),
            expectedAddress,
            "en",
            Some(Location(50.9986451, -1.4690977))
          ),
          LookedUpAddressWrapper(
            "GB200000706254",
            Uprn(200000706253L),
            expectedAddress,
            "en",
            Some(Location(50.9986451, -1.4690977))
          ),
          LookedUpAddressWrapper(
            "GB200000706255",
            Uprn(200000706253L),
            expectedAddress,
            "en",
            Some(Location(50.9986451, -1.4690977))
          )
        )
      )

      Json.parse(testJson).as[AddressLookupResponseModel] must be(expectedAddressLookupResponseModel)
    }
  }

  "Uprn serialisation" - {
    "serialises correctly to JSON number" in {
      val testUprn     = Uprn(200000706253L)
      val expectedJson = "200000706253"

      Json.prettyPrint(Json.toJson(testUprn)) must be(expectedJson)
    }

    "deserialises correctly from a JSON number" in {
      val testJson     = "200000706253"
      val expectedUprn = Uprn(200000706253L)

      Json.parse(testJson).as[Uprn] must be(expectedUprn)
    }
  }

  "Location serialisation" - {
    "serialises correctly to JSON object" in {
      val testLocation = Location(50.9986451, -1.4690977)
      val expectedJson = """{
                           |  "latitude" : 50.9986451,
                           |  "longitude" : -1.4690977
                           |}""".stripMargin

      Json.prettyPrint(Json.toJson(testLocation)) must be(expectedJson)
    }

    "deserialises correctly from a JSON object" in {
      val testJson = """{
                       |  "latitude" : 50.9986451,
                       |  "longitude" : -1.4690977
                       |}""".stripMargin
      val expectedLocation = Location(50.9986451, -1.4690977)

      Json.parse(testJson).as[Location] must be(expectedLocation)
    }

    "deserialises correctly from a JSON array" in {
      val testJson         = """[ 50.9986451, -1.4690977 ]"""
      val expectedLocation = Location(50.9986451, -1.4690977)

      Json.parse(testJson).as[Location] must be(expectedLocation)
    }
  }

  private def testAddressJson(id: String): String =
    s"""{
       |    "id": "$id",
       |    "uprn": 200000706253,
       |    "address": {
       |        "lines": [
       |            "line 1",
       |            "line 2"
       |        ],
       |        "town": "ABC Town",
       |        "county": "ABC County",
       |        "postcode": "ZZ99 1AA",
       |        "subdivision": {
       |            "code": "ZZ-Code",
       |            "name": "ZZ-Name"
       |        },
       |        "country": {
       |            "code": "ZZ",
       |            "name": "ZZ-Countrym"
       |        }
       |    },
       |    "localCustodian": {
       |        "code": 1760,
       |        "name": "ABC-Name"
       |    },
       |    "location": [
       |        50.9986451,
       |        -1.4690977
       |    ],
       |    "language": "en"
       |}""".stripMargin

}
