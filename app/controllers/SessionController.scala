package controllers

import javax.inject.{Inject, Singleton}

import models._
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import services.SessionService
import models.JsonFormatters._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SessionController  @Inject()(cc: ControllerComponents, sessionService: SessionService) extends AbstractController(cc) with CommonControllers {

  def create() = Action.async(parse.json) { implicit request =>
    withJsonBody[SessionCreateRequest] { sessionCreateRequest: SessionCreateRequest =>
    sessionService.createForUser(sessionCreateRequest.email, sessionCreateRequest.password) map { userSession => Created(Json.toJson(userSession))}
    } recover {
      case _: InvalidCredentials => ErrorInvalidCredentials().toHttpResponse
    }
  }

  def fetch(sessionId: String) = Action.async { implicit request =>
    sessionService.fetch(sessionId) map { userSession => Ok(Json.toJson(userSession))
    } recover {
      case _: SessionNotFound => ErrorSessionNotFound().toHttpResponse
    }
  }

  def delete(sessionId: String) = Action.async { implicit request =>
    Future.successful(Ok(""))
  }

}
