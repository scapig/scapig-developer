package repository

import javax.inject.Singleton

import models.{HasSucceeded, Session, User}
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import utils.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SessionRepositorySpec extends UnitSpec with BeforeAndAfterEach {

  val session = Session("sessionId", DateTime.now(), DateTime.now(), "user@test.com")

  lazy val fakeApplication: Application = new GuiceApplicationBuilder()
    .configure("mongodb.uri" -> "mongodb://localhost:27017/tapi-developer-test")
    .build()

  lazy val underTest = fakeApplication.injector.instanceOf[SessionRepository]

  override def afterEach {
    await(await(underTest.repository).drop(failIfNotFound = false))
  }

  "save" should {
    "insert a new session" in {
      await(underTest.save(session))

      await(underTest.fetch(session.sessionId)) shouldBe Some(session)
    }

    "update an existing session" in {
      val updatedSession = session.copy(lastActivity = DateTime.now())
      await(underTest.save(session))

      await(underTest.save(updatedSession))

      await(underTest.fetch(session.sessionId)) shouldBe Some(updatedSession)
    }
  }

  "fetch" should {
    "return the sessiom" in {
      await(underTest.save(session))

      await(underTest.fetch(session.sessionId)) shouldBe Some(session)
    }

    "return None when no user exist" in {
      await(underTest.fetch("anId")) shouldBe None
    }
  }

  "delete" should {
    "delete a session" in {
      await(underTest.save(session))

      val result = await(underTest.delete(session.sessionId))

      result shouldBe HasSucceeded
      await(underTest.fetch(session.sessionId)) shouldBe None
    }

    "not fail when the session does not exist" in {
      await(underTest.delete(session.sessionId)) shouldBe HasSucceeded
    }

  }
}