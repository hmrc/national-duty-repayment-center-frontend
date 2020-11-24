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

package models

import models.responses.LookedUpAddressWrapper
import play.api.libs.json.{Json, OFormat}

final case class Address(
                          AddressLine1:String,
                          AddressLine2:Option[String],
                          City:String,
                          Region:String,
                          CountryCode:String,
                          PostalCode:Option[String]
                        ){
  val inlineText: String = List(
    AddressLine1,
    Some(AddressLine2),
    City,
    Region,
    CountryCode,
    Some(PostalCode)
  ).collect { case Some(x) => x }.mkString(", ")
}

object Address {

  implicit val format: OFormat[Address] = Json.format[Address]

  def fromLookupResponse(candidate: LookedUpAddressWrapper): Address = Address(
    candidate.address.line1,
    candidate.address.line2,
    candidate.address.city,
    candidate.address.region,
    candidate.address.countryCode,
    candidate.address.postalCode
  )
}