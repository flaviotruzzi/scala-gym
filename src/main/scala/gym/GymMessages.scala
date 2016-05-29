package gym

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class StepResponse(done: Boolean, info: Map[String, String], observation: List[Double], reward: Double)

case class InitResponse(observation: List[Double]) {
  def toStepResponse = StepResponse(done = false, Map.empty, observation, 0)
}

case object Initialize

case object EndOfEpisode

case object Act

case class Update(observation: StepResponse)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val stepResponseFormat = jsonFormat4(StepResponse)
  implicit val initResponseFormat = jsonFormat1(InitResponse)
}
