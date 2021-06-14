/*
 * Copyright 2021 HM Revenue & Customs
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

package models.eis

import models.Address
import play.api.libs.json.{Json, OFormat}

final case class EISAddress(
  AddressLine1: String,
  AddressLine2: Option[String],
  City: String,
  Region: Option[String],
  CountryCode: String,
  PostalCode: Option[String]
)

object EISAddress {

  implicit val format: OFormat[EISAddress] = Json.format[EISAddress]

  def apply(address: Address): EISAddress = new EISAddress(
    address.AddressLine1,
    address.AddressLine2,
    address.City,
    address.Region,
    address.Country.code,
    address.PostalCode
  )

}
