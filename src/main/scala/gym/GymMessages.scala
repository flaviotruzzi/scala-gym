package gym

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

package object gym {
  type Action = Int
  type State = Int
}

case class StepResponse(done: Boolean, info: Map[String, String], observation: List[Double], reward: Double)

case class InitResponse(observation: List[Double]) {
  def toStepResponse = StepResponse(done = false, Map.empty, observation, 0)
}

case object Initialize

case object EndOfEpisode

case class Act[T](state: gym.State)

case class Update[A](observation: StepResponse, lastState: Option[gym.State], lastAction: Option[gym.Action])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val stepResponseFormat = jsonFormat4(StepResponse)
  implicit val initResponseFormat = jsonFormat1(InitResponse)
}


