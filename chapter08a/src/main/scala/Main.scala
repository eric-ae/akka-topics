package words

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.management.scaladsl.AkkaManagement

object Main extends App {

  val system = ActorSystem(Behaviors.empty, "words")
  AkkaManagement(system).start()

  println("#################### press ENTER to terminate ###############")
  scala.io.StdIn.readLine()
  system.terminate()
}
