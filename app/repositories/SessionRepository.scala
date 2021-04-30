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

import java.time.LocalDateTime

import akka.stream.Materializer
import javax.inject.Inject
import models.{RichJsObject, SessionState, UserAnswers}
import play.api.Configuration
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONSerializationPack
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.collection.Helpers.idWrites
import reactivemongo.play.json.collection.JSONCollection
import services.FileUploadState

import scala.concurrent.{ExecutionContext, Future}

class DefaultSessionRepository @Inject()(
                                          mongo: ReactiveMongoApi,
                                          config: Configuration
                                        )(implicit ec: ExecutionContext, m: Materializer) extends SessionRepository {


  private val collectionName: String = "user-answers"
  private val lastUpdatedIndexKey = "lastUpdated"
  private val lastUpdatedIndexName = "user-answers-last-updated-index"

  private val cacheTtl = config.get[Int]("mongodb.timeToLiveInSeconds")

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val lastUpdatedIndex = Index.apply(BSONSerializationPack)(
    key = Seq(lastUpdatedIndexKey -> IndexType.Ascending),
    name = Some(lastUpdatedIndexName),
    expireAfterSeconds = Some(cacheTtl),
    options = BSONDocument("expireAfterSeconds" -> cacheTtl),
    unique = false,
    background = false,
    dropDups = false,
    sparse = false,
    version = None,
    partialFilter = None,
    storageEngine = None,
    weights = None,
    defaultLanguage = None,
    languageOverride = None,
    textIndexVersion = None,
    sphereIndexVersion = None,
    bits = None,
    min = None,
    max = None,
    bucketSize = None,
    collation = None,
    wildcardProjection = None
  )

  def ensureTtlIndex(collection: JSONCollection): Future[Unit] = {
    collection.indexesManager.ensure(lastUpdatedIndex) flatMap {
      newlyCreated =>
        // false if the index already exists
        if (!newlyCreated) {
          for {
            _ <- collection.indexesManager.drop(lastUpdatedIndexName)
            _ <- collection.indexesManager.ensure(lastUpdatedIndex)
          } yield ()
        } else {
          Future.successful(())
        }
    }
  }

  val started: Future[Unit] =
    collection.flatMap(ensureTtlIndex).map(_ => ())

  override def get(id: String): Future[Option[UserAnswers]] =
    collection.flatMap(_.find(Json.obj("_id" -> id), None).one[UserAnswers])

  def resetData(userAnswers: UserAnswers): Future[Boolean] = {

    val selector = Json.obj(
      "_id" -> userAnswers.id
    )

    val modifier = {
        Json.obj(
      "$set" -> Json.toJson(userAnswers copy (lastUpdated = LocalDateTime.now)).
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
      if(userAnswers.fileUploadState.isEmpty)
      Json.obj(
      "$set" -> Json.toJson(userAnswers copy (lastUpdated = LocalDateTime.now)).as[JsObject].setObject(userAnswers.fileUploadPath, JsNull).get
    ) else
        Json.obj(
          "$set" -> (userAnswers copy (lastUpdated = LocalDateTime.now))
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

trait SessionRepository {

  val started: Future[Unit]

  def get(id: String): Future[Option[UserAnswers]]

  def set(userAnswers: UserAnswers): Future[Boolean]

  def updateSession(newState: FileUploadState, userAnswers: Option[UserAnswers]): Future[Boolean]

  def getFileUploadState(id: String): Future[SessionState]

  def resetData(userAnswers: UserAnswers): Future[Boolean]
}
