package services

import models._
import org.joda.time.DateTimeUtils
import org.mockito.BDDMockito.given
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import repository.UserRepository
import utils.{BCryptGenerator, UnitSpec}

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

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

  "updateUser" should {
    val updateRequest = UserEditRequest("updatedFirstName", "updatedLastName")

    "fail with UserNotFound error when the user does not exist" in new Setup {
      given(userRepository.fetchByEmail(user.email)).willReturn(successful(None))

      intercept[UserNotFound]{await(underTest.updateUser(user.email, updateRequest))}
    }

    "update the user first name and last name" in new Setup {
      val expectedUser = user.copy(firstName = "updatedFirstName", lastName = "updatedLastName")

      given(userRepository.fetchByEmail(user.email)).willReturn(successful(Some(user)))
      given(userRepository.save(any())).willAnswer(returnSame)

      val result = await(underTest.updateUser(user.email, updateRequest))

      result shouldBe expectedUser
      verify(userRepository).save(expectedUser)
    }
  }

  "updatePassword" should {
    val passwordChangeRequest = PasswordChangeRequest("oldPassword", "newPassword")

    "fail with UserNotFound error when the user does not exist" in new Setup {
      given(userRepository.fetchByEmail(user.email)).willReturn(successful(None))

      intercept[UserNotFound]{await(underTest.updatePassword(user.email, passwordChangeRequest))}
    }

    "fail with InvalidCredentials error when the oldPassword is invalid" in new Setup {
      given(userRepository.fetchByEmail(user.email)).willReturn(successful(Some(user)))
      given(bCryptGenerator.authenticate(passwordChangeRequest.oldPassword, encodedPassword)).willReturn(failed(InvalidCredentials()))

      intercept[InvalidCredentials]{await(underTest.updatePassword(user.email, passwordChangeRequest))}
    }

    "update the password" in new Setup {
      val expectedUser = user.copy(credentials = "newEncryptedPassword")

      given(userRepository.fetchByEmail(user.email)).willReturn(successful(Some(user)))
      given(bCryptGenerator.authenticate(passwordChangeRequest.oldPassword, encodedPassword)).willReturn(successful(HasSucceeded))
      given(bCryptGenerator.fromPassword(passwordChangeRequest.newPassword)).willReturn("newEncryptedPassword")
      given(userRepository.save(any())).willAnswer(returnSame)

      val result = await(underTest.updatePassword(user.email, passwordChangeRequest))

      result shouldBe expectedUser
      verify(userRepository).save(expectedUser)
    }
  }

}