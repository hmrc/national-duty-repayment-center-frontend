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

package views

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint

object ViewUtils {

  def renderEmpref(empref: String): String = empref.replace("-", "/")

  def title(form: Form[_], titleStr: String, section: Option[String] = None, titleMessageArgs: Seq[String] = Seq())
           (implicit messages: Messages): String = {
    titleNoForm(s"${errorPrefix(form)} ${messages(titleStr, titleMessageArgs:_*)}", section)
  }


  def titleNoForm(title: String, section: Option[String] = None, titleMessageArgs: Seq[String] = Seq())(implicit messages: Messages): String =
    s"${messages(title, titleMessageArgs:_*)} - ${section.fold("")(messages(_) + " - ")}${messages("site.service_name")} - ${messages("site.govuk")}"

  def errorPrefix(form: Form[_])(implicit messages: Messages): String = {
    if (form.hasErrors || form.hasGlobalErrors) messages("error.browser.title.prefix") else ""
  }
}
