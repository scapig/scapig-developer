package controllers

import javax.inject.{Inject, Singleton}

import models.{ErrorUserAlreadyRegistered, ErrorUserNotFound, UserAlreadyRegistered, UserCreateRequest}
import play.api.mvc.{AbstractController, ControllerComponents}
import models.JsonFormatters._
import play.api.libs.json.Json
import services.DeveloperService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class DeveloperController  @Inject()(cc: ControllerComponents, developerService: DeveloperService) extends AbstractController(cc) with CommonControllers {

  def fetchByEmail(email: String) = Action.async { implicit request =>
    developerService.fetchByEmail(email) map {
      case Some(user) => Ok(Json.toJson(user))
      case None => ErrorUserNotFound().toHttpResponse
    }
  }

  def register() =  Action.async(parse.json) { implicit request =>
    withJsonBody[UserCreateRequest] { userCreateRequest: UserCreateRequest =>
      developerService.createUser(userCreateRequest) map (user => Ok(Json.toJson(user)))
    } recover {
      case _: UserAlreadyRegistered   => ErrorUserAlreadyRegistered().toHttpResponse
    }
  }
}
