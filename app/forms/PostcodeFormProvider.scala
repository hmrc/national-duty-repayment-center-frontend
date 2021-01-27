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
import models.PostcodeLookup
import play.api.data.Forms._
import play.api.data.{Form, Forms}

class PostcodeFormProvider @Inject() extends Mappings {

  val postalCodeMinLength = 6
  val postalCodeMaxLength = 9

  def apply(): Form[PostcodeLookup] =
    Form(
      mapping(
        "postCode" -> text("postcode.error.required")
          .verifying(minLength(postalCodeMinLength, "postcode.error.length"))
          .verifying(maxLength(postalCodeMaxLength, "postcode.error.length"))
      )(PostcodeLookup.apply)(PostcodeLookup.unapply)
    )
}