import agents.policy.QLearningAgent
import akka.actor.ActorSystem
import gym.{Initialize, DockerGymServer}

object Boot extends App with DockerGymServer {
  self =>

  override val environment: String = "CartPole-v0"

  val system = ActorSystem("mySystem")

  initialize()

  val actor = system.actorOf(QLearningAgent.props(
    discount = 0.9,
    alpha = 0.2,
    epsilon = 0.05,
    actionSpace = List(0, 1),
    render = false,
    gymServer = this))


  actor ! Initialize
}
