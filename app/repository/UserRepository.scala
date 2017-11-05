package repository

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import models.User
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.JsonFormatters._
import reactivemongo.play.json._

@Singleton
class UserRepository @Inject()(val reactiveMongoApi: ReactiveMongoApi)  {

  def repository: Future[JSONCollection] =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("user"))

  def save(user: User): Future[User] = {
    repository.flatMap(collection =>
      collection.update(
        Json.obj("email"-> user.email), user, upsert = true) map {
        case result: UpdateWriteResult if result.ok => user
        case error => throw new RuntimeException(s"Failed to save user ${error.errmsg}")
      }
    )
  }

  def fetchByEmail(email: String): Future[Option[User]] = {
    repository.flatMap(collection =>
      collection.find(Json.obj("email"-> email)).one[User]
    )
  }

  private def createIndex(field: String, indexName: String): Future[WriteResult] = {
    repository.flatMap(collection =>
      collection.indexesManager.create(Index(Seq((field, IndexType.Ascending)), Some(indexName)))
    )
  }

  private def ensureIndexes() = {
    Future.sequence(Seq(
      createIndex("email", "emailIndex")
    ))
  }

  ensureIndexes()
}
