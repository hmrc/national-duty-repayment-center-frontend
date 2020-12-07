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

import forms.{AddressSelectionFormProvider, ImporterAddressFormProvider}
import models.{NormalMode, PostcodeLookup}
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem
import views.behaviours.ViewBehaviours
import views.html.ImporterAddressConfirmationView

class ImporterAddressConfirmationViewSpec extends ViewBehaviours {

  val form = new AddressSelectionFormProvider().apply()

  val lookup = new PostcodeLookup("ZZZZZZZ")
  val addresses = Seq(
    SelectItem(
      text = "Line1, TOWN, AA1 1AA",
      value = Some("""{"line1":"Line1","town":"TOWN","postCode":"AA1 1AA"}"""))
  )

  "ImporterAddressConfirmation view" must {

    val view = viewFor[ImporterAddressConfirmationView](Some(emptyUserAnswers))

    val applyView = view.apply(form, lookup, addresses, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView, "importerAddressConfirmation")

    behave like pageWithBackLink(applyView)
  }
}
