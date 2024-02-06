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
import models.{FileType, UpscanNotification}
import play.api.mvc.{Action, Result}
import repositories.SessionRepository
import services.FileUploadService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanCallBackService @Inject() (
  sessionRepository: SessionRepository,
  val fileUtils: FileUploadUtils
)(implicit ec: ExecutionContext)
    extends FileUploadService {

  def upscanCallBack(id: String, fileType: FileType, notification: UpscanNotification): Future[Result] =
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
