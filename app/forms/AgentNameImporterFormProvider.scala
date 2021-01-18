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
import models.UserName
import play.api.data.Form
import play.api.data.Forms._

class AgentNameImporterFormProvider @Inject() extends Mappings {

  def apply(): Form[UserName] =
    Form(
        mapping (
        "firstName" -> text("agentNameImporter.error.required.firstName")
          .verifying(firstError(
            maxLength(512,
              "agentNameImporter.error.length")
          )),
        "lastName" -> text("agentNameImporter.error.required.lastName")
          .verifying(firstError(
            maxLength(512,
              "agentNameImporter.error.length")
          ))
        )(UserName.apply)(UserName.unapply))

}
