/*
 * Copyright 2025 HM Revenue & Customs
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

package views

import base.SpecBase
import models.UpscanNotification.{FailureDetails, QUARANTINE, REJECTED, UNKNOWN}
import models.{
  DuplicateFileUpload,
  FileTransmissionFailed,
  FileTransmissionTimedOut,
  FileVerificationFailed,
  S3UploadError
}
import play.api.data.FormError

class UploadFileViewContextSpec extends SpecBase {

  val context = injector.instanceOf[UploadFileViewContext]

  "UploadFileViewContext" must {

    "convert toFormError" when {

      "error is FileTransmissionFailed" in {

        context.toFormError(FileTransmissionFailed(S3UploadError("key", "errorCode", "errorMessage"))) mustBe FormError(
          "file",
          "error.file-upload.unknown"
        )
        context.toFormError(
          FileTransmissionFailed(S3UploadError("key", "MissingFile", "errorMessage"))
        ) mustBe FormError("file", "error.file-upload.required")
        context.toFormError(
          FileTransmissionFailed(S3UploadError("key", "InternalError", "errorMessage"))
        ) mustBe FormError("file", "error.file-upload.try-again")
        context.toFormError(
          FileTransmissionFailed(S3UploadError("key", "EntityTooLarge", "errorMessage"))
        ) mustBe FormError("file", "error.file-upload.invalid-size-large")
        context.toFormError(
          FileTransmissionFailed(S3UploadError("key", "EntityTooSmall", "errorMessage"))
        ) mustBe FormError("file", "error.file-upload.invalid-size-small")
      }

      "error is FileVerificationFailed" in {

        context.toFormError(FileVerificationFailed(FailureDetails(QUARANTINE, "message"))) mustBe FormError(
          "file",
          "error.file-upload.quarantine"
        )
        context.toFormError(FileVerificationFailed(FailureDetails(REJECTED, "message"))) mustBe FormError(
          "file",
          "error.file-upload.invalid-type"
        )
        context.toFormError(FileVerificationFailed(FailureDetails(UNKNOWN, "message"))) mustBe FormError(
          "file",
          "error.file-upload.unknown"
        )

      }

      "error is DuplicateFileUpload" in {

        context.toFormError(DuplicateFileUpload("checksum", "existingFile", "duplicateFile")) mustBe FormError(
          "file",
          "error.file-upload.duplicate"
        )

      }

      "error is FileTransmissionTimedOut" in {

        context.toFormError(FileTransmissionTimedOut("reference")) mustBe FormError("file", "error.file-upload.timeout")

      }
    }

  }
}
