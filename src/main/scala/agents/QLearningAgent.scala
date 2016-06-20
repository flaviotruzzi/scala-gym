package agents

import akka.actor.Props
import gym.gym.{Action, State}
import gym.{GymAgent, DockerGymServer, ActionResponse}

import scala.collection.mutable
import scala.concurrent.Future
import scala.language.implicitConversions

class QLearningAgent(val gymServer: DockerGymServer,
                     val environment: String,
                     val render: Boolean,
                     override val actionSpace: List[Action],
                     override val epsilon: Double,
                     val discount: Double,
                     val alpha: Double) extends GymAgent with EpsilonGreedy with CartPoleState {

  private val qFunction: mutable.Map[(State, Action), Double] = mutable.Map.empty

  override def updateState(observation: ActionResponse, lastState: Option[State], action: Option[Action]): Unit = Future.successful {
    val newState = toState(observation)
    import observation._

    lastState.foreach { state =>

      val maxQa = actionSpace.map(a => Q(newState, a)).max

      qFunction += (state, action.get) -> (Q(state, action.get) + (
        alpha * (reward + discount * maxQa - Q(state, action.get))
      ))

    }

  }

  override def Q: (State, Action) => Double =
    (state, action) => qFunction.getOrElse((state, action), 1.0)

}

object QLearningAgent {
  def props(gymServer: DockerGymServer, actionSpace: List[Action], render: Boolean, epsilon: Double, discount: Double, alpha: Double) =
    Props(classOf[QLearningAgent], gymServer, "CartPole-v0", render, actionSpace, epsilon, discount, alpha)
}


trait CartPoleState {

  val discreteSpace: List[List[Double]] = List(
    List(),
    makeRange(-3.5, 3.5, 10),
    makeRange(-0.41, 0.41, 10),
    makeRange(-3.5, 3.5, 10)
  )

  def makeRange(init: Double, end: Double, slices: Int): List[Double] = {
    init to end by ((end - init) / (slices - 1))
  }.toList

  implicit def toState: (ActionResponse => State) = { stepResponse =>
     val state = stepResponse
      .observation
      .zipWithIndex
      .map(feature => discreteSpace(feature._2).count(p => p < feature._1))
      .map(_.toString)
      .mkString
      .toInt
    state
  }

}


trait QFunction {
  def Q: (State, Action) => Double
}

trait EpsilonGreedy extends QFunction {

  val randomGenerator = scala.util.Random

  val actionSpace: List[Action]

  val epsilon: Double

  def randomAction(): Action = {
    actionSpace(randomGenerator.nextInt(actionSpace.size))
  }

  def chooseAction(state: State): Action = {
    if (randomGenerator.nextDouble() <= epsilon) {
      randomAction()
    } else {
      actionSpace
        .map(action => Q(state, action) -> action)
        .maxBy(_._1)
        ._2
    }
  }
}
