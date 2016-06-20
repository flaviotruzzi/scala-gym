package agents.policy

import gym.gym._

trait EpsilonGreedy extends QFunction {

  val randomGenerator = scala.util.Random

  val actionSpace: List[Action]

  val ε: Double

  def randomAction(): Action = {
    actionSpace(randomGenerator.nextInt(actionSpace.size))
  }

  def chooseAction(state: State): Action = {
    if (randomGenerator.nextDouble() <= ε) {
      randomAction()
    } else {
      actionSpace
        .map(action => Q(state, action) -> action)
        .maxBy(_._1)
        ._2
    }
  }
}
