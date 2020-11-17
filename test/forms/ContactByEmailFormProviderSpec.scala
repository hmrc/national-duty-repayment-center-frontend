package forms

import forms.behaviours.OptionFieldBehaviours
import models.ContactByEmail
import play.api.data.FormError

class ContactByEmailFormProviderSpec extends OptionFieldBehaviours {

  val form = new ContactByEmailFormProvider()()

  ".value" must {

    val fieldName = "value"
    val requiredKey = "contactByEmail.error.required"

    behave like optionsField[ContactByEmail](
      form,
      fieldName,
      validValues  = ContactByEmail.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
