import akka.actor.{ActorSystem, Props}
import gym.{GymAgent, GymServer, StepResponse}

import scala.concurrent.{Future, blocking}

object Boot extends App with GymServer {
  self =>

  override val environment: String = "CartPole-v0"

  val system = ActorSystem("mySystem")


  initialize()
  val actor = system.actorOf(Props[RLActor])

  class RLActor extends GymAgent {
    override val gymServer: GymServer = Boot.this

    override def updateState(observation: StepResponse): Future[_] = Future.successful()

    override def chooseAction(): Int = 0
  }

  blocking {
    Thread.sleep(5000)
    destroy()
    
  }

}
