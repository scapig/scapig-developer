package models

import org.joda.time.DateTime

case class User(email: String,
                firstName: String,
                lastName: String,
                credentials: String,
                registrationTime: DateTime = DateTime.now())
