/*
 * Copyright 2022 HM Revenue & Customs
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

package models.eis

import base.SpecBase
import org.scalatest.MustMatchers

class QuoteFormatterSpec extends SpecBase with MustMatchers {

  val quoteProtection = injector.instanceOf[QuoteFormatter]

  "QuoteTransformer" must {

    "not transform value" when {

      def doesNotTransform(value: String) = quoteProtection.format(value) mustEqual value

      "value has no illegal characters" in {
        doesNotTransform("Value without illegal characters")
      }
      "value has double quotes in middle" in {
        doesNotTransform("""Must "retain" value""")
      }
      "value has single quotes in middle" in {
        doesNotTransform("""Must 'retain' value""")
      }
      "value has back-tick in middle" in {
        doesNotTransform("""Must `retain` value""")
      }
      "value has semi-colon in middle" in {
        doesNotTransform("""Must; retain value""")
      }
      "value ends with semi-colon" in {
        doesNotTransform("""Must retain value;""")
      }
      "value starts with space" in {
        doesNotTransform(" space at start")
      }
      "value ends with space" in {
        doesNotTransform("space at end ")
      }
    }

    "transform value" when {

      def doesTransform(value: String, expected: String) = quoteProtection.format(value) mustEqual expected

      "value starts with double quote" in {
        doesTransform(""""Transforms" this""", """["Transforms" this]""")
      }
      "value ends with double quote" in {
        doesTransform("""Transforms "this"""", """[Transforms "this"]""")
      }
      "value starts with single quote" in {
        doesTransform("""'Transforms' this""", """['Transforms' this]""")
      }
      "value ends with single quote" in {
        doesTransform("""Transforms 'this'""", """[Transforms 'this']""")
      }
      "value starts with back-tick" in {
        doesTransform("""`Transforms` this""", """[`Transforms` this]""")
      }
      "value ends with back-tick" in {
        doesTransform("""Transforms `this`""", """[Transforms `this`]""")
      }
      "value starts with semi-colon" in {
        doesTransform(""";Transforms this""", """[;Transforms this]""")
      }
      "value starts with space followed by illegal character" in {
        doesTransform(" 'test space' at beginning", "[ 'test space' at beginning]")
      }
      "value ends with illegal character followed by space" in {
        doesTransform("test space at `end` ", "[test space at `end` ]")
      }
    }

  }
}
