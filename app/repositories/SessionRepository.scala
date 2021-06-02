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
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import services.FileUploadState

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

  override def clearChangePage(userAnswers: UserAnswers): Future[Boolean] = {

    val selector = Json.obj(
      "_id" -> userAnswers.id
    )

    val modifier = {
      if(userAnswers.changePage.isEmpty)
        Json.obj(
          "$set" -> Json.toJson(userAnswers copy (lastUpdated = LocalDateTime.now)).as[JsObject].setObject(userAnswers.changePagePath, JsNull).get
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

  // TODO - drop this method once optional fields properly supported
  def clearChangePage(userAnswers: UserAnswers): Future[Boolean]

  def updateSession(newState: FileUploadState, userAnswers: Option[UserAnswers]): Future[Boolean]

  def getFileUploadState(id: String): Future[SessionState]

  def resetData(userAnswers: UserAnswers): Future[Boolean]
}
