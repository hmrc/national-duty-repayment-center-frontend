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

package repositories

import java.time.Instant
import java.util.concurrent.TimeUnit

import akka.stream.Materializer
import com.mongodb.client.model.Indexes.ascending
import javax.inject.Inject
import models.{RichJsObject, SessionState, UserAnswers}
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions, Updates}
import play.api.Configuration
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import services.FileUploadState
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.play.http.logging.Mdc

import scala.concurrent.{ExecutionContext, Future}

class DefaultSessionRepository @Inject()(
                                          mongo: ReactiveMongoApi,
                                          config: Configuration
                                        )(implicit ec: ExecutionContext, m: Materializer) extends SessionRepository {


  private val collectionName: String = "user-answers"

  private val cacheTtl = config.get[Int]("mongodb.timeToLiveInSeconds")

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val lastUpdatedIndex = Index(
    key = Seq("lastUpdated" -> IndexType.Ascending),
    name = Some("user-answers-last-updated-index"),
    options = BSONDocument("expireAfterSeconds" -> cacheTtl)
  )

  val started: Future[Unit] =
    collection.flatMap {
      _.indexesManager.ensure(lastUpdatedIndex)
    }.map(_ => ())

  override def get(id: String): Future[Option[UserAnswers]] =
    collection.flatMap(_.find(Json.obj("_id" -> id), None).one[UserAnswers])

  def resetData(userAnswers: UserAnswers): Future[Boolean] = {

    val selector = Json.obj(
      "_id" -> userAnswers.id
    )

    val modifier = {
      Json.obj(
        "$set" -> Json.toJson(userAnswers copy (lastUpdated = Instant.now)).
          as[JsObject].setObject(userAnswers.dataPath, Json.obj()).get.
          setObject(userAnswers.fileUploadPath, JsNull).get
      )
    }
    collection.flatMap {
      _.update(ordered = false)
        .one(selector, modifier, upsert = true).map {
        lastError =>
          lastError.ok
      }
    }
  }

  override def set(userAnswers: UserAnswers): Future[Boolean] = {

    val selector = Json.obj(
      "_id" -> userAnswers.id
    )

    val modifier = {
      if (userAnswers.fileUploadState.isEmpty)
        Json.obj(
          "$set" -> Json.toJson(userAnswers copy (lastUpdated = Instant.now)).as[JsObject].setObject(userAnswers.fileUploadPath, JsNull).get
        ) else
        Json.obj(
          "$set" -> (userAnswers copy (lastUpdated = Instant.now))
        )
    }
    collection.flatMap {
      _.update(ordered = false)
        .one(selector, modifier, upsert = true).map {
        lastError =>
          lastError.ok
      }
    }
  }

  override def updateSession(newState: FileUploadState, userAnswers: Option[UserAnswers]): Future[Boolean] = {
    if (userAnswers.nonEmpty)
      set(userAnswers = userAnswers.get.copy(fileUploadState = Some(newState)))
    else Future.successful(true)
  }

  override def getFileUploadState(id: String): Future[SessionState] = {
    for {
      u <- get(id)
    } yield (SessionState(u.flatMap(_.fileUploadState), u))
  }

}


class PlayMongoSessionRepository @Inject()(mongoComponent: MongoComponent, config: Configuration)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[UserAnswers](
    collectionName = "user-answers2",
    mongoComponent = mongoComponent,
    domainFormat = UserAnswers.formats,
    indexes = Seq(
      IndexModel(ascending("id"), IndexOptions().name("idIdx").unique(true)),
      IndexModel(
        ascending("lastUpdated"),
        IndexOptions().name("user-answers-last-updated-index").expireAfter(config.get[Int]("mongodb.timeToLiveInSeconds"), TimeUnit.SECONDS)
      )
    ),
    rebuildIndexes = true
  ) with SessionRepository {
  def get(id: String): Future[Option[UserAnswers]] = Mdc.preservingMdc {
    collection.findOneAndUpdate(filter(id), Updates.set("lastUpdated", Instant.now())).toFutureOption()
  }


  private def filter(id: String) =
    equal("id", Codecs.toBson(id))

  private val upsert = ReplaceOptions().upsert(true)

  override val started: Future[Unit] = ensureIndexes map (_ => ())

  override def set(userAnswers: UserAnswers): Future[Boolean] =
    collection.replaceOne(filter(userAnswers.id), userAnswers.copy(lastUpdated = Instant.now()), upsert).toFutureOption() map (
      result => result.exists(_.wasAcknowledged()))

  override def updateSession(newState: FileUploadState, userAnswers: Option[UserAnswers]): Future[Boolean] = {
    if (userAnswers.nonEmpty)
      set(userAnswers = userAnswers.get.copy(fileUploadState = Some(newState)))
    else Future.successful(true)
  }

  override def getFileUploadState(id: String): Future[SessionState] = {
    for {
      maybeUserAnswers <- get(id)
    } yield SessionState(maybeUserAnswers.flatMap(_.fileUploadState), maybeUserAnswers)
  }

  override def resetData(userAnswers: UserAnswers): Future[Boolean] = set(userAnswers.copy(data = Json.obj(), fileUploadState = None))
}

trait SessionRepository {

  val started: Future[Unit]

  def get(id: String): Future[Option[UserAnswers]]

  def set(userAnswers: UserAnswers): Future[Boolean]

  def updateSession(newState: FileUploadState, userAnswers: Option[UserAnswers]): Future[Boolean]

  def getFileUploadState(id: String): Future[SessionState]

  def resetData(userAnswers: UserAnswers): Future[Boolean]
}
