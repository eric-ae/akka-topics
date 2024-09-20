package countwords

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{ Receptionist, ServiceKey }

object Worker {

  val RegistrationKey: ServiceKey[Command] = ServiceKey[Worker.Command]("Worker")

  /*
  * [2024-09-19 12:56:00,083] [WARN] [akka.serialization.jackson.JacksonSerializer] [SECURITY] [WordsCluster-akka.actor.default-dispatcher-21] - Can't serialize/deserialize object of type [countwords.Worker$Process] in [akka.serialization.jackson.JacksonCborSerializer]. Only classes that are listed as allowed are allowed for security reasons. Configure allowed classes with akka.actor.serialization-bindings or akka.serialization.jackson.allowed-class-prefix.
[2024-09-19 12:56:00,083] [ERROR] [akka.remote.artery.Deserializer] [] [WordsCluster-akka.actor.default-dispatcher-21] - Failed to deserialize message from [akka://WordsCluster@127.0.0.1:2555] with serializer id [33] and manifest [countwords.Worker$Process].
java.lang.IllegalArgumentException: Can't serialize/deserialize object of type [countwords.Worker$Process] in [akka.serialization.jackson.JacksonCborSerializer]. Only classes that are listed as allowed are allowed for security reasons. Configure allowed classes with akka.actor.serialization-bindings or akka.serialization.jackson.allowed-class-prefix.
	at akka.serialization.jackson.JacksonSerializer.checkAllowedClass(JacksonSerializer.scala:431)
	at akka.serialization.jackson.JacksonSerializer.fromBinary(JacksonSerializer.scala:358)
	at akka.serialization.Serialization.$anonfun$deserializeByteArray$1(Serialization.scala:218)
	at akka.serialization.Serialization.withTransportInformation(Serialization.scala:157)
	at akka.serialization.Serialization.deserializeByteArray(Serialization.scala:216)
	at akka.serialization.Serialization.deserializeByteBuffer(Serialization.scala:270)
	at akka.remote.MessageSerializer$.deserializeForArtery(MessageSerializer.scala:101)
	at akka.remote.artery.Deserializer$$anon$3.onPush(Codecs.scala:668)
	at akka.stream.impl.fusing.GraphInterpreter.processPush(GraphInterpreter.scala:542)
	at akka.stream.impl.fusing.GraphInterpreter.processEvent(GraphInterpreter.scala:496)
	at akka.stream.impl.fusing.GraphInterpreter.execute(GraphInterpreter.scala:390)
	at akka.stream.impl.fusing.GraphInterpreterShell.runBatch(ActorGraphInterpreter.scala:650)
	at akka.stream.impl.fusing.GraphInterpreterShell$AsyncInput.execute(ActorGraphInterpreter.scala:521)
	at akka.stream.impl.fusing.GraphInterpreterShell.processEvent(ActorGraphInterpreter.scala:625)
	at akka.stream.impl.fusing.ActorGraphInterpreter.akka$stream$impl$fusing$ActorGraphInterpreter$$processEvent(ActorGraphInterpreter.scala:800)
	at akka.stream.impl.fusing.ActorGraphInterpreter$$anonfun$receive$1.applyOrElse(ActorGraphInterpreter.scala:818)
	at akka.actor.Actor.aroundReceive(Actor.scala:537)
	at akka.actor.Actor.aroundReceive$(Actor.scala:535)
	at akka.stream.impl.fusing.ActorGraphInterpreter.aroundReceive(ActorGraphInterpreter.scala:716)
	at akka.actor.ActorCell.receiveMessage(ActorCell.scala:579)
	at akka.actor.ActorCell.invoke(ActorCell.scala:547)
	at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:270)
	at akka.dispatch.Mailbox.run(Mailbox.scala:231)
	at akka.dispatch.Mailbox.exec(Mailbox.scala:243)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:373)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1182)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1655)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1622)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:165)
  * */
  sealed trait Command
  final case class Process(text: String, replyTo: ActorRef[Master.Event])
    extends Command
      with CborSerializable

  def apply(): Behavior[Command] = Behaviors.setup[Command] { context =>
    context.log.debug(s"${context.self} subscribing to $RegistrationKey")

    context.system.receptionist ! Receptionist.Register(RegistrationKey, context.self)

    Behaviors.receiveMessage {
      case Process(text, replyTo) =>
        context.log.debug(s"processing $text")
        replyTo ! Master.CountedWords(processTask(text)) //adapter?
        Behaviors.same
    }
  }

  def processTask(text: String): Map[String, Int] = {
    text
      .split("\\W+")
      .foldLeft(Map.empty[String, Int]) { (mapAccumulator, word) =>
        mapAccumulator + (word -> (mapAccumulator.getOrElse(word, 0) + 1))
      }
  }
}
