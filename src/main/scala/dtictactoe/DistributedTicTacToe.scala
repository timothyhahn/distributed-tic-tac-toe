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

  val players = for {
    x <- 1 to playerCount
  } yield TicTacToe.uuid

  players.foreach(player => actorSystem.actorOf(Props[SmartActor], player))

  val results = for {
    x <- 1 to gameCount
    roster = TicTacToe.rng.shuffle(players)
    matchups = roster.take(players.length / 2) zip roster.takeRight(players.length / 2)
    games = matchups.map(matchup => (TicTacToe.uuid, matchup))
    results = games.map{game => 
      gameSupervisor ? new Game(game._2)
    }
    futureResults = Future.sequence(results)
  } yield Await.result(futureResults, timeout.duration)

  println(results.flatten.groupBy(x => x).map(a => (a._1, a._2.length)).toSeq.sortWith(_._2 > _._2))

  actorSystem.shutdown()
}
