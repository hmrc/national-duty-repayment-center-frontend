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

class AgentImporterManualAddressFormProvider @Inject() (implicit countriesService: CountryService) extends Mappings {

  private val maxLineLength       = 128
  private val maxCityLength       = 64
  private val maxRegionLength     = 64
  private val maxCCLength         = 2
  private val minPostalCodeLength = 2
  private val maxPostalCodeLength = 10

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
          text("agentImporterManualAddress.line1.error.required")
            .verifying(
              firstError(
                maxLength(maxLineLength, "agentImporterManualAddress.line1.error.length"),
                regexp(Validation.safeInputPattern, "agentImporterManualAddress.line1.error.invalid")
              )
            ),
        "AddressLine2" ->
          optional(
            Forms.text
              .verifying(
                firstError(
                  maxLength(maxLineLength, "agentImporterManualAddress.line2.error.length"),
                  regexp(Validation.safeInputPattern, "agentImporterManualAddress.line2.error.invalid")
                )
              )
          ),
        "City" ->
          text("agentImporterManualAddress.city.error.required")
            .verifying(
              firstError(
                maxLength(maxCityLength, "agentImporterManualAddress.city.error.length"),
                regexp(Validation.safeInputPattern, "agentImporterManualAddress.city.error.invalid")
              )
            ),
        "Region" ->
          optional(
            Forms.text
              .verifying(
                firstError(
                  maxLength(maxRegionLength, "agentImporterManualAddress.region.error.length"),
                  regexp(Validation.safeInputPattern, "agentImporterManualAddress.region.error.invalid")
                )
              )
          ),
        "CountryCode" ->
          text("agentImporterManualAddress.countryCode.error.required")
            .verifying(
              firstError(
                maxLength(maxCCLength, "agentImporterManualAddress.countryCode.error.length"),
                regexp(Validation.safeInputPattern, "agentImporterManualAddress.countryCode.error.invalid")
              )
            ),
        "PostalCode" -> mandatoryIfEqual(
          "CountryCode",
          "GB",
          textNoSpaces("postcode.error.required")
            .verifying(
              firstError(
                minLength(minPostalCodeLength, "agentImporterManualAddress.postalCode.error.invalid"),
                maxLength(maxPostalCodeLength, "agentImporterManualAddress.postalCode.error.invalid")
              )
            )
        )
      )(formToModel)(
        agentImporterManualAddress =>
          Some(
            (
              agentImporterManualAddress.AddressLine1,
              agentImporterManualAddress.AddressLine2,
              agentImporterManualAddress.City,
              agentImporterManualAddress.Region,
              agentImporterManualAddress.Country.code,
              agentImporterManualAddress.PostalCode
            )
          )
      )
    )
  }

}
