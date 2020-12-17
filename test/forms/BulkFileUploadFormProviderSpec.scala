package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.BulkFileUpload
import play.api.data.FormError

class BulkFileUploadFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new BulkFileUploadFormProvider()()

  ".value" must {

    val fieldName = "value"
    val requiredKey = "bulkFileUpload.error.required"

    behave like checkboxField[BulkFileUpload](
      form,
      fieldName,
      validValues  = BulkFileUpload.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
