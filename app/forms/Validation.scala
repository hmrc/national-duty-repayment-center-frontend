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

package forms

import scala.util.matching.Regex

object Validation {

  val accountNumberPattern: String = "^[0-9]{6,8}$"
  val sortCodePattern: String      = "^[0-9]{6}$"
  val eoriPattern: Regex           = "((?:[Gg][Bb])\\d{15})|((?:[Gg][Bb])\\d{12})".r.anchored
  val phoneNumberPattern           = """^[0]{1}[0-9]{10}"""
  val monetaryPattern: String      = """^-?(\d*(\.\d{1,2})?)$"""
  val numberOfEntries              = "^([2-9]|[0-9]{2,6})$"
  val epu                          = "^[0-9][0-9][0-9]$"
  val epuEntryNumber               = "^([0-9]{6}[a-z|A-Z])$"

  val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,85}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,85}[a-zA-Z0-9])?)*$"""

  val referenceNumberPattern = """^[a-zA-Z0-9]*$"""
}
