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

class AgentBusinessAddressFormProvider @Inject() extends Mappings {

  private val maxLineLength = 128
  private val maxCityLength = 64
  private val maxRegionLength = 64
  private val maxCCLength = 2

  def apply(): Form[Address] = Form(
    mapping(
      "AddressLine1" ->
        text("agentBusinessAddress.line1.error.required")
          .verifying(firstError(
            maxLength(maxLineLength, "agentBusinessAddress.line1.error.length"),
            regexp(Validation.safeInputPattern,"agentBusinessAddress.line1.error.invalid")
          )),
      "AddressLine2" ->
        optional(Forms.text
          .verifying(firstError(
            maxLength(maxLineLength, "agentBusinessAddress.line2.error.length"),
            regexp(Validation.safeInputPattern,"agentBusinessAddress.line2.error.invalid")
          ))),
      "City" ->
        text("agentBusinessAddress.city.error.required")
          .verifying(firstError(
            maxLength(maxCityLength, "agentBusinessAddress.city.error.length"),
            regexp(Validation.safeInputPattern,"agentBusinessAddress.city.error.invalid")
          )),
      "Region" ->
        optional(Forms.text
          .verifying(firstError(
            maxLength(maxRegionLength, "agentBusinessAddress.region.error.length"),
            regexp(Validation.safeInputPattern,"agentBusinessAddress.region.error.invalid")
          ))),
      "CountryCode" ->
        text("agentBusinessAddress.countryCode.error.required")
          .verifying(firstError(
            maxLength(maxCCLength, "agentBusinessAddress.countryCode.error.length"),
            regexp(Validation.safeInputPattern,"agentBusinessAddress.countryCode.error.invalid")
          )),
      "postCode" ->
        optional(Forms.text
          .verifying(firstError(
            regexp(Validation.postcodeRegex, "agentBusinessAddress.postalCode.error.invalid")
          )))
    )(Address.apply)(agentBusinessAddress => Some((agentBusinessAddress.AddressLine1, agentBusinessAddress.AddressLine2, agentBusinessAddress.City, agentBusinessAddress.Region, agentBusinessAddress.CountryCode, agentBusinessAddress.postCode)))
  )
}
