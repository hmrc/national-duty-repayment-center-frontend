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

class AgentImporterManualAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "agentImporterManualAddress.error.required"
  val lengthKey = "agentImporterManualAddress.error.length"
  val maxLength = 128

  val form = new AgentImporterManualAddressFormProvider()()

  ".AddressLine1" must {

    val fieldName = "AddressLine1"
    val requiredKey = "agentImporterManualAddress.line1.error.required"
    val lengthKey = "agentImporterManualAddress.line1.error.length"
    val invalidKey = "agentImporterManualAddress.line1.error.invalid"
    val maxLength = 128

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeInputsWithMaxLength(maxLength)
    )

    behave like fieldThatPreventsUnsafeInput(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.safeInputPattern))
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

  ".AddressLine2" must {

    val fieldName = "AddressLine2"
    val lengthKey = "agentImporterManualAddress.line2.error.length"
    val invalidKey = "agentImporterManualAddress.line2.error.invalid"
    val maxLength = 128

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeInputsWithMaxLength(maxLength)
    )

    behave like fieldThatPreventsUnsafeInput(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.safeInputPattern))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like optionalField(
      form,
      fieldName
    )
  }

  ".City" must {

    val fieldName = "City"
    val requiredKey = "agentImporterManualAddress.city.error.required"
    val lengthKey = "agentImporterManualAddress.city.error.length"
    val invalidKey = "agentImporterManualAddress.city.error.invalid"
    val maxLength = 64

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeInputsWithMaxLength(maxLength)
    )

    behave like fieldThatPreventsUnsafeInput(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.safeInputPattern))
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

  ".Region" must {

    val fieldName = "Region"
    val lengthKey = "agentImporterManualAddress.region.error.length"
    val invalidKey = "agentImporterManualAddress.region.error.invalid"
    val maxLength = 64

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeInputsWithMaxLength(maxLength)
    )

    behave like fieldThatPreventsUnsafeInput(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.safeInputPattern))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }

  ".CountryCode" must {

    val fieldName = "CountryCode"
    val requiredKey = "agentImporterManualAddress.countryCode.error.required"
    val lengthKey = "agentImporterManualAddress.countryCode.error.length"
    val invalidKey = "agentImporterManualAddress.countryCode.error.invalid"
    val maxLength = 2

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeInputsWithMaxLength(maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldThatPreventsUnsafeInput(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.safeInputPattern))
    )
  }

  ".PostalCode" must {

    val fieldName = "PostalCode"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validPostcodes
    )

    behave like optionalField(
      form,
      fieldName
    )

    "not bind invalid postcodes" in {

      val expectedError = FormError(fieldName, "agentImporterManualAddress.postalCode.error.invalid", Seq(Validation.postcodeRegex))

      val results = List(
        form.bind(Map(fieldName -> "AA 1AA")).apply(fieldName),
        form.bind(Map(fieldName -> "AAA 1AA")).apply(fieldName),
        form.bind(Map(fieldName -> "AA111 1AA")).apply(fieldName),
        form.bind(Map(fieldName -> "AA1 AA")).apply(fieldName),
        form.bind(Map(fieldName -> "AA11 1A")).apply(fieldName),
        form.bind(Map(fieldName -> "AA1 1A1")).apply(fieldName),
        form.bind(Map(fieldName -> "1A1 1AA")).apply(fieldName)
      )

      results.foreach {
        result =>
          result.errors shouldEqual Seq(expectedError)
      }
    }
  }
}
