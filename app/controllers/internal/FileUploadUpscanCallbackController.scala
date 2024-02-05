/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.FileUploadUtils
import controllers.FileUploadUtils._
import models.FileType.SupportingEvidence
import models.UpscanNotification
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import services._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileUploadUpscanCallbackController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  val fileUtils: FileUploadUtils
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with FileUploadService {

  // POST /callback-from-upscan/ndrc/:id
  final def callbackFromUpscan(id: String): Action[UpscanNotification] =
    Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>
      sessionRepository.getFileUploadState(id).flatMap { ss =>
        ss.state match {
          case Some(s) =>
            fileUtils.applyTransition(upscanCallbackArrived(request.body, SupportingEvidence)(_), s, ss).map(newState =>
              acknowledgeFileUploadRedirect(newState)
            )
          case None => Future.successful(fileStateErrror)
        }
      }
    }

}
