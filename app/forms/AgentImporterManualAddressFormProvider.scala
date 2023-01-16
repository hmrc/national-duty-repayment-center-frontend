/*
 * Copyright 2023 HM Revenue & Customs
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

class AgentImporterManualAddressFormProvider @Inject() (implicit val countriesService: CountryService)
    extends Mappings with AddressHandling {

  def apply(): Form[Address] =
    Form(
      mapping(
        "AddressLine1" ->
          text("agentImporterManualAddress.line1.error.required")
            .verifying(firstError(maxLength(maxLineLength, "agentImporterManualAddress.line1.error.length"))),
        "AddressLine2" ->
          optional(
            Forms.text
              .verifying(firstError(maxLength(maxLineLength, "agentImporterManualAddress.line2.error.length")))
          ),
        "City" ->
          text("agentImporterManualAddress.city.error.required")
            .verifying(firstError(maxLength(maxCityLength, "agentImporterManualAddress.city.error.length"))),
        "Region" ->
          optional(
            Forms.text
              .verifying(firstError(maxLength(maxRegionLength, "agentImporterManualAddress.region.error.length")))
          ),
        "CountryCode" ->
          text("agentImporterManualAddress.countryCode.error.required")
            .verifying(firstError(maxLength(maxCCLength, "agentImporterManualAddress.countryCode.error.length"))),
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
        ),
        "auditRef" -> optional(text())
      )(formToModel)(modelToForm)
    )

}
