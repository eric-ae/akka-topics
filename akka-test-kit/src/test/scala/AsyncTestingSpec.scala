package async

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.testkit.typed.scaladsl.LoggingTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import scala.concurrent.duration._
import akka.actor.testkit.typed.scaladsl.FishingOutcomes
import java.util.Base64

class AsyncTestingSpec
    extends AnyWordSpec
    with BeforeAndAfterAll
    with Matchers {

  val testKit = ActorTestKit()

  "a Simplified Manager" must {

    "actor gets forwarded message from manager" in {
      val manager = testKit.spawn(SimplifiedManager())
      val probe = testKit.createTestProbe[String]()
      manager ! SimplifiedManager.Forward(
        "message-to-parse",
        probe.ref)
      probe.expectMessage("message-to-parse")
    }
  }

  "a Simplified Manager" must {

    "be able to log 'it's done'" in {
      val manager = testKit.spawn(SimplifiedManager(), "manager")

      LoggingTestKit.info("it's done").expect {
        manager ! SimplifiedManager.Log
      }
    }
  }

  "a monitor" must {

    "intercept the messages" in {

      val probe = createTestProbe[String]
      val behaviorUnderTest = Behaviors.receiveMessage[String] { _ =>
        Behaviors.ignore
      }
      val behaviorMonitored =
        Behaviors.monitor(probe.ref, behaviorUnderTest)
      val actor = testkit.spawn(behaviorMonitored)

      actor ! "checking"
      probe.expectMessage("checking")

    }
  }
}

object SimplifiedManager {

  sealed trait Command
  case class CreateChild(name: String) extends Command
  case class Forward(message: String, sendTo: ActorRef[String])
      extends Command
  case object Log extends Command

  def apply(): Behaviors.Receive[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case Forward(text, sendTo) =>
          sendTo ! text
          Behaviors.same
        case Log =>
          context.log.info(s"it's done")
          Behaviors.same
      }
    }
}
