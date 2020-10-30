package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class customsDutyPaidFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "customsDutyPaid.error.required"
  val lengthKey = "customsDutyPaid.error.length"
  val maxLength = 14

  val form = new customsDutyPaidFormProvider()()

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
