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

  val accountNumberPattern: Regex = "^[ -]*(?:\\d[ -]*){6,8}$".r.anchored
  val sortCodePattern: Regex = "^[ -]*(?:\\d[ -]*){6,6}$".r.anchored
  val eoriPattern: Regex = "((?:[Gg][Bb])?\\d{15})|((?:[Gg][Bb])?\\d{12})".r.anchored
  val safeInputPattern = """^[A-Za-z0-9À-ÿ \!\)\(.,_/’'"&-]+$"""
  val postcodeRegex = """^[ ]*[A-Za-z][ ]*[A-Za-z]{0,1}[ ]*[0-9][ ]*[0-9A-Za-z]{0,1}[ ]*[0-9][ ]*[A-Za-z][ ]*[A-Za-z][ ]*$"""
  val monetaryPattern: String = """^-?(\d*(\.\d{1,2})?)$"""
  val numberOfEntries = "^([2-9]|[0-9]{2,6})$"
  val epu = "^[0-9][0-9][0-9]$"
  val epuEntryNumber = "^([0-9]{6}[a-z|A-Z])$"

}
