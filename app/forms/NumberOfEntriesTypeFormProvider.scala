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

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.{Form, Forms}
import models.{Entries, NumberOfEntriesType}
import play.api.data.Forms.{mapping, optional}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

class NumberOfEntriesTypeFormProvider @Inject() extends Mappings {

  def apply(): Form[Entries] =
    Form(mapping(
      "value" -> enumerable[NumberOfEntriesType]("numberOfEntriesType.error.required"),
      "entries" ->
        mandatoryIfEqual("value","02",
          decimal("howManyEntries.error.required", "howManyEntries.error.length")
            .verifying(
              regexp(Validation.numberOfEntries, "howManyEntries.error.length")))
    )(Entries.apply)(en => Some(en.numberOfEntriesType, en.entries)))

}