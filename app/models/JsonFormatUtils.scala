/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json._

import scala.util.{Failure, Success, Try}

object JsonFormatUtils {
  def stringFormat[A](fromString: String => A)(makeString: A => String): Format[A] = new Format[A] {
    def reads(json: JsValue): JsResult[A] = json match {
      case JsString(str) => JsSuccess(fromString(str))
      case _ => JsError(s"Expected JSON string type")
    }

    def writes(o: A): JsValue = JsString(makeString(o))
  }

  def intFormat[A](fromInt: Int => A)(makeInt: A => Int): Format[A] = new Format[A] {
    def reads(json: JsValue): JsResult[A] = json match {
      case JsNumber(num) => Try(num.toIntExact) match {
        case Failure(_) => JsError("Expected number to be an integer")
        case Success(value) => JsSuccess(fromInt(value))
      }
      case _ => JsError("Expected JSON number type")
    }

    def writes(o: A): JsValue = JsNumber(makeInt(o))
  }

  def longFormat[A](fromLong: Long => A)(makeLong: A => Long): Format[A] = new Format[A] {
    def reads(json: JsValue): JsResult[A] = json match {
      case JsNumber(num) => Try(num.toLongExact) match {
        case Failure(_) => JsError("Expected number to be a long")
        case Success(value) => JsSuccess(fromLong(value))
      }
      case _ => JsError("Expected JSON number type")
    }

    def writes(o: A): JsValue = JsNumber(makeLong(o))
  }
}
