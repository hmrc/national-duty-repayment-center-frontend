package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.{JsError, JsString, Json}

class ContactByEmailSpec extends WordSpec with MustMatchers with ScalaCheckPropertyChecks with OptionValues {

  "ContactByEmail" must {

    "deserialise valid values" in {

      val gen = Gen.oneOf(ContactByEmail.values.toSeq)

      forAll(gen) {
        contactByEmail =>

          JsString(contactByEmail.toString).validate[ContactByEmail].asOpt.value mustEqual contactByEmail
      }
    }

    "fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ContactByEmail.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ContactByEmail] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {

      val gen = Gen.oneOf(ContactByEmail.values.toSeq)

      forAll(gen) {
        contactByEmail =>

          Json.toJson(contactByEmail) mustEqual JsString(contactByEmail.toString)
      }
    }
  }
}
