package utils

import models.{HasSucceeded, InvalidCredentials}
import org.scalatest.mockito.MockitoSugar

class BCyptGeneratorSpec extends UnitSpec with MockitoSugar {

  val rawPassword = "password"
  val underTest = new BCryptGenerator()

  "fromPassword" should {
    "generate a BCrypt from a password - generating a salt + including it in the payload" in {
      val hashedPassword = underTest.fromPassword(rawPassword)

      await(underTest.authenticate(rawPassword, hashedPassword)) shouldBe HasSucceeded
    }

    "generate a BCrypt with unique salt" in {
      underTest.fromPassword(rawPassword) shouldNot be(underTest.fromPassword(rawPassword))
    }
  }

  "authenticate" should {
    "return HasSucceeded when the credentials are valid" in {
      val hashedPassword = underTest.fromPassword(rawPassword)

      await(underTest.authenticate(rawPassword, hashedPassword)) shouldBe HasSucceeded
    }

    "fail with InvalidCredentials when the credentials are invalid" in {
      val hashedPassword = underTest.fromPassword(rawPassword)

      intercept[InvalidCredentials]{await(underTest.authenticate("otherpassword", hashedPassword))}
    }
  }
}