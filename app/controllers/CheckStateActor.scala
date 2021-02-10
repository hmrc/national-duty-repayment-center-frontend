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

package controllers

import akka.actor.Actor
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import config.FrontendAppConfig
import models.FileUploadError
import play.api.Logger.logger
import play.api.mvc.Call
import repositories.SessionRepository
import services.{FileUploadService, FileUploadState, FileUploaded, UploadFile}
import uk.gov.hmrc.govukfrontend.views.viewmodels.fileupload.FileUpload
import uk.gov.hmrc.http.HttpVerbs.{GET, POST}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

case class CheckState(id: String, exitTime: LocalDateTime,state: FileUploadState)

class CheckStateActor @Inject()(sessionRepository: SessionRepository, appConfig: FrontendAppConfig)(implicit ec: ExecutionContext) extends Actor with FileUploadService {
  implicit val timeout = Timeout(30 seconds)
  override def receive: Receive = {
    case CheckState(id, exitTime, state) => {
      logger.info(s"state $state")
      if (LocalDateTime.now().isAfter(exitTime) || state.isInstanceOf[FileUploaded]) {
        logger.info("exiting.........")
        Future.successful(state).pipeTo(sender)
      }
      else {
        logger.info("continue.........")
        sessionRepository.get(id).flatMap(ss => ss.flatMap(_.fileUploadState) match {
          case Some(s@FileUploaded(_, _)) => {
            logger.info("found")
            Future.successful(s)
          }
          case Some(s@UploadFile(reference, uploadRequest, fileUploads, maybeUploadError)) => {
            if(s.maybeUploadError.nonEmpty) {
              Future.successful(s)
            } else
            (self ? CheckState(id, exitTime, s))
          }
        }).pipeTo(sender)
      }
    }
  }
}