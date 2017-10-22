package config

import javax.inject.{Inject, Singleton}

import play.api.Configuration

@Singleton
class AppConfig @Inject()(configuration: Configuration) {

  val sessionTimeout = configuration.get[Int]("session.timeout")
}
