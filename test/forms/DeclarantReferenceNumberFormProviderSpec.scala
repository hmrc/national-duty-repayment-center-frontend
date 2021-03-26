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

    behave like mandatoryField(
      form.bind(Map(radioFieldName -> "02")),
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    //    "fail to bind entries with characters" in {
    //      val results = List(
    //        form.bind(Map(radioFieldName -> "02")).bind(Map(fieldName -> "1")).apply(fieldName),
    //        form.bind(Map(radioFieldName -> "02")).bind(Map(fieldName -> (maxLength+1).toString)).apply(fieldName)
    //      )
    //      val expectedError = FormError(fieldName, requiredKey , Seq())
    //      results.foreach {
    //        result =>
    //          result.errors shouldEqual Seq(expectedError)
    //      }
    //    }
    //
    //    "fail to bind a value" in {
    //      val result = form.bind(Map(radioFieldName -> "02")).bind(Map(fieldName -> "")).apply(fieldName)
    //      val expectedError = error(fieldName, requiredKey)
    //
    //      result.errors shouldEqual(expectedError)
    //    }
  }
}
