package services

import models.{User, UserAlreadyRegistered, UserCreateRequest}
import org.joda.time.DateTimeUtils
import org.mockito.BDDMockito.given
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import repository.UserRepository
import utils.{BCryptGenerator, UnitSpec}

import scala.concurrent.Future.successful

class DeveloperServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  val userCreateRequest = UserCreateRequest("User@test.com", "password", "John", "Doe")
  val encodedPassword = "encodedPassword"

  trait Setup {
    val userRepository = mock[UserRepository]
    val bCryptGenerator = mock[BCryptGenerator]

    val underTest = new DeveloperService(userRepository, bCryptGenerator)

    val user = User("user@test.com", "John", "Doe", encodedPassword)

    when(bCryptGenerator.fromPassword(userCreateRequest.password)).thenReturn(encodedPassword)
    when(userRepository.save(any())).thenAnswer(returnSame)
  }

  override def beforeEach: Unit = {
    DateTimeUtils.setCurrentMillisFixed(2000)
  }

  override def afterEach: Unit = {
    DateTimeUtils.setCurrentMillisSystem()
  }

  "createUser" should {
    "save the user in the repository and return it" in new Setup {
      given(userRepository.fetchByEmail(user.email)).willReturn(successful(None))

      val result = await(underTest.createUser(userCreateRequest))

      verify(userRepository).save(user)
      result shouldBe user
    }

    "fail with UserAlreadyRegistered when the user already exists" in new Setup {
      given(userRepository.fetchByEmail(user.email)).willReturn(successful(Some(user)))

      intercept[UserAlreadyRegistered]{await(underTest.createUser(userCreateRequest))}
    }
  }

  "fetchByEmail" should {
    "return the user" in new Setup {
      given(userRepository.fetchByEmail(user.email)).willReturn(successful(Some(user)))

      val result = await(underTest.fetchByEmail(user.email))

      result shouldBe Some(user)
    }

    "return the user for uppercase email" in new Setup {
      given(userRepository.fetchByEmail(user.email)).willReturn(successful(Some(user)))

      val result = await(underTest.fetchByEmail(user.email.toUpperCase()))

      result shouldBe Some(user)
    }

    "return None when no user matches" in new Setup {
      given(userRepository.fetchByEmail(user.email)).willReturn(successful(None))

      val result = await(underTest.fetchByEmail(user.email))

      result shouldBe None
    }

  }
}