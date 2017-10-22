package models

import java.util.UUID

import org.joda.time.DateTime

case class Session(userEmail: String,
                   sessionId: String = UUID.randomUUID().toString,
                   startTime: DateTime = DateTime.now(),
                   lastActivity: DateTime  = DateTime.now())

case class SessionCreateRequest(email: String, password: String)

case class UserSession(session: Session, user: UserResponse)
