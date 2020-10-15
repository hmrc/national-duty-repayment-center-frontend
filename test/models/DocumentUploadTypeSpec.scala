/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.{JsError, JsString, Json}

import models.DocumentUploadType

class DocumentUploadTypeSpec extends WordSpec with MustMatchers with ScalaCheckPropertyChecks with OptionValues {

  "DocumentUploadType" must {
    "serialise" in {

      val gen = Gen.oneOf(DocumentUploadType.values)

      forAll(gen) {
        documentUploadType =>

          Json.toJson(documentUploadType) mustEqual JsString(documentUploadType.toString)
      }
    }

    "deserialise valid values" in {

      val gen = Gen.oneOf(DocumentUploadType.values)

      forAll(gen) {
        documentUploadType =>

          JsString(documentUploadType.toString).validate[DocumentUploadType].asOpt.value mustEqual documentUploadType
      }
    }

    "fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!DocumentUploadType.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[DocumentUploadType] mustEqual JsError("error.invalid")
      }
    }
  }

}
