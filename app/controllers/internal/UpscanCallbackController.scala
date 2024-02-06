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

import controllers.FileUploadUtils
import controllers.FileUploadUtils._
import models.FileType.{Bulk, ProofOfAuthority, SupportingEvidence}
import models.{FileType, UpscanNotification}
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.SessionRepository
import services.FileUploadService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanCallbackController @Inject() (
  sessionRepository: SessionRepository,
  val fileUtils: FileUploadUtils,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with FileUploadService {

  // POST /callback-from-upscan/bulk/:id
  final def bulkUploadCallbackFromUpscan(id: String): Action[UpscanNotification] =
    Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>
      upscanCallBack(id, Bulk, request.body)
    }

  // POST /callback-from-upscan/ndrc/:id
  final def fileUploadCallbackFromUpscan(id: String): Action[UpscanNotification] =
    Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>
      upscanCallBack(id, SupportingEvidence, request.body)
    }

  // POST /callback-from-upscan/amend/ndrc/:id
  final def amendFileUploadCallbackFromUpscan(id: String): Action[UpscanNotification] =
    Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>
      upscanCallBack(id, SupportingEvidence, request.body)
    }

  // POST  /callback-from-upscan/upload-proof-of-authority/:id
  final def proofOfAuthorityCallbackFromUpscan(id: String): Action[UpscanNotification] =
    Action.async(parse.json.map(_.as[UpscanNotification])) { implicit request =>
      upscanCallBack(id, ProofOfAuthority, request.body)
    }

  private def upscanCallBack(id: String, fileType: FileType, notification: UpscanNotification): Future[Result] =
    sessionRepository.getFileUploadState(id).flatMap { ss =>
      ss.state match {
        case Some(s) =>
          fileUtils.applyTransition(upscanCallbackArrived(notification, fileType)(_), s, ss).map(newState =>
            acknowledgeFileUploadRedirect(newState)
          )
        case None => Future.successful(fileStateErrror)
      }
    }

}
