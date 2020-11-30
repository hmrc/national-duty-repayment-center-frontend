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

package forms

import javax.inject.Inject
import forms.mappings.Mappings
import models.NoOfEntries
import play.api.data.Form

class HowManyEntriesFormProvider @Inject() extends Mappings {

  def apply(): Form[NoOfEntries] =
    Form(
      "value" -> text("howManyEntries.error.required")
        .verifying(maxLength(2, "howManyEntries.error.length")).transform[NoOfEntries](NoOfEntries.apply, _.value)
    )
}
