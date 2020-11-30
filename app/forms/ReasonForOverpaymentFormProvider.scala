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
import models.ClaimDescription
import play.api.data.Form

class ReasonForOverpaymentFormProvider @Inject() extends Mappings {

  def apply(): Form[ClaimDescription] =
    Form(
      "value" -> text("reasonForOverpayment.error.required")
        .verifying(maxLength(750, "reasonForOverpayment.error.length")).transform[ClaimDescription](ClaimDescription.apply, _.value)
    )
}
