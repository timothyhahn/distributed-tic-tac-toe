package dtictactoe

// Akka Imports
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

// Scala Imports
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

object DistributedTicTacToe extends App {
  implicit val timeout = Timeout(60 seconds)

  val actorSystem = ActorSystem()
  val gameSupervisor = actorSystem.actorOf(Props[GameSupervisor])
  val playerCount = 50
  val gameCount = 1000

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

  players.foreach{player => 
    val name = player._1
    player._2 match {
      case "Dumb" =>
        actorSystem.actorOf(Props[DumbActor], name)
      case "Smart" =>
        actorSystem.actorOf(Props[SmartActor], name)
    }
  }

  val results = for {
    x <- 1 to gameCount
    ids = players.map(player => player._1)
    roster = TicTacToe.rng.shuffle(ids)
    matchups = roster.take(ids.length / 2) zip roster.takeRight(ids.length / 2)
    games = matchups.map(matchup => (TicTacToe.uuid, matchup))
    results = games.map{game => 
      gameSupervisor ? new Game(game._2)
    }
    futureResults = Future.sequence(results)
  } yield Await.result(futureResults, timeout.duration)

  def playerType(name: String): String = {
    val search = players.find(player => player._1 == name)
    search match {
      case Some(player: (String, String)) =>
        player._2
      case _ =>
        "Unknown"
    }
  }

  println(results.flatten.groupBy(x => x).map(a => (a._1, a._2.length, playerType(a._1.toString))).toSeq.sortWith(_._2 > _._2))

  val server = new Server(actorSystem)

  server.start()
  while(true) {
  }
  actorSystem.shutdown()
}
