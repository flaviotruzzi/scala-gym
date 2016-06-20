package agents.policy

import agents.environment.CartPoleState
import akka.actor.Props
import gym.gym.{Action, State}
import gym.{ActionResponse, DockerGymServer, GymAgent}

import scala.collection.mutable
import scala.concurrent.Future
import scala.language.implicitConversions

class QLearningAgent(val γ: Double,
                     val α: Double,
                     val ε: Double,
                     val actionSpace: List[Action],
                     val render: Boolean,
                     val gymServer: DockerGymServer) extends GymAgent with EpsilonGreedy with CartPoleState {

  private val qFunction: mutable.Map[(State, Action), Double] = mutable.Map.empty

  override def updateState(observation: ActionResponse,
                           lastState: Option[State],
                           action: Option[Action]) = Future.successful {

    val newState = toState(observation)

    lastState.foreach { state =>
      import observation._

      val maxQa = actionSpace.map(a => Q(newState, a)).max
      val learnedValue = reward + γ * maxQa

      qFunction.update((state, action.get), Q(state, action.get) * (1 - α) + α * learnedValue)
      
    }

  }

  override def Q: (State, Action) => Double =
    (state, action) => qFunction.getOrElse((state, action), 1.0)

}

object QLearningAgent {
  def props(discount: Double,
            alpha: Double,
            epsilon: Double,
            actionSpace: List[Action],
            render: Boolean,
            gymServer: DockerGymServer) = {

    Props(classOf[QLearningAgent], discount, alpha, epsilon, actionSpace, render, gymServer)

  }
}
