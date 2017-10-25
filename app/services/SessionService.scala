package services

import javax.inject.{Inject, Singleton}

import models._
import repository.{SessionRepository, UserRepository}
import utils.BCryptGenerator

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SessionService @Inject()(sessionRepository: SessionRepository, authenticationService: AuthenticationService, userRepository: UserRepository) {

  def createForUser(email: String, password: String): Future[UserSession] = {
    for {
      user <- authenticationService.authenticate(email, password)
      session <- sessionRepository.save(Session(user.email))
    } yield UserSession(session, UserResponse(user))
  }

  def fetch(sessionId: String): Future[UserSession] = {
    for {
      maybeSession <- sessionRepository.fetch(sessionId)
      session = maybeSession.getOrElse(throw SessionNotFound(sessionId))
      maybeUser <- userRepository.fetchByEmail(session.userEmail)
      user = maybeUser.getOrElse(throw SessionNotFound(sessionId))
    } yield UserSession(session, UserResponse(user))
  }

  def delete(sessionId: String): Future[HasSucceeded] = {
    sessionRepository.delete(sessionId)
  }
}
