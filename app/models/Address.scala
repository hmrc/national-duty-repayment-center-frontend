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

package models

import play.api.libs.json.{Json, OFormat}

// TODO - add audit ref to this class (for ALF)
final case class Address(
  AddressLine1: String,
  AddressLine2: Option[String],
  City: String,
  Region: Option[String],
  Country: Country,
  PostalCode: String
) {}

object Address {

  implicit val format: OFormat[Address] = Json.format[Address]

}
