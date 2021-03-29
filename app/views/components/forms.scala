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

package views.components

import javax.inject.{Inject, Singleton}

@Singleton
class forms @Inject() (
  val formWithCSRF: uk.gov.hmrc.govukfrontend.views.html.helpers.formWithCSRF,
  val fieldset: views.html.components.fieldset,
//  val errorSummary: views.html.components.errorSummary,
    val inputText: views.html.components.inputText,
  val inputNumber: views.html.components.inputNumber,
  val inputHidden: views.html.components.inputHidden,
  val inputDate: views.html.components.inputDate,
  val inputCheckboxes: views.html.components.inputCheckboxes,
  val inputRadio: views.html.components.inputRadio,
  val yesNoRadio: views.html.components.yesNoRadio,
  val inputSelect: views.html.components.inputSelect,
//  val inputTime: views.html.components.inputTime,
  val textarea: views.html.components.textarea,
  val errorSummary: views.html.components.errorSummary
                      )
