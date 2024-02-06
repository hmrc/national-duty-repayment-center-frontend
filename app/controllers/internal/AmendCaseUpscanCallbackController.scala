/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.internal

import models.FileType.SupportingEvidence
import models.UpscanNotification
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AmendCaseUpscanCallbackController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  upscanCallBackService: UpscanCallBackService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  // POST /callback-from-upscan/ndrc/:id
  final def callbackFromUpscan(id: String): Action[UpscanNotification] =
    Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>
      upscanCallBackService.upscanCallBack(id, SupportingEvidence, request.body)
    }

}
