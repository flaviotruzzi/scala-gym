package gym

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

package object gym {
  type Action = Int
  type State = Int
}


import gym._

trait APIEntity

case class EnvRequest(environment: String) extends APIEntity
case class ResetRequest(render: Boolean) extends APIEntity
case class ActionRequest(action: Action, render: Boolean) extends APIEntity
case class MonitorStartRequest(force: Boolean, resume: Boolean) extends APIEntity
case class UploadRequest(algorithm_id: String, writeup: String, api_key: String, ignore_open_monitors: Boolean) extends APIEntity

case class GenericResponse(message: Boolean) extends APIEntity
case class EnvResponse(instance_id: String) extends APIEntity


case class ActionResponse(done: Boolean,
                          info: Map[String, String],
                          observation: List[Double],
                          reward: Double,
                          render: Option[String]) extends APIEntity


case class InitResponse(observation: List[Double], render: Option[String]) {
  def toActionResponse = ActionResponse(done = false, Map.empty, observation, 0, render)
}


case object Initialize

case object EndOfEpisode

case class Act[T](state: gym.State)

case class Update[A](observation: ActionResponse, lastState: Option[gym.State], lastAction: Option[gym.Action])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val stepResponseFormat = jsonFormat5(ActionResponse)
  implicit val initResponseFormat = jsonFormat2(InitResponse)
  implicit val envRequestFormat = jsonFormat1(EnvRequest)
  implicit val envResponseFormat = jsonFormat1(EnvResponse)
  implicit val envResetRequestFormat = jsonFormat1(ResetRequest)
  implicit val actionRequestFormat = jsonFormat2(ActionRequest)
  implicit val monitorStartRequestFormat = jsonFormat2(MonitorStartRequest)
  implicit val genericResponseFormat = jsonFormat1(GenericResponse)
  implicit val uploadRequestFormat = jsonFormat4(UploadRequest)
}


