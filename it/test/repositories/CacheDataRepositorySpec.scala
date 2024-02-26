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

package repositories

import models.requests.Identification
import models.{EORI, FileUpload, FileUploads, SessionState, UserAnswers}
import org.mockito.MockitoSugar
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Configuration
import play.api.libs.json.Json
import services.FileUploaded
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId, ZonedDateTime}
import scala.concurrent.ExecutionContext.Implicits.global

class CacheDataRepositorySpec
    extends AnyFreeSpec with Matchers with DefaultPlayMongoRepositorySupport[UserAnswers] with ScalaFutures
    with OptionValues with MockitoSugar {

  private val instant          = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val mockConfig = mock[Configuration]
  when(mockConfig.get[Int]("mongodb.timeToLiveInSeconds")) thenReturn 1
  when(mockConfig.getOptional[Boolean]("mongodb.replaceIndexes")) thenReturn Some(true)

  protected override val repository = new CacheDataRepository(
    mongoComponent = mongoComponent,
    config = mockConfig,
    stubClock
  )

  ".set" - {

    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val answers        = UserAnswers(Identification("userId", None))
      val expectedResult = answers copy (lastUpdated = instant)

      val setResult     = repository.set(answers).futureValue
      val updatedRecord = find(Filters.equal("id", answers.id)).futureValue.headOption.value

      setResult mustEqual true
      updatedRecord mustEqual expectedResult
    }
  }

  ".get" - {

    "when there is a record for this id" - {

      "must update the lastUpdated time and get the record" in {

        val answers = UserAnswers(Identification("user1", Some(EORI("eori"))))
        repository.set(answers).futureValue

        val result         = repository.get(answers.id).futureValue
        val expectedResult = answers copy (lastUpdated = instant)

        result.value mustEqual expectedResult
      }
    }
  }

  ".updateSession" - {

    "update user answers with FileUploadState data when session data exists" in {

      val answers = UserAnswers(Identification("id", Some(EORI("eori")))).copy(lastUpdated = instant)

      val fileUploaded = FileUploaded(FileUploads(Seq(FileUpload.Accepted(
        1,
        "foo-bar-ref-1",
        "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
        ZonedDateTime.parse("2018-04-24T09:30:00Z"),
        "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
        "test.pdf",
        "application/pdf"
      ))))

      val result             = repository.updateSession(fileUploaded, Some(answers)).futureValue
      val updatedUserAnswers = repository.get("id").futureValue.value

      result mustEqual true
      updatedUserAnswers mustBe answers.copy(fileUploadState = Some(fileUploaded))
    }

    "must return true when session data does not exists" in {

      val fileUploaded = FileUploaded(FileUploads(Seq(FileUpload.Accepted(
        1,
        "foo-bar-ref-1",
        "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
        ZonedDateTime.parse("2018-04-24T09:30:00Z"),
        "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
        "test.pdf",
        "application/pdf"
      ))))

      val result = repository.updateSession(fileUploaded, None).futureValue

      result mustEqual true
    }
  }

  ".getFileUploadState" - {

    "when there is a record for this id" - {

      "must return the SessionState" in {

        val answers = UserAnswers(Identification("id", Some(EORI("eori"))))
        repository.set(answers).futureValue

        val result = repository.getFileUploadState(answers.id).futureValue
        val expectedResult =
          SessionState(None, Some(UserAnswers("id", Some(EORI("eori")), Json.obj(), None, instant, None)))
        result mustEqual expectedResult
      }
    }
  }

  ".resetData" - {

    "must clear data from session data" - {

      val answers = UserAnswers(Identification("id", Some(EORI("eori"))))

      val result             = repository.resetData(answers).futureValue
      val updatedUserAnswers = repository.get("id").futureValue.value

      result mustEqual true
      updatedUserAnswers.data mustBe Json.obj()
    }
  }

}
