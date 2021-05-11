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

class RepresentativeDeclarantAndBusinessNameFormProviderSpec extends StringFieldBehaviours {

  val requiredDeclarantNameKey = "representative.declarantAndBusinessName.declarantName.error.required"
  val lengthDeclarantNameKey = "representative.declarantAndBusinessName.declarantName.error.length"
  val invalidDeclarantNameKey = "representative.declarantAndBusinessName.declarantName.error.invalid"

  val requiredAgentNameKey = "representative.declarantAndBusinessName.agentName.error.required"
  val lengthAgentNameKey = "representative.declarantAndBusinessName.agentName.error.length"
  val invalidAgentNameKey = "representative.declarantAndBusinessName.agentName.error.invalid"

  val maxLength = 512

  val form = new RepresentativeDeclarantAndBusinessNameFormProvider()()

  ".declarantName" must {

    val fieldName = "declarantName"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLengthAlpha(maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredDeclarantNameKey)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthDeclarantNameKey, Seq(maxLength))
    )

    behave like fieldThatPreventsUnsafeInput(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidDeclarantNameKey, Seq(Validation.safeInputPattern))
    )
  }

  ".agentName" must {

    val fieldName = "agentName"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLengthAlpha(maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredAgentNameKey)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthAgentNameKey, Seq(maxLength))
    )

    behave like fieldThatPreventsUnsafeInput(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidAgentNameKey, Seq(Validation.safeInputPattern))
    )
  }
}
