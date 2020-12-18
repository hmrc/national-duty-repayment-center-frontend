package models

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.{JsError, JsString, Json}

class BulkFileUploadSpec extends WordSpec with MustMatchers with ScalaCheckPropertyChecks with OptionValues  {

  "BulkFileUpload" must {

    "deserialise valid values" in {

      val gen = Gen.oneOf(BulkFileUpload.values)

      forAll(gen) {
        bulkFileUpload =>

          JsString(bulkFileUpload.toString).validate[BulkFileUpload].asOpt.value mustEqual bulkFileUpload
      }
    }

    "fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!BulkFileUpload.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[BulkFileUpload] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {

      val gen = Gen.oneOf(BulkFileUpload.values)

      forAll(gen) {
        bulkFileUpload =>

          Json.toJson(bulkFileUpload) mustEqual JsString(bulkFileUpload.toString)
      }
    }
  }
}
