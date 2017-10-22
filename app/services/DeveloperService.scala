package services

import javax.inject.{Inject, Singleton}

import models.{User, UserAlreadyRegistered, UserCreateRequest}
import repository.UserRepository
import utils.BCryptGenerator

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class DeveloperService @Inject()(userRepository: UserRepository, bCryptGenerator: BCryptGenerator) {

  def createUser(userCreateRequest: UserCreateRequest): Future[User] = {
    for {
      developer <- userRepository.fetchByEmail(userCreateRequest.email.toLowerCase())
      _ = if (developer.isDefined) throw UserAlreadyRegistered(userCreateRequest.email)
      user <- userRepository.save(User(userCreateRequest, bCryptGenerator.fromPassword(userCreateRequest.password)))
    } yield user
  }

  def fetchByEmail(email: String): Future[Option[User]] = {
    userRepository.fetchByEmail(email.toLowerCase)
  }
}