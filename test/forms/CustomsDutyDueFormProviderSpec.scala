package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class CustomsDutyDueFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "customsDutyDue.error.required"
  val lengthKey = "customsDutyDue.error.length"
  val maxLength = 14

  val form = new CustomsDutyDueFormProvider()()

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
