package gym

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.Future

trait GymAgent extends Actor with GymClient with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  private var episodes: Int = 0
  private var timesteps: Int = 0

  def updateState(observation: StepResponse): Future[_]

  def chooseAction(): Int

  def receive = {
    case Update(observation) =>

      updateState(observation).map { f =>
        if (observation.done) {
          self ! EndOfEpisode
        } else {
          timesteps += 1
          self ! Act
        }
      }

    case Act =>
      val action = chooseAction()
      sendAction(action).map(Update).pipeTo(self)

    case EndOfEpisode =>
      episodes += 1

      log.info(s"Episode $episodes ended with $timesteps timesteps")

      self ! Initialize

    case Initialize =>
      timesteps = 0
      initialize().map(Update).pipeTo(self)
  }

}
