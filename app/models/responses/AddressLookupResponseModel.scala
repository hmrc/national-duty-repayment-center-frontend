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

package models.responses

import models.JsonFormatUtils
import play.api.libs.json._

final case class AddressLookupResponseModel(candidateAddresses: Seq[LookedUpAddressWrapper]) {
  lazy val noOfHits: Int = candidateAddresses.size
}

object AddressLookupResponseModel {

  implicit val reads: Reads[AddressLookupResponseModel] =
    __.read[Seq[LookedUpAddressWrapper]].map(AddressLookupResponseModel.apply)

}

final case class LookedUpAddressWrapper(
  id: String,
  uprn: Uprn,
  address: LookedUpAddress,
  language: String,
  location: Option[Location]
)

object LookedUpAddressWrapper {
  implicit val reads: Reads[LookedUpAddressWrapper] = Json.reads[LookedUpAddressWrapper]
}

case class LookedUpAddress(lines: Seq[String], town: String, county: Option[String], postcode: String)

object LookedUpAddress {
  implicit val reads: Reads[LookedUpAddress] = Json.reads[LookedUpAddress]
}

final case class Uprn(value: Long)

object Uprn {
  implicit val format: Format[Uprn] = JsonFormatUtils.longFormat(Uprn.apply)(_.value)
}

final case class Location(latitude: BigDecimal, longitude: BigDecimal)

object Location {

  private val arrayNumberReads: Reads[Location] = (json: JsValue) =>
    json.validate[Seq[BigDecimal]] match {
      case JsSuccess(Seq(lat, long), _) => JsSuccess(Location(lat, long))
      case _                            => JsError("Expected exactly two numbers for lat and long in location field")
    }

  implicit val format: OFormat[Location] = OFormat(arrayNumberReads orElse Json.reads[Location], Json.writes[Location])
}
