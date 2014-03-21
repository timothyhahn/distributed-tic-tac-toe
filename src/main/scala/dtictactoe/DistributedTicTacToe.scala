package dtictactoe

// Akka Imports
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask

object DistributedTicTacToe extends App {
  val actorSystem = ActorSystem()
  val gameSupervisor = actorSystem.actorOf(Props[GameSupervisor], "GameSupervisor")
  val redisHandler = actorSystem.actorOf(Props[RedisHandler], "RedisHandler")

  val playerCount = 50

  def pickActorType(actorType: Int): String = {
    actorType match {
      case 1 =>
        "Smart"
      case _ =>
        "Dumb"
    }
  }

  val players = for {
    x <- 1 to playerCount
    actorType = pickActorType(TicTacToe.rng.nextInt(2))
  } yield (TicTacToe.uuid, actorType)

  val gameTrigger = actorSystem.actorOf(Props(new GameTrigger(players.toList)), "GameTrigger")

  players.foreach{player => 
    val name = player._1
    player._2 match {
      case "Dumb" =>
        actorSystem.actorOf(Props[DumbActor], name)
      case "Smart" =>
        actorSystem.actorOf(Props[SmartActor], name)
    }
    redisHandler ! new Player(player._1, player._2) 
  }

  val server = new Server(actorSystem)
  server.start()
}
