/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json._

object EnumerationFormatSpec {

  sealed trait Foo

  object Foo extends EnumerationFormats[Foo] {
    case object Bar extends Foo
    case object Baz extends Foo

    val values: Set[Foo] = Set(Bar, Baz)
  }

}

class EnumerationFormatSpec extends AnyWordSpec with Matchers with EitherValues with OptionValues {

  import EnumerationFormatSpec._

  ".reads" must {
    Foo.values.foreach {
      value =>
        s"bind correctly for: $value" in {
          Json.fromJson[Foo](JsString(value.toString)).asEither mustBe Right(value)
        }
    }

    "fail to bind for invalid values" in {
      Json.fromJson[Foo](JsString("invalid")).asEither.left.value must contain(
        JsPath -> Seq(JsonValidationError("Unsupported enum key invalid, should be one of Bar,Baz"))
      )
    }

    "fail to bind for invalid type" in {
      Json.fromJson[Foo](JsNumber(123456)).asEither.left.value must contain(
        JsPath -> Seq(JsonValidationError("Expected json string but got JsNumber"))
      )
    }
  }

  ".writes" must {
    Foo.values.foreach {
      value =>
        s"write $value" in {
          Json.toJson(value) mustEqual JsString(value.toString)
        }
    }
  }
}
