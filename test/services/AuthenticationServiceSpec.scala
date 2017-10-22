package services

import models.{HasSucceeded, InvalidCredentials, User, UserNotFound}
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import repository.UserRepository
import utils.{BCryptGenerator, UnitSpec}

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class AuthenticationServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  val password = "aPassword"
  val encodedPassword = "encodedPassword"
  val user = User("user@test.com", "John", "Doe", encodedPassword)

  trait Setup {
    val userRepository = mock[UserRepository]
    val bCryptGenerator = mock[BCryptGenerator]

    val underTest = new AuthenticationService(userRepository, bCryptGenerator)
  }

  "authenticate" should {
    "return the user when the email and password are valid" in new Setup {
      given(userRepository.fetchByEmail(user.email)).willReturn(successful(Some(user)))
      given(bCryptGenerator.authenticate(password, encodedPassword)).willReturn(successful(HasSucceeded))

      val result = await(underTest.authenticate(user.email, password))

      result shouldBe user
    }

    "fail with InvalidCredentials when the email is invalid" in new Setup {
      given(userRepository.fetchByEmail(user.email)).willReturn(successful(None))

      intercept[InvalidCredentials]{await(underTest.authenticate(user.email, password))}
    }

    "fail with InvalidCredentials when the credential validation fails" in new Setup {
      given(userRepository.fetchByEmail(user.email)).willReturn(successful(Some(user)))
      given(bCryptGenerator.authenticate(password, encodedPassword)).willReturn(failed(InvalidCredentials()))

      intercept[InvalidCredentials]{await(underTest.authenticate(user.email, password))}
    }

  }
}
