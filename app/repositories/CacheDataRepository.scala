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

package repositories

import com.mongodb.client.model.Indexes.ascending
import models.{SessionState, UserAnswers}
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions, Updates}
import play.api.Configuration
import play.api.libs.json.Json
import services.FileUploadState
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.play.http.logging.Mdc

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CacheDataRepository @Inject() (mongoComponent: MongoComponent, config: Configuration, clock: Clock)(implicit
  ec: ExecutionContext
) extends PlayMongoRepository[UserAnswers](
      collectionName = "cache-data",
      mongoComponent = mongoComponent,
      domainFormat = UserAnswers.formats,
      indexes = Seq(
        IndexModel(ascending("id"), IndexOptions().name("idIdx").unique(true)),
        IndexModel(
          ascending("lastUpdated"),
          IndexOptions().name("user-answers-last-updated-index").expireAfter(
            config.get[Int]("mongodb.timeToLiveInSeconds"),
            TimeUnit.SECONDS
          )
        )
      ),
      replaceIndexes = config.getOptional[Boolean]("mongodb.replaceIndexes").getOrElse(false)
    ) with SessionRepository {

  private def filter(id: String) =
    equal("id", Codecs.toBson(id))

  private val upsert = ReplaceOptions().upsert(true)

  def get(id: String): Future[Option[UserAnswers]] = Mdc.preservingMdc {
    collection.findOneAndUpdate(
      filter(id),
      Updates.set("lastUpdated", Instant.now(clock))
    ).toFutureOption()
  }

  def set(userAnswers: UserAnswers): Future[Boolean] = Mdc.preservingMdc {
    collection.replaceOne(
      filter(userAnswers.id),
      userAnswers.copy(lastUpdated = Instant.now(clock)),
      upsert
    ).toFutureOption() map (result => result.exists(_.wasAcknowledged()))
  }

  def updateSession(newState: FileUploadState, userAnswers: Option[UserAnswers]): Future[Boolean] =
    if (userAnswers.nonEmpty)
      set(userAnswers = userAnswers.get.copy(fileUploadState = Some(newState)))
    else Future.successful(true)

  def getFileUploadState(id: String): Future[SessionState] =
    for {
      maybeUserAnswers <- get(id)
    } yield SessionState(maybeUserAnswers.flatMap(_.fileUploadState), maybeUserAnswers)

  def resetData(userAnswers: UserAnswers): Future[Boolean] = set(
    userAnswers.copy(data = Json.obj(), fileUploadState = None)
  )

}
