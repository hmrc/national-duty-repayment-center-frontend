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

package models.eis

import com.google.inject.ImplementedBy
import models.eis.QuoteFormatterImpl.illegalRegex

import scala.util.matching.Regex

@ImplementedBy(classOf[QuoteFormatterImpl])
trait QuoteFormatter {

  def format(value: String): String

}

class QuoteFormatterImpl extends QuoteFormatter {

  override def format(value: String): String = illegalRegex.findFirstIn(value).fold(value)(_ => s"[$value]")
}

object QuoteFormatterImpl {
  val illegalRegex: Regex = """^\s*[\"'`;]|[\"'`]\s*$""".r
}
