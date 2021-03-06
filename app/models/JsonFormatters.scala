package models

import org.joda.time.DateTime
import play.api.libs.json._

object JsonFormatters {
  val datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val dateRead: Reads[DateTime] = JodaReads.jodaDateReads(datePattern)
  implicit val dateWrite: Writes[DateTime] = JodaWrites.jodaDateWrites(datePattern)
  implicit val dateFormat: Format[DateTime] = Format[DateTime](dateRead, dateWrite)

  implicit val errorResponseWrites = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }

  implicit val formatSession = Json.format[Session]
  implicit val formatUser = Json.format[User]
  implicit val formatUserCreateRequest = Json.format[UserCreateRequest]
  implicit val formatUserEditRequest = Json.format[UserEditRequest]
  implicit val formatUserResponse = Json.format[UserResponse]
  implicit val formatUserSession = Json.format[UserSession]
  implicit val formatSessionCreateRequest = Json.format[SessionCreateRequest]
  implicit val formatPasswordChangeRequest = Json.format[PasswordChangeRequest]

}
