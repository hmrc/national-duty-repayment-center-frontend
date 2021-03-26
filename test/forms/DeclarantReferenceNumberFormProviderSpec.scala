package forms

import forms.behaviours.{OptionFieldBehaviours, StringFieldBehaviours}
import models.{DeclarantReferenceType, DeclarantReferenceNumber}
import play.api.data.FormError

class DeclarantReferenceNumberFormProviderSpec extends OptionFieldBehaviours with StringFieldBehaviours {

  val form = new DeclarantReferenceNumberFormProvider()()

  val requiredKey = "declarantReferenceNumber.error.required"
  val radioFieldName = "value"

  ".value" must {

    val requiredKey = "declarantReferenceNumber.error.required"

    behave like optionsField[DeclarantReferenceNumber](
      form,
      radioFieldName,
      validValues  = Seq(DeclarantReferenceNumber.apply(DeclarantReferenceType.Yes,None),
        DeclarantReferenceNumber.apply(DeclarantReferenceType.No,Some(stringsWithMaxLength(50).toString))),
      invalidError = FormError(radioFieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      radioFieldName,
      requiredError = FormError(radioFieldName, requiredKey)
    )
  }

  ".declarantReferenceNumber" must {

    val fieldName = "declarantReferenceNumber"
    val requiredKey = "declarantReferenceNumber.error.required"
    val lengthKey = "declarantReferenceNumber.error.invalid"
    val maxLength = 50
    val minLength = 1

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMinAndMaxLength(minLength,maxLength)
    )
  }
}
