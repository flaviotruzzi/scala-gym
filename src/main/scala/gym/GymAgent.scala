package gym

import akka.actor.{Actor, ActorLogging}
import gym._

trait GymAgent extends Actor with GymClient with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  private var episodes: Int = 0
  private var timesteps: Int = 0

  def updateState(observation: ActionResponse, lastState: Option[State], action: Option[Action]): Unit

  def chooseAction(state: State): Action

  implicit def toState: ActionResponse => State

  override def receive = {
    case Update(observation, lastState, lastAction) =>
      observation.done match {
        case true =>
          updateState(observation.copy(reward = -200), lastState, lastAction)
          self ! EndOfEpisode
        case false =>
          updateState(observation, lastState, lastAction)

          self ! Act(observation)
      }

    case Act(state) =>
      if (timesteps >= 200)
        self ! EndOfEpisode
      else {
        timesteps += 1

        val action = chooseAction(state)
        step(action).map(actionResponse => Update(actionResponse, Option(state), Option(action))).pipeTo(self)
      }

    case EndOfEpisode =>
      episodes += 1

      log.info(s"Episode $episodes ended with $timesteps timesteps")

      self ! Initialize

    case Initialize =>
      log.info("Initializing!")
      timesteps = 0
      initialize().map(s => Update(s, None, None)).pipeTo(self)
  }

}
