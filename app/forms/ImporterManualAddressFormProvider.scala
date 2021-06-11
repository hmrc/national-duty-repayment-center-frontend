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

package forms

import forms.mappings.Mappings
import javax.inject.Inject
import models.Address
import play.api.data.Forms.{mapping, optional}
import play.api.data.{Form, Forms}
import services.CountryService
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

class ImporterManualAddressFormProvider @Inject() (implicit countriesService: CountryService) extends Mappings {

  private val maxLineLength   = 128
  private val maxCityLength   = 64
  private val maxRegionLength = 64
  private val maxCCLength     = 2

  def apply(): Form[Address] = {
    val formToModel = (
      addressLine1: String,
      addressLine2: Option[String],
      city: String,
      region: Option[String],
      countryCode: String,
      postCode: Option[String]
    ) => new Address(addressLine1, addressLine2, city, region, countriesService.find(countryCode), postCode)
    Form(
      mapping(
        "AddressLine1" ->
          text("importerAddress.line1.error.required")
            .verifying(
              firstError(
                maxLength(maxLineLength, "importerAddress.line1.error.length"),
                regexp(Validation.safeInputPattern, "importerAddress.line1.error.invalid")
              )
            ),
        "AddressLine2" ->
          optional(
            Forms.text
              .verifying(
                firstError(
                  maxLength(maxLineLength, "importerAddress.line2.error.length"),
                  regexp(Validation.safeInputPattern, "importerAddress.line2.error.invalid")
                )
              )
          ),
        "City" ->
          text("importerAddress.city.error.required")
            .verifying(
              firstError(
                maxLength(maxCityLength, "importerAddress.city.error.length"),
                regexp(Validation.safeInputPattern, "importerAddress.city.error.invalid")
              )
            ),
        "Region" ->
          optional(
            Forms.text
              .verifying(
                firstError(
                  maxLength(maxRegionLength, "importerAddress.region.error.length"),
                  regexp(Validation.safeInputPattern, "importerAddress.region.error.invalid")
                )
              )
          ),
        "CountryCode" ->
          text("importerAddress.countryCode.error.required")
            .verifying(
              firstError(
                maxLength(maxCCLength, "importerAddress.countryCode.error.length"),
                regexp(Validation.safeInputPattern, "importerAddress.countryCode.error.invalid")
              )
            ),
        "PostalCode" -> mandatoryIfEqual(
          "CountryCode",
          "GB",
          textNoSpaces("postcode.error.required")
            .verifying(
              firstError(
                minLength(2, "importerAddress.postalCode.error.invalid"),
                maxLength(10, "importerAddress.postalCode.error.invalid")
              )
            )
        )
      )(formToModel)(
        importerAddress =>
          Some(
            (
              importerAddress.AddressLine1,
              importerAddress.AddressLine2,
              importerAddress.City,
              importerAddress.Region,
              importerAddress.Country.code,
              importerAddress.PostalCode
            )
          )
      )
    )
  }

}
