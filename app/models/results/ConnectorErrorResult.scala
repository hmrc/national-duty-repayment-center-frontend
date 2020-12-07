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

package models.results

sealed trait ConnectorErrorResult {
  val body: String
}

case object InvalidJson extends ConnectorErrorResult {
  override val body: String = "Invalid JSON received"
}

case object NotFound extends ConnectorErrorResult {
  override val body: String = "Not found"
}

case class UnexpectedResponseStatus(status: Int, body: String) extends ConnectorErrorResult

case class UnexpectedException(message: String) extends ConnectorErrorResult {
  override val body: String = message
}
