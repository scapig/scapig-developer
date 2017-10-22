package repository

import javax.inject.{Inject, Singleton}

import config.AppConfig
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import models.Session
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection
import models.JsonFormatters._
import reactivemongo.play.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SessionRepository @Inject()(val reactiveMongoApi: ReactiveMongoApi, val appConfig: AppConfig)  {

  val repository: Future[JSONCollection] =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("session"))

  def save(session: Session): Future[Session] = {
    repository.flatMap(collection =>
      collection.update(
        Json.obj("sessionId"-> session.sessionId), session, upsert = true) map {
        case result: UpdateWriteResult if result.ok => session
        case error => throw new RuntimeException(s"Failed to save session ${error.errmsg}")
      }
    )
  }

  def fetch(sessionId: String): Future[Option[Session]] = {
    repository.flatMap(collection =>
      collection.find(Json.obj("sessionId"-> sessionId)).one[Session]
    )
  }

  private def createIndex(index: Index): Future[WriteResult] = {
    repository.flatMap(collection =>
      collection.indexesManager.create(index)
    )
  }

  private def ensureIndexes() = {
    Future.sequence(Seq(
      createIndex(Index(Seq("lastActivity" -> IndexType.Ascending), name = Some("sessionTTLIndex"), options = BSONDocument("expireAfterSeconds" -> appConfig.sessionTimeout))),
      createIndex(Index(Seq("sessionId" -> IndexType.Ascending), name = Some("sessionIdIndex"), unique = true))
    ))
  }

  ensureIndexes()
}
