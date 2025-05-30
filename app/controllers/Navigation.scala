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

package controllers

import models.UserAnswers
import navigation.{Navigator, NavigatorBack}
import pages.Page
import play.api.mvc.Call

trait Navigation[T <: UserAnswers] {
  val navigator: Navigator[T]
  val page: Page

  def nextPage: T => Call = navigator.nextPage(page, _)

  def backLink: T => NavigatorBack = navigator.previousPage(page, _)
}
