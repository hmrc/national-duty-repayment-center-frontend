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
import models.{DeclarantReferenceNumber, DeclarantReferenceType}
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

class DeclarantReferenceNumberFormProvider @Inject() extends Mappings {

  val minLength = 1
  val maxLength = 50

  def apply(): Form[DeclarantReferenceNumber] =
    Form(
      mapping(
        "value" -> enumerable[DeclarantReferenceType]("declarantReferenceNumber.error.required"),
        "declarantReferenceNumber" ->
          mandatoryIfEqual(
            "value",
            "01",
            text("declarantReferenceNumber.error.required.declarantReferenceNumber")
              .verifying(firstError(maxLength(maxLength, "declarantReferenceNumber.error.invalid.length")))
          )
      )(DeclarantReferenceNumber.apply)(en => Some((en.declarantReferenceType, en.declarantReferenceNumber)))
    )

}
