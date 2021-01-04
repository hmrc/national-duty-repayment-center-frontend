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

import javax.inject.Inject
import forms.mappings.Mappings
import models.Address
import play.api.data.{Form, Forms}
import play.api.data.Forms.{mapping, optional}

class AgentImporterAddressFormProvider @Inject() extends Mappings {

  private val maxLineLength = 128
  private val maxCityLength = 64
  private val maxRegionLength = 64
  private val maxCCLength = 2

  def apply(): Form[Address] = Form(
    mapping(
      "AddressLine1" ->
        text("agentImporterAddress.line1.error.required")
          .verifying(firstError(
            maxLength(maxLineLength, "agentImporterAddress.line1.error.length"),
            regexp(Validation.safeInputPattern,"agentImporterAddress.line1.error.invalid")
          )),
      "AddressLine2" ->
        optional(Forms.text
          .verifying(firstError(
            maxLength(maxLineLength, "agentImporterAddress.line2.error.length"),
            regexp(Validation.safeInputPattern,"agentImporterAddress.line2.error.invalid")
          ))),
      "City" ->
        text("agentImporterAddress.city.error.required")
          .verifying(firstError(
            maxLength(maxCityLength, "agentImporterAddress.city.error.length"),
            regexp(Validation.safeInputPattern,"agentImporterAddress.city.error.invalid")
          )),
      "Region" ->
        optional(Forms.text
          .verifying(firstError(
            maxLength(maxRegionLength, "agentImporterAddress.region.error.length"),
            regexp(Validation.safeInputPattern,"agentImporterAddress.region.error.invalid")
          ))),
      "CountryCode" ->
        text("agentImporterAddress.countryCode.error.required")
          .verifying(firstError(
            maxLength(maxCCLength, "agentImporterAddress.countryCode.error.length"),
            regexp(Validation.safeInputPattern,"agentImporterAddress.countryCode.error.invalid")
          )),
      "postCode" ->
        optional(Forms.text
          .verifying(firstError(
            regexp(Validation.postcodeRegex, "agentImporterAddress.postalCode.error.invalid")
          )))
    )(Address.apply)(agentImporterAddress => Some((agentImporterAddress.AddressLine1, agentImporterAddress.AddressLine2, agentImporterAddress.City, agentImporterAddress.Region, agentImporterAddress.CountryCode, agentImporterAddress.postCode)))
  )
}
