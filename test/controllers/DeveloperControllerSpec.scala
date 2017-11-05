package controllers

import models.{PasswordChangeRequest, UserEditRequest, _}
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{verify, verifyZeroInteractions}
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import play.api.test.{FakeRequest, Helpers}
import services.DeveloperService
import utils.UnitSpec
import models.JsonFormatters._

import scala.concurrent.Future
import scala.concurrent.Future.successful

class DeveloperControllerSpec extends UnitSpec with MockitoSugar {

  val userCreateRequest = UserCreateRequest("User@test.com", "password", "John", "Doe")
  val user = User("user@test.com", "John", "Doe", "encodedPassword")

  trait Setup {
    val request = FakeRequest()
    val developerService = mock[DeveloperService]

    val underTest = new DeveloperController(Helpers.stubControllerComponents(), developerService)
  }

  "register" should {
    "succeed with a 201 (Created) and the user" in new Setup {
      given(developerService.createUser(userCreateRequest)).willReturn(successful(user))

      val result = await(underTest.register()(request.withBody(toJson(userCreateRequest))))

      status(result) shouldBe Status.CREATED
      jsonBodyOf(result) shouldBe Json.toJson(UserResponse(user))
    }

    "fail with a 400 (Bad Request) when the json payload is invalid for the request" in new Setup {

      val body = """{ "invalid": "json" }"""

      val result = await(underTest.register()(request.withBody(Json.parse(body))))

      status(result) shouldBe Status.BAD_REQUEST
      jsonBodyOf(result) shouldBe Json.parse("""{"code":"INVALID_REQUEST","message":"email is required"}""")
      verifyZeroInteractions(developerService)
    }

    "return 409 (Conflict) when the user already exists" in new Setup {
      given(developerService.createUser(userCreateRequest)).willReturn(Future.failed(UserAlreadyRegistered("email")))

      val result = await(underTest.register()(request.withBody(toJson(userCreateRequest))))

      status(result) shouldBe Status.CONFLICT
      jsonBodyOf(result) shouldBe Json.parse("""{"code":"EMAIL_ALREADY_REGISTERED","message":"The email is already registered"}""")
    }

  }

  "fetchByEmail" should {
    "return 200 (Ok) with the user" in new Setup {
      given(developerService.fetchByEmail(user.email)).willReturn(successful(Some(user)))

      val result = await(underTest.fetchByEmail(user.email)(request))

      status(result) shouldBe Status.OK
      jsonBodyOf(result) shouldBe Json.toJson(user)
    }

    "return 404 (NotFound) when the user does not exist" in new Setup {
      given(developerService.fetchByEmail(user.email)).willReturn(successful(None))

      val result = await(underTest.fetchByEmail(user.email)(request))

      status(result) shouldBe Status.NOT_FOUND
      jsonBodyOf(result) shouldBe Json.parse("""{"code":"USER_NOT_FOUND","message":"The user could not be found."}""")
    }

  }

  "updateProfile" should {
    val userEditRequest = UserEditRequest("updatedFirstName", "updatedLastName")
    val updatedUser = user.copy(firstName = "updatedFirstName", lastName = "updatedLastName")

    "save the update user and return 200 (Ok) with the updated user" in new Setup {
      given(developerService.updateUser(user.email, userEditRequest)).willReturn(successful(updatedUser))

      val result = await(underTest.updateProfile(user.email)(request.withBody(toJson(userEditRequest))))

      verify(developerService).updateUser(user.email, userEditRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(result) shouldBe Json.toJson(UserResponse(updatedUser))
    }
  }

  "changePassword" should {
    val passwordChangeRequest = PasswordChangeRequest("updatedPassword")
    val updatedUser = user.copy(credentials = "updatedPassword")

    "save the update password and return 200 (Ok) with the updated user" in new Setup {
      given(developerService.updatePassword(user.email, "updatedPassword")).willReturn(successful(updatedUser))

      val result = await(underTest.changePassword(user.email)(request.withBody(toJson(passwordChangeRequest))))

      verify(developerService).updatePassword(user.email, passwordChangeRequest.password)
      status(result) shouldBe Status.OK
      jsonBodyOf(result) shouldBe Json.toJson(UserResponse(updatedUser))
    }
  }

}