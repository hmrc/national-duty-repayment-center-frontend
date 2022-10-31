/*
 * Copyright 2022 HM Revenue & Customs
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

import data.TestData
import forms.behaviours.StringFieldBehaviours
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.{Form, FormError}

class ImporterManualAddressFormProviderSpec extends StringFieldBehaviours {

  val maxLength = 128

  implicit val countriesService = TestData.testCountryService
  val form: Form[_]             = new ImporterManualAddressFormProvider().apply()

  ".AddressLine1" must {

    val fieldName   = "AddressLine1"
    val requiredKey = "importerAddress.line1.error.required"
    val lengthKey   = "importerAddress.line1.error.length"
    val maxLength   = 128

    behave like fieldThatBindsValidData(form, fieldName, safeInputsWithMaxLength(maxLength))

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredKey))
  }

  ".AddressLine2" must {

    val fieldName = "AddressLine2"
    val lengthKey = "importerAddress.line2.error.length"
    val maxLength = 128

    behave like fieldThatBindsValidData(form, fieldName, safeInputsWithMaxLength(maxLength))

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like optionalField(form, fieldName)
  }

  ".City" must {

    val fieldName   = "City"
    val requiredKey = "importerAddress.city.error.required"
    val lengthKey   = "importerAddress.city.error.length"
    val maxLength   = 64

    behave like fieldThatBindsValidData(form, fieldName, safeInputsWithMaxLength(maxLength))

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredKey))
  }

  ".Region" must {

    val fieldName = "Region"
    val lengthKey = "importerAddress.region.error.length"
    val maxLength = 64

    behave like fieldThatBindsValidData(form, fieldName, safeInputsWithMaxLength(maxLength))

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }

  ".CountryCode" must {

    val fieldName   = "CountryCode"
    val requiredKey = "importerAddress.countryCode.error.required"
    val lengthKey   = "importerAddress.countryCode.error.length"
    val maxLength   = 2

    behave like fieldThatBindsValidData(form, fieldName, safeInputsWithMaxLength(maxLength))

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredKey))

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

  }

  ".PostalCode" must {

    val fieldName   = "PostalCode"
    val maxLength   = 10
    val minLength   = 2
    val requiredKey = "postcode.error.required"

    behave like fieldThatBindsValidData(form, fieldName, stringsWithMinAndMaxLength(minLength, maxLength))

    "not error on missing postcode for non-UK countries" in {
      val result = form.bind(Map("CountryCode" -> "FR")).apply(fieldName)
      result.errors shouldBe Seq.empty
    }

    "error on missing postcode for UK countries" in {
      val result = form.bind(Map("CountryCode" -> "GB")).apply(fieldName)
      result.errors shouldBe Seq(FormError(fieldName, Seq(requiredKey)))
    }

    "not error with UK postcode" in {
      val result = form.bind(Map("CountryCode" -> "GB", fieldName -> "HG12DG")).apply(fieldName)
      result.errors shouldBe Seq.empty
    }
  }

}
