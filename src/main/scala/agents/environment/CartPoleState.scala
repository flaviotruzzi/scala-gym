package agents.environment

import gym.ActionResponse
import utils.FiniteQueue
import gym.gym._

trait CartPoleState {

  val environment = "CartPole-v0"
  val minimumAverage = 195.0
  val consecutiveTrials = 100
  val fixedSizeQueue = FiniteQueue[Int](consecutiveTrials)

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

  def isSolved = {
    if (fixedSizeQueue.size < consecutiveTrials) false
    else (1.0 * fixedSizeQueue.sum) / fixedSizeQueue.size >= minimumAverage
  }

}
