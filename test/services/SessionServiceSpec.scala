package services

import models._
import org.joda.time.{DateTime, DateTimeUtils}
import org.mockito.{BDDMockito, Matchers, Mockito}
import org.mockito.BDDMockito.given
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import repository.{SessionRepository, UserRepository}
import utils.UnitSpec

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class SessionServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  val password = "aPassword"
  val encodedPassword = "encodedPassword"
  val user = User("user@test.com", "John", "Doe", encodedPassword)

  trait Setup {
    val sessionRepository = mock[SessionRepository]
    val authenticationService = mock[AuthenticationService]
    val userRepository = mock[UserRepository]

    val underTest = new SessionService(sessionRepository, authenticationService, userRepository)

    when(sessionRepository.save(any())).thenAnswer(returnSame)
  }

  override def beforeEach(): Unit = {
    DateTimeUtils.setCurrentMillisFixed(2000)
  }

  override def afterEach(): Unit = {
    DateTimeUtils.setCurrentMillisSystem()
  }

  "createForUser" should {
    "create a session when authentication succeeds" in new Setup {
      given(authenticationService.authenticate(user.email, password)).willReturn(successful(user))

      val userSession = await(underTest.createForUser(user.email, password))

      userSession.user shouldBe UserResponse(user.email, user.firstName, user.lastName, user.registrationTime)
      verify(sessionRepository).save(userSession.session)
    }

    "fail with InvalidCredentials when authentication fails" in new Setup {
      given(authenticationService.authenticate(user.email, password)).willReturn(failed(InvalidCredentials()))

      intercept[InvalidCredentials] {
        await(underTest.createForUser(user.email, password))
      }
    }
  }

  "fetch" should {
    val session = Session(user.email)

    "return the session" in new Setup {
      given(sessionRepository.fetch(session.sessionId)).willReturn(successful(Some(session)))
      given(userRepository.fetchByEmail(session.userEmail)).willReturn(successful(Some(user)))

      val result = await(underTest.fetch(session.sessionId))

      result shouldBe UserSession(session, UserResponse(user))
    }

    "fail with SessionNotFound when the sessionId is invalid" in new Setup {
      given(sessionRepository.fetch(session.sessionId)).willReturn(successful(None))

      intercept[SessionNotFound]{await(underTest.fetch(session.sessionId))}
    }

    "fail with SessionNotFound when the userId can not be found" in new Setup {
      given(sessionRepository.fetch(session.sessionId)).willReturn(successful(Some(session)))
      given(userRepository.fetchByEmail(session.userEmail)).willReturn(successful(None))

      intercept[SessionNotFound]{await(underTest.fetch(session.sessionId))}
    }

  }

  "delete" should {
    val session = Session(user.email)

    "delete the session from the repository" in new Setup {
      given(sessionRepository.delete(session.sessionId)).willReturn(successful(HasSucceeded))

      val result = await(underTest.delete(session.sessionId))

      result shouldBe HasSucceeded
    }
  }
}