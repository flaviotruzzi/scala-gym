package gym

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


trait GymClient extends Actor with ActorLogging with JsonSupport {

  import context.dispatcher
  import spray.json._

  val gymServer: GymServer
  val http = Http(context.system)
  val timeout = 5 seconds

  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  override def preStart() = {
    self ! Initialize
  }

  protected def sendAction(action: Int): Future[StepResponse] = http
    .singleRequest(HttpRequest(uri = gymServer.actEndpoint(action)))
    .flatMap(r => r.entity.toStrict(timeout))
    .map(_.data.decodeString("UTF-8").parseJson.convertTo[StepResponse])

  protected def initialize(): Future[StepResponse] = http
    .singleRequest(HttpRequest(uri = gymServer.resetEndpoint()))
    .flatMap(r => r.entity.toStrict(timeout))
    .map(_.data.decodeString("UTF-8").parseJson.convertTo[InitResponse])
    .map(initResponse => initResponse.toStepResponse)
}




