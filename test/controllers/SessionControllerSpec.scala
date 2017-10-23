package controllers

import models.JsonFormatters._
import models._
import org.mockito.BDDMockito.given
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.{FakeRequest, Helpers}
import services.SessionService
import utils.UnitSpec

import scala.concurrent.Future.{failed, successful}

class SessionControllerSpec extends UnitSpec with MockitoSugar {

  val sessionCreateRequest = SessionCreateRequest("User@test.com", "password")
  val user = User("user@test.com", "John", "Doe", "encodedPassword")
  val userSession = UserSession(Session(user.email), UserResponse(user))

  trait Setup {
    val request = FakeRequest()
    val sessionService = mock[SessionService]

    val underTest = new SessionController(Helpers.stubControllerComponents(), sessionService)
  }

  "create" should {
    "return a 201 (Created) with a new session when the credentials are valid" in new Setup {
      given(sessionService.createForUser(sessionCreateRequest.email, sessionCreateRequest.password)).willReturn(successful(userSession))

      val result = await(underTest.create()(request.withBody(Json.toJson(sessionCreateRequest))))

      status(result) shouldBe Status.CREATED
      jsonBodyOf(result) shouldBe Json.toJson(userSession)
    }

    "return a 401 (Unauthorized) when the credentials are invalid" in new Setup {
      given(sessionService.createForUser(sessionCreateRequest.email, sessionCreateRequest.password)).willReturn(failed(InvalidCredentials()))

      val result = await(underTest.create()(request.withBody(Json.toJson(sessionCreateRequest))))

      status(result) shouldBe Status.UNAUTHORIZED
      jsonBodyOf(result) shouldBe Json.parse("""{"code":"UNAUTHORIZED","message":"Invalid unsername or password"}""")
    }
  }

  "fetch" should {
    "return 200 (Ok) with the session" in new Setup {
      given(sessionService.fetch(userSession.session.sessionId)).willReturn(successful(userSession))

      val result = await(underTest.fetch(userSession.session.sessionId)(request))

      status(result) shouldBe Status.OK
      jsonBodyOf(result) shouldBe Json.toJson(userSession)
    }

    "return 404 (Not Found) when the session does not exist" in new Setup {
      given(sessionService.fetch(userSession.session.sessionId)).willReturn(failed(SessionNotFound("sessionId")))

      val result = await(underTest.fetch(userSession.session.sessionId)(request))

      status(result) shouldBe Status.NOT_FOUND
      jsonBodyOf(result) shouldBe Json.parse("""{"code":"SESSION_NOT_FOUND","message":"The session is invalid or expired."}""")
    }

  }
}
