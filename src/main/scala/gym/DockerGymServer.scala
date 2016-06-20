package gym

import java.util

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.messages.{ContainerConfig, HostConfig, PortBinding}
import com.typesafe.scalalogging.LazyLogging
import gym.Action


trait DockerGymServer extends LazyLogging {

  import scala.collection.JavaConversions._

  val docker: DefaultDockerClient = DefaultDockerClient.fromEnv().build()
  val environment: String
  val image = "flaviotruzzi/gym-server:latest"
  var id: String = ""
  val port = "5000"

  def initialize() = {
    docker.pull(image)

    val binding: util.List[PortBinding] = Seq(PortBinding.of("0.0.0.0", port))

    val bindings: util.Map[String, util.List[PortBinding]] = Map(port -> binding)

    logger.debug("Building Host Configuration")
    val hostConfig = HostConfig
      .builder()
      .portBindings(bindings)
      .build()

    logger.debug(s"Building Container Configuration")
    val containerConfig = ContainerConfig
      .builder()
      .hostConfig(hostConfig)
      .image(image)
      .exposedPorts(port)
      .build()

    logger.info("Creating container")
    val creation = docker.createContainer(containerConfig)

    id = creation.id()

    logger.info("Starting container")
    docker.startContainer(id)
  }

  def destroy() = {
    if (!id.isEmpty) {
      logger.debug(s"Stopping container $id")
      docker.stopContainer(id, 5)

      logger.debug(s"Removing container $id")
      docker.removeContainer(id)
    }
  }

}
