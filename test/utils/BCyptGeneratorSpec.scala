package utils

import org.scalatest.mockito.MockitoSugar
import utils.BCryptGenerator.authenticate

class BCyptGeneratorSpec extends UnitSpec with MockitoSugar {

  val rawPassword = "password"

  "fromPassword" should {
    "generate a BCrypt from a password - generating a salt + including it in the payload" in {
      val hashedPassword = BCryptGenerator.fromPassword(rawPassword)

      authenticate(rawPassword, hashedPassword) shouldBe true
    }

    "generate a BCrypt with unique salt" in {
      BCryptGenerator.fromPassword(rawPassword) shouldNot be (BCryptGenerator.fromPassword(rawPassword))
    }
  }
}
