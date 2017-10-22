package services

import javax.inject.{Inject, Singleton}

import models.{InvalidCredentials, UserNotFound}
import repository.UserRepository
import utils.BCryptGenerator

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AuthenticationService @Inject()(userRepository: UserRepository, bCryptGenerator: BCryptGenerator) {

  def authenticate(email: String, password: String) = {
    for {
      maybeUser <- userRepository.fetchByEmail(email)
      user = maybeUser.getOrElse(throw InvalidCredentials())
      _ = bCryptGenerator.authenticate(user.credentials, password)
    } yield user
  }
}
