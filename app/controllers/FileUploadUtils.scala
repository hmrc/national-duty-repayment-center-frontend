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

import controllers.FileUploadUtils.ConvertStateApi
import models.{FileVerificationStatus, SessionState}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{Request, Result}
import play.mvc.Http.HeaderNames
import repositories.SessionRepository
import services.{FileUploadState, FileUploaded}

import javax.inject.Inject
import scala.concurrent.Future

case class FileUploadException(message: String) extends RuntimeException(message)

case class FileUploadUtils @Inject()(sessionRepository: SessionRepository) {

  implicit val ec = scala.concurrent.ExecutionContext.global

  def applyTransition(f: ConvertStateApi, s: FileUploadState,  ss: SessionState):  Future[FileUploadState] = {
    for {
      newState <- f(s)
      res <- sessionRepository.updateSession(newState, ss.userAnswers)
      if res
    } yield newState
  }
}

object FileUploadUtils {

  def fileStateErrror = throw FileUploadException("File upload state error")
  type ConvertStateApi = (FileUploadState) => Future[FileUploadState]

  def acknowledgeFileUploadRedirect(state: FileUploadState)(
    implicit request: Request[_]
  ): Result =
    (state match {
      case _: FileUploaded => Created
      case _ => NoContent
    }).withHeaders(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*")


  def renderFileVerificationStatus(
                                    reference: String, state: Option[FileUploadState])(implicit request: Request[_]
                                  ): Result = {

    state match {
      case Some(s: FileUploadState) =>
        s.fileUploads.files.find(_.reference == reference) match {
          case Some(f) => Ok(Json.toJson(FileVerificationStatus(f)))
          case None => NotFound
        }
      case _ => NotFound
    }
  }
}
