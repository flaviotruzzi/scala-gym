
trait Action

trait State

trait QLearner {

  val discountFactor: Double
  val learningRate: Double

  val actions: Seq[Action]

  def predict: Unit

}
