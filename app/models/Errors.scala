package models

import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import models.JsonFormatters._

sealed abstract class ErrorResponse(
                                     val httpStatusCode: Int,
                                     val errorCode: String,
                                     val message: String) {

  def toHttpResponse: Result = Results.Status(httpStatusCode)(Json.toJson(this))
}

case class ErrorInvalidRequest(errorMessage: String) extends ErrorResponse(BAD_REQUEST, "INVALID_REQUEST", errorMessage)
case class ErrorInternalServerError(errorMessage: String) extends ErrorResponse(INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", errorMessage)
case class ErrorNotFound() extends ErrorResponse(NOT_FOUND, "NOT_FOUND", "The resource could not be found.")
case class ErrorUserAlreadyRegistered() extends ErrorResponse(CONFLICT, "EMAIL_ALREADY_REGISTERED", "The email is already registered")
case class ErrorUserNotFound() extends ErrorResponse(NOT_FOUND, "USER_NOT_FOUND", "The user could not be found.")
case class ErrorSessionNotFound() extends ErrorResponse(NOT_FOUND, "SESSION_NOT_FOUND", "The session is invalid or expired.")

class ValidationException(message: String) extends RuntimeException(message)

trait HasSucceeded
object HasSucceeded extends HasSucceeded

case class UserAlreadyRegistered(email: String) extends RuntimeException(s"User already registered with email: $email")
case class UserNotFound(email: String) extends RuntimeException(s"User not found for email: $email")
case class InvalidCredentials() extends RuntimeException("Invalid credentials")
case class SessionNotFound(sessionId: String) extends RuntimeException(s"Session not found for sessionId: $sessionId")
