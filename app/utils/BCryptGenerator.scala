package utils

import models.{HasSucceeded, InvalidCredentials}
import org.mindrot.jbcrypt.{BCrypt => BCryptUtils}

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class BCryptGenerator {

  def fromPassword(password: String): String = BCryptUtils.hashpw(password, BCryptUtils.gensalt(10))

  def authenticate(rawPassword: String, hashedPassword: String): Future[HasSucceeded] = {
    if(!BCryptUtils.checkpw(rawPassword, hashedPassword)) failed(InvalidCredentials())
    else successful(HasSucceeded)
  }
}
