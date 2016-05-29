package gym

import java.util

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.messages.{ContainerConfig, HostConfig, PortBinding}
import com.typesafe.scalalogging.LazyLogging


trait GymServer extends LazyLogging {

  import scala.collection.JavaConversions._

  lazy val env = Seq(s"GYM_ENV=$environment")
  val docker: DefaultDockerClient = DefaultDockerClient.fromEnv().build()
  val environment: String
  val image = "flaviotruzzi/gym-server:latest"
  var id: String = ""
  val port = "5000"

  def initialize() = {
    docker.pull(image)

    val binding: util.List[PortBinding] = Seq(PortBinding.of("0.0.0.0", port))

    val bindings: util.Map[String, util.List[PortBinding]] = Map(port -> binding)

    logger.info("Building Host Configuration")
    val hostConfig = HostConfig
      .builder()
      .portBindings(bindings)
      .build()

    logger.info(s"Building Container Configuration with env: $env")
    val containerConfig = ContainerConfig
      .builder()
      .hostConfig(hostConfig)
      .image(image)
      .env(env: _*)
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
      logger.info(s"Stopping container $id")
      docker.stopContainer(id, 5)

      logger.info(s"Removing container $id")
      docker.removeContainer(id)
    }
  }

  def infoEndPoint() = s"http://${docker.getHost}:$port/info"
  def actEndpoint(action: Int) = s"http://${docker.getHost}:$port/step/$action"
  def resetEndpoint() = s"http://${docker.getHost}:$port/reset"

}
