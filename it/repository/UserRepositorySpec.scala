package repository

import javax.inject.Singleton

import models.User
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import utils.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserRepositorySpec extends UnitSpec with BeforeAndAfterEach {

  val user = User("email@test.com", "john", "doe", "hashedpassword")

  lazy val fakeApplication: Application = new GuiceApplicationBuilder()
    .configure("mongodb.uri" -> "mongodb://localhost:27017/scapig-developer-test")
    .build()

  lazy val underTest = fakeApplication.injector.instanceOf[UserRepository]

  override def afterEach {
    await(await(underTest.repository).drop(failIfNotFound = false))
  }

  "save" should {
    "insert a new user" in {
      await(underTest.save(user))

      await(underTest.fetchByEmail(user.email)) shouldBe Some(user)
    }

    "update an existing user" in {
      val updatedUser = user.copy(lastName = "updatedName")
      await(underTest.save(user))

      await(underTest.save(updatedUser))

      await(underTest.fetchByEmail(user.email)) shouldBe Some(updatedUser)
    }

  }

  "fetchByEmail" should {
    "return the user" in {
      await(underTest.save(user))

      await(underTest.fetchByEmail(user.email)) shouldBe Some(user)
    }

    "return None when no user exist" in {
      await(underTest.fetchByEmail("other@gmail.com")) shouldBe None
    }
  }
}