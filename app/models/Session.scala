package models

import org.joda.time.DateTime

case class Session(sessionId: String,
                   startTime: DateTime,
                   lastActivity: DateTime,
                   userEmail: String)
