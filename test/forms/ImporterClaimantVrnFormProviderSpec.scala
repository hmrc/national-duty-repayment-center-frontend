package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ImporterClaimantVrnFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "importerClaimantVrn.error.required"
  val lengthKey = "importerClaimantVrn.error.length"
  val maxLength = 9

  val form = new ImporterClaimantVrnFormProvider()()

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
