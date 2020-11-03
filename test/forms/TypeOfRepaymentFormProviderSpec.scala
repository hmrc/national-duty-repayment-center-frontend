package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.TypeOfRepayment
import play.api.data.FormError

class TypeOfRepaymentFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new TypeOfRepaymentFormProvider()()

  ".value" must {

    val fieldName = "value"
    val requiredKey = "typeOfRepayment.error.required"

    behave like checkboxField[TypeOfRepayment](
      form,
      fieldName,
      validValues  = TypeOfRepayment.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
