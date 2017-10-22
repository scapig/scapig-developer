package utils

import org.mindrot.jbcrypt.{BCrypt => BCryptUtils}

object BCryptGenerator {

  def fromPassword(password: String): String = BCryptUtils.hashpw(password, BCryptUtils.gensalt(10))

  def authenticate(rawPassword: String, hashedPassword: String): Boolean = BCryptUtils.checkpw(rawPassword, hashedPassword)
}
