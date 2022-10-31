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

package models

import models.FileType.SupportingEvidence
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec

import java.time.ZonedDateTime

class FileUploadsSpec extends AnyWordSpec with Matchers {

  "FileUploads" when {
    val MAX = FileUpload.MAX_FILENAME_LENGTH

    "trim the file name" in {

      FileUpload.trimFileName("") shouldBe ""
      FileUpload.trimFileName("a") shouldBe "a"
      FileUpload.trimFileName("a.a") shouldBe "a.a"
      FileUpload.trimFileName("a" * MAX) shouldBe "a" * MAX
      FileUpload.trimFileName("a" * (MAX + 1)) shouldBe "a" * MAX
      FileUpload.trimFileName("a" * (MAX - 5) + ".ext") shouldBe "a" * (MAX - 5) + ".ext"
      FileUpload.trimFileName("a" * (MAX - 4) + ".ext") shouldBe "a" * (MAX - 4) + ".ext"
      FileUpload.trimFileName("a" * (MAX + 1) + ".ext") shouldBe "a" * (MAX - 4) + ".ext"
      FileUpload.trimFileName("a" * MAX + ".xml.ext") shouldBe "a" * (MAX - 4) + ".ext"
      FileUpload.trimFileName("a" * (MAX - 7) + ".xml.ext") shouldBe "a" * (MAX - 7) + "xml.ext"
      FileUpload.trimFileName("a" * (MAX - 2) + ".") shouldBe "a" * (MAX - 2) + "."
      FileUpload.trimFileName("a" * (MAX - 1) + ".") shouldBe "a" * (MAX - 1) + "."
      FileUpload.trimFileName("a" * MAX + ".") shouldBe "a" * (MAX - 1) + "."
      FileUpload.trimFileName("-" * MAX) shouldBe "-" * MAX
      FileUpload.trimFileName("-" * (MAX - 5) + ".ext") shouldBe "-" * (MAX - 5) + ".ext"
      FileUpload.trimFileName("-" * (MAX - 4) + ".ext") shouldBe "-" * (MAX - 4) + ".ext"
      FileUpload.trimFileName("-" * (MAX + 1) + ".ext") shouldBe "-" * (MAX - 4) + ".ext"

      FileUpload.trimFileName(
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus tempor egestas viverra usce."
      ) shouldBe "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus tempor egestas viverra usce."
      FileUpload.trimFileName(
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum cursus, erat sed fringilla lacinia, sem nulla vulputate mauris, at tincidunt eros.ext"
      ) shouldBe "LoremipsumdolorsitametconsecteturadipiscingelitVestibulumcursuseratsedfringillalaciniasem.ext"
      FileUpload.trimFileName(
        "123orem_ipsum_dolor_sit_amet-----consec9999999999tetur-adipiscing elit_Vestibulum***12cursus,!!![erat]+sed+fringilla (lacinia), sem/nulla/vulputate /_mauris,~at&tincidunt@eros.ext"
      ) shouldBe "123oremipsumdolorsitametconsec9999999999teturadipiscingelitVestibulum12cursuseratsedfring.ext"

    }

    "trim the file name when converting to sequence of UploadedFile" in {

      val fileUploads = FileUploads(files =
        Seq(
          FileUpload.Accepted(
            1,
            "foo-bar-ref-1",
            "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
            ZonedDateTime.parse("2018-04-24T09:30:00Z"),
            "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
            "a" * (MAX + 10) + ".pdf",
            "application/pdf",
            Some(SupportingEvidence)
          )
        )
      )

      fileUploads.toUploadedFiles.head.fileName shouldBe "a" * (MAX - 4) + ".pdf"
    }
  }
}
