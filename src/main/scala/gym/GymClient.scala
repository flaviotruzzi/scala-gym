package gym

import java.io.IOException

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import gym.Action

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait GymClient extends Actor with GymEndPointsV1 with ActorLogging with JsonSupport {

  import context.dispatcher
  import spray.json._

  lazy val connectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    http.outgoingConnection(gymServer.docker.getHost, gymServer.port.toInt)

  val gymServer: DockerGymServer
  val environment: String
  val render: Boolean
  val http = Http(context.system)
  val timeout = 5 seconds

  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  var instanceId: String = ""

  override def preStart() = {
    create(environment).map { env =>
      instanceId = env.instance_id
    }
  }

  def create(env: String): Future[EnvResponse] = {
    mkRequest(RequestBuilding.Post(v1.create, mkEntity(EnvRequest(env).toJson))).flatMap { response =>
      response.status match {
        case Created => Unmarshal(response.entity).to[EnvResponse]
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          Future.failed(new IOException(s"FAIL - ${response.status}"))
        }
      }
    }
  }

  def step(action: Action): Future[ActionResponse] = {
    mkRequest(RequestBuilding.Post(v1.step(instanceId), mkEntity(ActionRequest(action, render).toJson))).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[ActionResponse]
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          Future.failed(new IOException(s"FAIL - ${response.status}"))
        }
      }
    }
  }

  def monitorStart(force: Boolean, resume: Boolean): Future[Boolean] = {
    mkRequest(RequestBuilding.Post(v1.monitorStart(instanceId), mkEntity(MonitorStartRequest(force, resume).toJson))).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[GenericResponse].map(_.message)
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          Future.failed(new IOException(s"FAIL - ${response.status}"))
        }
      }
    }
  }

  def mkEntity(json: JsValue) = HttpEntity(ContentTypes.`application/json`, json.toString())

  def mkRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(connectionFlow).runWith(Sink.head)

  def monitorStop(): Future[Boolean] = {
    mkRequest(RequestBuilding.Post(v1.monitorStop(instanceId))).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[GenericResponse].map(_.message)
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          Future.failed(new IOException(s"FAIL - ${response.status}"))
        }
      }
    }
  }

  def upload(algorithmId: String, writeup: String, apiKey: String, ignoreOpenMonitors: Boolean) = {
    mkRequest(RequestBuilding.Post(v1.upload(instanceId), mkEntity(UploadRequest(algorithmId, writeup, apiKey, ignoreOpenMonitors).toJson))).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[GenericResponse].map(_.message)
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          Future.failed(new IOException(s"FAIL - ${response.status}"))
        }
      }
    }
  }

  def info(instanceId: String) = {
    throw new NotImplementedError()
  }

  protected def initialize(): Future[ActionResponse] = reset()

  def reset(): Future[ActionResponse] = {
    mkRequest(RequestBuilding.Post(v1.reset(instanceId), mkEntity(ResetRequest(render).toJson))).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[InitResponse].map(_.toActionResponse)
        case _ =>
          Unmarshal(response.entity).to[String].flatMap { entity =>
            Future.failed(new IOException(s"FAIL - ${response.status}"))
          }
      }
    }
  }
}


trait GymEndPointsV1 {

  object v1 {
    def help = "/v1/help"

    def create = "/v1/envs/create/"

    def info(instanceId: String) = s"/v1/envs/$instanceId/info"

    def reset(instanceId: String) = s"/v1/envs/$instanceId/reset/"

    def step(instanceId: String) = s"/v1/envs/$instanceId/step/"

    def monitorStart(instanceId: String) = s"/v1/envs/$instanceId/monitor/start/"

    def monitorStop(instanceId: String) = s"/v1/envs/$instanceId/monitor/stop/"

    def upload(instanceId: String) = s"/v1/envs/$instanceId/upload/"
  }

}

