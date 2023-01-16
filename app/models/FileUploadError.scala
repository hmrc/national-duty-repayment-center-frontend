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

package models

import play.api.libs.json.Json

sealed trait FileUploadError

case class FileTransmissionFailed(error: S3UploadError)                       extends FileUploadError
case class FileTransmissionTimedOut(ref: String)                              extends FileUploadError
case class FileVerificationFailed(details: UpscanNotification.FailureDetails) extends FileUploadError

case class DuplicateFileUpload(checksum: String, existingFileName: String, duplicateFileName: String)
    extends FileUploadError

object FileUploadError extends SealedTraitFormats[FileUploadError] {

  override val formats = Set(
    Case[FileTransmissionFailed](Json.format[FileTransmissionFailed]),
    Case[FileTransmissionTimedOut](Json.format[FileTransmissionTimedOut]),
    Case[FileVerificationFailed](Json.format[FileVerificationFailed]),
    Case[DuplicateFileUpload](Json.format[DuplicateFileUpload])
  )

}
