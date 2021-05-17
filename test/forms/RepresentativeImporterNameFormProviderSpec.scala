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

class RepresentativeImporterNameFormProviderSpec extends StringFieldBehaviours {

  val requiredNameNameKey = "representative.importer.importerName.error.required"
  val lengthFirstNameKey = "representative.importer.importerName.error.length"
  val invalidKey = "representative.importer.importerName.error.invalid"
  val maxLength = 512

  val form = new RepresentativeImporterNameFormProvider()()

  ".importerName" must {

    val fieldName = "importerName"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLengthAlpha(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthFirstNameKey, Seq(maxLength))
    )

    behave like fieldThatPreventsUnsafeInput(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.safeInputPattern))
    )
  }
}
