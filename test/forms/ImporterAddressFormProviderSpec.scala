package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ImporterAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "importerAddress.error.required"
  val lengthKey = "importerAddress.error.length"
  val maxLength = 9

  val form = new ImporterAddressFormProvider()()

  ".value" must {

    val fieldName = "value"

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
}
