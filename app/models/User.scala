package models

import org.joda.time.DateTime

case class User(email: String,
                firstName: String,
                lastName: String,
                credentials: String,
                registrationTime: DateTime = DateTime.now())

object User {
  def apply(userCreateRequest: UserCreateRequest, credentials: String): User = User(
    userCreateRequest.email.toLowerCase,
    userCreateRequest.firstName,
    userCreateRequest.lastName,
    credentials)
}

case class UserCreateRequest(email: String,
                             password: String,
                             firstName: String,
                             lastName: String)

case class UserResponse(email: String,
                        firstName: String,
                        lastName: String,
                        registrationTime: DateTime)

object UserResponse {
  def apply(user: User): UserResponse = UserResponse(user.email, user.firstName, user.lastName, user.registrationTime)
}