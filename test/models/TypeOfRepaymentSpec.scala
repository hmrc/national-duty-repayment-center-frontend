package models

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.{JsError, JsString, Json}

class TypeOfRepaymentSpec extends WordSpec with MustMatchers with ScalaCheckPropertyChecks with OptionValues with ModelGenerators {

  "TypeOfRepayment" must {

    "deserialise valid values" in {

      val gen = arbitrary[TypeOfRepayment]

      forAll(gen) {
        typeOfRepayment =>

          JsString(typeOfRepayment.toString).validate[TypeOfRepayment].asOpt.value mustEqual typeOfRepayment
      }
    }

    "fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!TypeOfRepayment.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[TypeOfRepayment] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {

      val gen = arbitrary[TypeOfRepayment]

      forAll(gen) {
        typeOfRepayment =>

          Json.toJson(typeOfRepayment) mustEqual JsString(typeOfRepayment.toString)
      }
    }
  }
}
