/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.Inject
import models.{FileVerificationStatus, SessionState}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{Call, Result}
import play.mvc.Http.HeaderNames
import repositories.SessionRepository
import services.{FileUploadState, FileUploaded}

import scala.concurrent.{ExecutionContext, Future}

case class FileUploadException(message: String) extends RuntimeException(message)

case class FileUploadUtils @Inject() (sessionRepository: SessionRepository)(implicit val ec: ExecutionContext) {

  def applyTransition(f: ConvertStateApi, s: FileUploadState, ss: SessionState): Future[FileUploadState] =
    for {
      newState <- f(s)
      res      <- sessionRepository.updateSession(newState, ss.userAnswers)
      if res
    } yield newState

}

object FileUploadUtils {

  private val logger = Logger(this.getClass)

  def fileStateErrror = throw FileUploadException("File upload state error")
  type ConvertStateApi = (FileUploadState) => Future[FileUploadState]

  def redirectFileStateMissing(message: String, call: Call) = {
    logger.warn("Missing FileUploadState") // for PD alerts
    logger.info(s"FileUploadState was missing performing $message")
    Redirect(call)
  }

  def acknowledgeFileUploadRedirect(state: FileUploadState): Result =
    (state match {
      case _: FileUploaded => Created
      case _               => NoContent
    }).withHeaders(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*")

  def renderFileVerificationStatus(reference: String, state: Option[FileUploadState]): Result =
    state match {
      case Some(s: FileUploadState) =>
        s.fileUploads.files.find(_.reference == reference) match {
          case Some(f) => Ok(Json.toJson(FileVerificationStatus(f)))
          case None    => NotFound
        }
      case _ => NotFound
    }

}
