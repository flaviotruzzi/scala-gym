package agents.policy

import gym.DockerGymServer
import org.scalatest.FlatSpec

class QLearningAgentTest extends FlatSpec {

  behavior of "QLearningAgent"

  val server = new DockerGymServer { override val environment: String = "CartPole-v0" }

  it should "solve CartPole-v0" in {
    
  }

}
