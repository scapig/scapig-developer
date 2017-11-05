package services

import javax.inject.{Inject, Singleton}

import models._
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

  def updateUser(email: String, userEditRequest: UserEditRequest): Future[User] = {
    for {
      user <- userRepository.fetchByEmail(email).map(_.getOrElse(throw UserNotFound(email)))
      updated <- userRepository.save(user.copy(firstName = userEditRequest.firstName, lastName = userEditRequest.lastName))
    } yield updated
  }

  def updatePassword(email: String, passwordChangeRequest: PasswordChangeRequest): Future[User] = {
    for {
      user <- userRepository.fetchByEmail(email).map(_.getOrElse(throw UserNotFound(email)))
      _ <- bCryptGenerator.authenticate(passwordChangeRequest.oldPassword, user.credentials)
      updated <- userRepository.save(user.copy(credentials = bCryptGenerator.fromPassword(passwordChangeRequest.newPassword)))
    } yield updated
  }

  def fetchByEmail(email: String): Future[Option[User]] = {
    userRepository.fetchByEmail(email.toLowerCase)
  }
}