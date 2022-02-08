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

package forms

import models.Address
import play.api.data.Forms.{mapping, optional}
import play.api.data.{Form, Forms}
import services.CountryService

trait AddressHandling {

  val countriesService: CountryService

  protected val maxLineLength       = 128
  protected val maxCityLength       = 64
  protected val maxRegionLength     = 64
  protected val maxCCLength         = 2
  protected val minPostalCodeLength = 2
  protected val maxPostalCodeLength = 10

  val formToModel: (String, Option[String], String, Option[String], String, Option[String], Option[String]) => Address =
    (
      addressLine1: String,
      addressLine2: Option[String],
      city: String,
      region: Option[String],
      countryCode: String,
      postCode: Option[String],
      auditRef: Option[String]
    ) => new Address(addressLine1, addressLine2, city, region, countriesService.find(countryCode), postCode, auditRef)

  val modelToForm
    : Address => Some[(String, Option[String], String, Option[String], String, Option[String], Option[String])] =
    (address: Address) =>
      Some(
        (
          address.AddressLine1,
          address.AddressLine2,
          address.City,
          address.Region,
          address.Country.code,
          address.PostalCode,
          address.auditRef
        )
      )

  /* Use this to unbind the form data from the request to get around the problem of conditional mandatoryIfEqual mapping not binding postcode */
  val dataExtractor: Form[Address] =
    Form(
      mapping(
        "AddressLine1" ->
          Forms.text,
        "AddressLine2" ->
          optional(Forms.text),
        "City" ->
          Forms.text,
        "Region" ->
          optional(Forms.text),
        "CountryCode" ->
          Forms.text,
        "PostalCode" ->
          optional(Forms.text),
        "auditRef" -> optional(Forms.text)
      )(formToModel)(modelToForm)
    )

}
