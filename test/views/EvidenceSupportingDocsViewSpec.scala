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

package views

import forms.EvidenceSupportingDocsFormProvider
import models.{EvidenceSupportingDocs, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.CheckboxViewBehaviours
import views.html.EvidenceSupportingDocsView

class EvidenceSupportingDocsViewSpec extends CheckboxViewBehaviours[EvidenceSupportingDocs] {

  val messageKeyPrefix = "evidenceSupportingDocs"

  val form = new EvidenceSupportingDocsFormProvider()()

  "EvidenceSupportingDocsView" must {

    val view = viewFor[EvidenceSupportingDocsView](Some(emptyUserAnswers))

    def applyView(form: Form[Set[EvidenceSupportingDocs]]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like checkboxPage(form, applyView, messageKeyPrefix, EvidenceSupportingDocs.options)
  }
}
