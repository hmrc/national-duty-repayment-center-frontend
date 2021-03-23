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

package service

import base.SpecBase
import models.FileType.{Bulk, SupportingEvidence}
import models.requests.UploadRequest
import models.{DuplicateFileUpload, FileTransmissionFailed, FileUpload, FileUploads, FileVerificationFailed, S3UploadError, UpscanFileFailed, UpscanFileReady, UpscanNotification}
import org.scalatest.{MustMatchers, OptionValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import services.{FileUploadService, FileUploaded, UploadFile}

import java.time.ZonedDateTime

class FileUploadServiceSpec extends SpecBase with MustMatchers with ScalaCheckPropertyChecks with OptionValues {

  val service = new FileUploadService {
  }
  "at state FileUpload" should {

    "change state to FileUploaded when upscan callback has returned and accepted already" in {

      val currentState =
        UploadFile(
          "foo-bar-ref-3",
          UploadRequest(
            href = "https://s3.bucket",
            fields = Map(
              "callbackUrl"     -> "https://foo.bar/callback",
              "successRedirect" -> "https://foo.bar/success",
              "errorRedirect"   -> "https://foo.bar/failure"
            )
          ),
          FileUploads(files =
            Seq(
              FileUpload.Posted(1, "foo-bar-ref-1"),
              FileUpload.Initiated(2, "foo-bar-ref-2"),
              FileUpload.Accepted(
                3,
                "foo-bar-ref-3",
                "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
                ZonedDateTime.parse("2018-04-24T09:30:00Z"),
                "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
                "test.pdf",
                "application/pdf",
                Some(SupportingEvidence)
              ),
              FileUpload.Failed(
                4,
                "foo-bar-ref-4",
                UpscanNotification.FailureDetails(UpscanNotification.REJECTED, "some failure reason")
              )
            )
          )
        )
      val expectedState = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Posted(1, "foo-bar-ref-1"),
            FileUpload.Initiated(2, "foo-bar-ref-2"),
            FileUpload.Accepted(
              3,
              "foo-bar-ref-3",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test.pdf",
              "application/pdf",
              Some(SupportingEvidence)
            ),
            FileUpload.Failed(
              4,
              "foo-bar-ref-4",
              UpscanNotification.FailureDetails(UpscanNotification.REJECTED, "some failure reason")
            )
          )
        )
      )
      val upscanReady = UpscanFileReady(
        reference = "foo-bar-ref-3",
        downloadUrl = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
        uploadDetails = UpscanNotification.UploadDetails(
          uploadTimestamp = ZonedDateTime.parse("2018-04-24T09:30:00Z"),
          checksum = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          fileName = "test.pdf",
          fileMimeType = "application/pdf"
        )
      )
      whenReady(service.upscanCallbackArrived(upscanReady, SupportingEvidence)(currentState)) {
        newState => assert(newState == expectedState)
      }
    }
    "change state to UploadFile when upscan file upload already rejected" in {
      val currentState =
        UploadFile(
          "foo-bar-ref-4",
          UploadRequest(
            href = "https://s3.bucket",
            fields = Map(
              "callbackUrl"     -> "https://foo.bar/callback",
              "successRedirect" -> "https://foo.bar/success",
              "errorRedirect"   -> "https://foo.bar/failure"
            )
          ),
          FileUploads(files =
            Seq(
              FileUpload.Posted(1, "foo-bar-ref-1"),
              FileUpload.Initiated(2, "foo-bar-ref-2"),
              FileUpload.Accepted(
                3,
                "foo-bar-ref-3",
                "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
                ZonedDateTime.parse("2018-04-24T09:30:00Z"),
                "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
                "test.pdf",
                "application/pdf",
                Some(SupportingEvidence)
              ),
              FileUpload.Failed(
                4,
                "foo-bar-ref-4",
                UpscanNotification.FailureDetails(UpscanNotification.REJECTED, "some failure reason")
              )
            )
          )
        )
      val expectedState = UploadFile(
        "foo-bar-ref-4",
        UploadRequest(
          href = "https://s3.bucket",
          fields = Map(
            "callbackUrl"     -> "https://foo.bar/callback",
            "successRedirect" -> "https://foo.bar/success",
            "errorRedirect"   -> "https://foo.bar/failure"
          )
        ),
        FileUploads(files =
          Seq(
            FileUpload.Posted(1, "foo-bar-ref-1"),
            FileUpload.Initiated(2, "foo-bar-ref-2"),
            FileUpload.Accepted(
              3,
              "foo-bar-ref-3",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test.pdf",
              "application/pdf",
              Some(SupportingEvidence)
            ),
            FileUpload.Failed(
              4,
              "foo-bar-ref-4",
              UpscanNotification.FailureDetails(UpscanNotification.REJECTED, "some failure reason")
            )
          )
        ),
        Some(
          FileVerificationFailed(
            UpscanNotification.FailureDetails(UpscanNotification.REJECTED, "some failure reason")
          )
        )
      )
      whenReady(service.upscanCallbackArrived(UpscanFileFailed(
        reference = "foo-bar-ref-4",
        failureDetails = UpscanNotification.FailureDetails(
          failureReason = UpscanNotification.REJECTED,
          message = "some failure reason"
        )
      ), SupportingEvidence)(currentState)) {
        newState => assert(newState == expectedState)
      }
    }
    "change state to FileUploaded when upscanCallbackArrived and accepted, and reference matches" in {
      val currentState =
        UploadFile(
          "foo-bar-ref-1",
          UploadRequest(
            href = "https://s3.bucket",
            fields = Map(
              "callbackUrl"     -> "https://foo.bar/callback",
              "successRedirect" -> "https://foo.bar/success",
              "errorRedirect"   -> "https://foo.bar/failure"
            )
          ),
          FileUploads(files = Seq(FileUpload.Initiated(1, "foo-bar-ref-1")))
        )

      val expectedState = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Accepted(
              1,
              "foo-bar-ref-1",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test.pdf",
              "application/pdf",
              Some(SupportingEvidence)
            )
          )
        )
      )
      val upscanReady = UpscanFileReady(
        reference = "foo-bar-ref-1",
        downloadUrl = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
        uploadDetails = UpscanNotification.UploadDetails(
          uploadTimestamp = ZonedDateTime.parse("2018-04-24T09:30:00Z"),
          checksum = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          fileName = "test.pdf",
          fileMimeType = "application/pdf"
        )
      )
      whenReady(service.upscanCallbackArrived(upscanReady, SupportingEvidence)(currentState)) {
        newState => assert(newState == expectedState)
      }
    }
    "change state to UploadFile when upscanCallbackArrived and accepted, and reference matches but upload is a duplicate" in {
      val currentState = UploadFile(
        "foo-bar-ref-1",
        UploadRequest(
          href = "https://s3.bucket",
          fields = Map(
            "callbackUrl"     -> "https://foo.bar/callback",
            "successRedirect" -> "https://foo.bar/success",
            "errorRedirect"   -> "https://foo.bar/failure"
          )
        ),
        FileUploads(files =
          Seq(
            FileUpload.Initiated(1, "foo-bar-ref-1"),
            FileUpload.Accepted(
              2,
              "foo-bar-ref-2",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2020-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test1.pdf",
              "application/pdf",
              Some(SupportingEvidence)

            )
          )
        )
      )
      val expectedState = UploadFile(
        "foo-bar-ref-1",
        UploadRequest(
          href = "https://s3.bucket",
          fields = Map(
            "callbackUrl"     -> "https://foo.bar/callback",
            "successRedirect" -> "https://foo.bar/success",
            "errorRedirect"   -> "https://foo.bar/failure"
          )
        ),
        FileUploads(files =
          Seq(
            FileUpload.Duplicate(
              1,
              "foo-bar-ref-1",
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test1.pdf",
              "test2.png",
              Some(SupportingEvidence)
            ),
            FileUpload.Accepted(
              2,
              "foo-bar-ref-2",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2020-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test1.pdf",
              "application/pdf",
              Some(SupportingEvidence)
            )
          )
        ),
        Some(
          DuplicateFileUpload(
            "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
            "test1.pdf",
            "test2.png"
          )
        )
      )
      val ready = UpscanFileReady(
        reference = "foo-bar-ref-1",
        downloadUrl = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
        uploadDetails = UpscanNotification.UploadDetails(
          uploadTimestamp = ZonedDateTime.parse("2020-04-24T09:32:13Z"),
          checksum = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          fileName = "test2.png",
          fileMimeType = "image/png"
        )
      )
      whenReady(service.upscanCallbackArrived(ready, SupportingEvidence)(currentState)) {
        newState => assert(newState == expectedState)
      }
    }
    "change state to UploadFile when upscanCallbackArrived and failed, and reference matches" in {
      val currentState = UploadFile(
        "foo-bar-ref-1",
        UploadRequest(
          href = "https://s3.bucket",
          fields = Map(
            "callbackUrl"     -> "https://foo.bar/callback",
            "successRedirect" -> "https://foo.bar/success",
            "errorRedirect"   -> "https://foo.bar/failure"
          )
        ),
        FileUploads(files = Seq(FileUpload.Initiated(1, "foo-bar-ref-1")))
      )
      val expectedState = UploadFile(
        "foo-bar-ref-1",
        UploadRequest(
          href = "https://s3.bucket",
          fields = Map(
            "callbackUrl"     -> "https://foo.bar/callback",
            "successRedirect" -> "https://foo.bar/success",
            "errorRedirect"   -> "https://foo.bar/failure"
          )
        ),
        FileUploads(files =
          Seq(
            FileUpload.Failed(
              1,
              "foo-bar-ref-1",
              UpscanNotification.FailureDetails(UpscanNotification.UNKNOWN, "e.g. This file has a virus")
            )
          )
        ),
        Some(
          FileVerificationFailed(
            UpscanNotification.FailureDetails(UpscanNotification.UNKNOWN, "e.g. This file has a virus")
          )
        )
      )
      whenReady(service.upscanCallbackArrived(UpscanFileFailed(
        reference = "foo-bar-ref-1",
        failureDetails = UpscanNotification.FailureDetails(
          failureReason = UpscanNotification.UNKNOWN,
          message = "e.g. This file has a virus"
        )
      ), SupportingEvidence)(currentState)) {
        newState => assert(newState == expectedState)
      }
    }
    "change state to UploadFile with error when fileUploadWasRejected" in {
      val currentState = UploadFile(
        "foo-bar-ref-1",
        UploadRequest(
          href = "https://s3.bucket",
          fields = Map(
            "callbackUrl"     -> "https://foo.bar/callback",
            "successRedirect" -> "https://foo.bar/success",
            "errorRedirect"   -> "https://foo.bar/failure"
          )
        ),
        FileUploads(files = Seq(FileUpload.Initiated(1, "foo-bar-ref-1")))
      )
      val expectedState = currentState.copy(
        fileUploads = FileUploads(files =
          Seq(
            FileUpload.Rejected(
              1,
              "foo-bar-ref-1",
              S3UploadError(
                key = "foo-bar-ref-1",
                errorCode = "a",
                errorMessage = "b",
                errorResource = Some("c"),
                errorRequestId = Some("d")
              )
            )
          )
        ),
        maybeUploadError = Some(
          FileTransmissionFailed(
            S3UploadError(
              key = "foo-bar-ref-1",
              errorCode = "a",
              errorMessage = "b",
              errorResource = Some("c"),
              errorRequestId = Some("d")
            )
          )
        )
      )
      val error =  S3UploadError(
        key = "foo-bar-ref-1",
        errorCode = "a",
        errorMessage = "b",
        errorResource = Some("c"),
        errorRequestId = Some("d")
      )
      val newState = service.fileUploadWasRejected(error)(currentState)
      assert(newState == expectedState)
    }
  }
  "when callback returns" should {

    "go to UploadFile when upscan callback and reference unknown" in {
      val currentState =
        UploadFile(
          "foo-bar-ref-2",
          UploadRequest(
            href = "https://s3.bucket",
            fields = Map(
              "callbackUrl"     -> "https://foo.bar/callback",
              "successRedirect" -> "https://foo.bar/success",
              "errorRedirect"   -> "https://foo.bar/failure"
            )
          ),
          FileUploads(files =
            Seq(
              FileUpload.Posted(1, "foo-bar-ref-1")
            )
          )
      )
      val expectedState = UploadFile(
        "foo-bar-ref-2",
        UploadRequest(
          href = "https://s3.bucket",
          fields = Map(
            "callbackUrl"     -> "https://foo.bar/callback",
            "successRedirect" -> "https://foo.bar/success",
            "errorRedirect"   -> "https://foo.bar/failure"
          )
        ),
        FileUploads(files =
          Seq(
            FileUpload.Posted(1, "foo-bar-ref-1")
          )
        )
      )
      val upscanReady = UpscanFileReady(
        reference = "",
        downloadUrl = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
        uploadDetails = UpscanNotification.UploadDetails(
          uploadTimestamp = ZonedDateTime.parse("2018-04-24T09:30:00Z"),
          checksum = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          fileName = "test.pdf",
          fileMimeType = "application/pdf"
        )
      )
      whenReady(service.upscanCallbackArrived(upscanReady, SupportingEvidence)(currentState)) {
        newState => assert(newState == expectedState)
      }
    }

    "go to FileUploaded when file already accepted" in {
          val currentState = UploadFile(
          "foo-bar-ref-1",
          UploadRequest(
            href = "https://s3.bucket",
            fields = Map(
              "callbackUrl"     -> "https://foo.bar/callback",
              "successRedirect" -> "https://foo.bar/success",
              "errorRedirect"   -> "https://foo.bar/failure"
            )
          ),
          FileUploads(files =
            Seq(
              FileUpload.Accepted(
                1,
                "foo-bar-ref-1",
                "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
                ZonedDateTime.parse("2018-04-24T09:30:00Z"),
                "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
                "test.pdf",
                "application/pdf",
                Some(SupportingEvidence))
            )
          )
        )
      val expectedState = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Accepted(
              1,
              "foo-bar-ref-1",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test.pdf",
              "application/pdf",
              Some(SupportingEvidence)
            )
          )
        )
      )
      val upscanReady = UpscanFileReady(
        reference = "foo-bar-ref-1",
        downloadUrl = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
        uploadDetails = UpscanNotification.UploadDetails(
          uploadTimestamp = ZonedDateTime.parse("2018-04-24T09:30:00Z"),
          checksum = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          fileName = "test.pdf",
          fileMimeType = "application/pdf"
        )
      )
      whenReady(service.upscanCallbackArrived(upscanReady, SupportingEvidence)(currentState)) {
        newState => assert(newState == expectedState)
      }
    }

    "go to FileUploaded when upscanCallbackArrived and accepted, and reference matches" in {
      val currentState =
        UploadFile(
          "foo-bar-ref-1",
          UploadRequest(
            href = "https://s3.bucket",
            fields = Map(
              "callbackUrl"     -> "https://foo.bar/callback",
              "successRedirect" -> "https://foo.bar/success",
              "errorRedirect"   -> "https://foo.bar/failure"
            )
          ),
          FileUploads(files = Seq(FileUpload.Posted(1, "foo-bar-ref-1")))
        )
      val expectedState = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Accepted(
              1,
              "foo-bar-ref-1",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test.pdf",
              "application/pdf",
              Some(SupportingEvidence)
            )
          )
        )
      )
      whenReady(service.upscanCallbackArrived(UpscanFileReady(
        reference = "foo-bar-ref-1",
        downloadUrl = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
        uploadDetails = UpscanNotification.UploadDetails(
          uploadTimestamp = ZonedDateTime.parse("2018-04-24T09:30:00Z"),
          checksum = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          fileName = "test.pdf",
          fileMimeType = "application/pdf"
        )
      ), SupportingEvidence)(currentState)) {
        newState => assert(newState == expectedState)
      }
    }

    "go to UploadFile when upscanCallbackArrived and failed, and reference matches" in {
      val currentState =
        UploadFile(
          "foo-bar-ref-1",
          UploadRequest(
            href = "https://s3.bucket",
            fields = Map(
              "callbackUrl"     -> "https://foo.bar/callback",
              "successRedirect" -> "https://foo.bar/success",
              "errorRedirect"   -> "https://foo.bar/failure"
            )
          ),
          FileUploads(files = Seq(FileUpload.Posted(1, "foo-bar-ref-1")))
        )
      val expectedState = UploadFile(
        "foo-bar-ref-1",
        UploadRequest(
          href = "https://s3.bucket",
          fields = Map(
            "callbackUrl"     -> "https://foo.bar/callback",
            "successRedirect" -> "https://foo.bar/success",
            "errorRedirect"   -> "https://foo.bar/failure"
          )
        ),
        FileUploads(files =
          Seq(
            FileUpload.Failed(
              1,
              "foo-bar-ref-1",
              UpscanNotification.FailureDetails(UpscanNotification.QUARANTINE, "e.g. This file has a virus")
            )
          )
        ),
        Some(
          FileVerificationFailed(
            UpscanNotification.FailureDetails(UpscanNotification.QUARANTINE, "e.g. This file has a virus")
          )
        )
      )

      whenReady(service.upscanCallbackArrived(UpscanFileFailed(
        reference = "foo-bar-ref-1",
        failureDetails = UpscanNotification.FailureDetails(
          failureReason = UpscanNotification.QUARANTINE,
          message = "e.g. This file has a virus"
        )
      ), SupportingEvidence)(currentState)) {
        newState => assert(newState == expectedState)
      }
    }
  }
  "at state FileUploaded" should {
    "go to acknowledged FileUploaded when waitForFileVerification" in {
      val state = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Accepted(
              1,
              "foo-bar-ref-1",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test.pdf",
              "application/pdf"
            )
          )
        ),
        acknowledged = false
      )
      val upscanReady = UpscanFileReady(
        reference = "foo-bar-ref-1",
        downloadUrl = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
        uploadDetails = UpscanNotification.UploadDetails(
          uploadTimestamp = ZonedDateTime.parse("2018-04-24T09:30:00Z"),
          checksum = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          fileName = "test.pdf",
          fileMimeType = "application/pdf"
        )
      )
      whenReady(service.upscanCallbackArrived(upscanReady, SupportingEvidence)(state)) {
        newState => assert(newState == state.copy(acknowledged = true))
      }
    }
  }
  "at multiple file uploads" should {
    "should replace an existing Bulk upload file in Accepted state" in {
      val currentState =
        UploadFile(
          "foo-bar-ref-2",
          UploadRequest(
            href = "https://s3.bucket",
            fields = Map(
              "callbackUrl"     -> "https://foo.bar/callback",
              "successRedirect" -> "https://foo.bar/success",
              "errorRedirect"   -> "https://foo.bar/failure"
            )
          ),
          FileUploads(files = Seq(FileUpload.Accepted(
            1,
            "foo-bar-ref-1",
            "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
            ZonedDateTime.parse("2018-04-24T09:30:00Z"),
            "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
            "test.pdf",
            "application/pdf",
            Some(Bulk)
          ), FileUpload.Initiated(2, "foo-bar-ref-2"),
          ))
        )

      val expectedState =  FileUploaded(
        FileUploads(files =
          Seq(FileUpload.Accepted(
        2,
        "foo-bar-ref-2",
        "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
        ZonedDateTime.parse("2018-04-24T09:30:00Z"),
        "000000dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
        "test2.pdf",
        "application/pdf",
        Some(Bulk))))
      )

      val upscanReady = UpscanFileReady(
        reference = "foo-bar-ref-2",
        downloadUrl = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
        uploadDetails = UpscanNotification.UploadDetails(
          uploadTimestamp = ZonedDateTime.parse("2018-04-24T09:30:00Z"),
          checksum = "000000dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          fileName = "test2.pdf",
          fileMimeType = "application/pdf",
        )
      )
      whenReady(service.upscanCallbackArrived(upscanReady, Bulk)(currentState)) {
        newState => assert(newState == expectedState)
      }
    }
  }

  "should not replace an existing Supporting evidence files" in {
    val currentState =
      UploadFile(
        "foo-bar-ref-2",
        UploadRequest(
          href = "https://s3.bucket",
          fields = Map(
            "callbackUrl"     -> "https://foo.bar/callback",
            "successRedirect" -> "https://foo.bar/success",
            "errorRedirect"   -> "https://foo.bar/failure"
          )
        ),
        FileUploads(files = Seq(FileUpload.Accepted(
          1,
          "foo-bar-ref-1",
          "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
          ZonedDateTime.parse("2018-04-24T09:30:00Z"),
          "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          "test.pdf",
          "application/pdf",
          Some(SupportingEvidence)
        ), FileUpload.Initiated(2, "foo-bar-ref-2"),
        ))
      )

    val expectedState =  FileUploaded(
      FileUploads(files =
        Seq(FileUpload.Accepted(
          1,
          "foo-bar-ref-1",
          "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
          ZonedDateTime.parse("2018-04-24T09:30:00Z"),
          "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          "test.pdf",
          "application/pdf",
          Some(SupportingEvidence)),
        FileUpload.Accepted(
          2,
          "foo-bar-ref-2",
          "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
          ZonedDateTime.parse("2018-04-24T09:30:00Z"),
          "000000dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          "test2.pdf",
          "application/pdf",
          Some(SupportingEvidence)))))

    val upscanReady = UpscanFileReady(
      reference = "foo-bar-ref-2",
      downloadUrl = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
      uploadDetails = UpscanNotification.UploadDetails(
        uploadTimestamp = ZonedDateTime.parse("2018-04-24T09:30:00Z"),
        checksum = "000000dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
        fileName = "test2.pdf",
        fileMimeType = "application/pdf",
      )
    )
    whenReady(service.upscanCallbackArrived(upscanReady, SupportingEvidence)(currentState)) {
      newState => assert(newState == expectedState)
    }
  }
}
