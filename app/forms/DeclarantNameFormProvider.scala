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
import models.Name
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class DeclarantNameFormProvider @Inject() extends Mappings {

  def apply(): Form[Name] =
    Form(
      mapping(
        "firstName" -> text("declarantName.firstName.error.required")
          .verifying(firstError(maxLength(255, "declarantName.firstName.error.length"))),
        "lastName" -> text("declarantName.lastName.error.required")
          .verifying(firstError(maxLength(255, "declarantName.lastName.error.length")))
      )(Name.apply)(Name.unapply)
    )

}
