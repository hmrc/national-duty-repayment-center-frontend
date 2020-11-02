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

package forms

import forms.mappings.Mappings
import javax.inject.Inject
import models.Address
import play.api.data.Forms._
import play.api.data.{Form, Forms}

class AddressFormProvider @Inject() extends Mappings {
  private val maxLineLength = 35
  private val maxCountyLength = 100

  def apply(): Form[Address] = Form(
    mapping(
      "line1" ->
        text("address.error.line1.required")
          .verifying(firstError(
            maxLength(maxLineLength, "address.error.line1.length"),
       //     regexp(Validation.safeInputPattern,"address.error.line1.invalid")
          )),
      "line2" ->
        optional(Forms.text
          .verifying(firstError(
            maxLength(maxLineLength, "address.error.line2.length"),
      //      regexp(Validation.safeInputPattern,"address.error.line2.invalid")
          ))),
      "town" ->
        text("address.error.town.required")
          .verifying(firstError(
            maxLength(maxLineLength, "address.error.town.length"),
        //    regexp(Validation.safeInputPattern,"address.error.town.invalid")
          )),
      "county" ->
        optional(Forms.text
          .verifying(firstError(
            maxLength(maxCountyLength, "address.error.county.length"),
          //  regexp(Validation.safeInputPattern,"address.error.county.invalid")
          ))),
      "postCode" ->
        text("address.error.postCode.required")
          .verifying(firstError(
          //  regexp(Validation.postcodeRegex, "address.error.postCode.invalid")
          ))
    )(Address.apply)(address => Some((address.AddressLine1, Some(address.AddressLine2), address.City, address.Region,
      address.CountryCode, Some(address.PostalCode), Some(address.TelephoneNumber),Some(address.EmailAddress))))
  )

}
