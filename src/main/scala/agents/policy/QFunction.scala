package agents.policy

import gym.gym._

trait QFunction {
  def Q: (State, Action) => Double
}
