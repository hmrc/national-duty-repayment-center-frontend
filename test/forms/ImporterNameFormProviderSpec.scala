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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ImporterNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "importerName.error.required.firstName"
  val lastNameKey = "importerName.error.required.lastName"
  val lengthKey = "importerName.error.length"
  val maxLength = 512

  val form = new ImporterNameFormProvider()()

  ".firstName" must {

    val fieldName = "firstName"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".lastName" must {

    val lastName = "lastName"

    behave like fieldThatBindsValidData(
      form,
      lastName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      lastName,
      maxLength = maxLength,
      lengthError = FormError(lastName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      lastName,
      requiredError = FormError(lastName, lastNameKey)
    )
  }
}
