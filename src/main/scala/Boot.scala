import agents.QLearningAgent
import akka.actor.ActorSystem
import gym.GymServer

import scala.concurrent.blocking

object Boot extends App with GymServer {
  self =>

  override val environment: String = "CartPole-v0"

  val system = ActorSystem("mySystem")

  initialize()

  blocking {
    Thread.sleep(5000)
  }

  system.actorOf(QLearningAgent.props(this, List(0, 1), 0.05, 1, 0.2))

}
