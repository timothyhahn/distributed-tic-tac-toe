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
  implicit val timeout = Timeout(1 seconds)

  val actorSystem = ActorSystem()

  var board = TicTacToe.emptyBoard
  val players = for {
    x <- 1 to 50
  } yield TicTacToe.uuid

  players.foreach(player => actorSystem.actorOf(Props(new DumbActor), player))

  val results = for {
    x <- 1 to 10
    roster = TicTacToe.rng.shuffle(players)
    matchups = roster.take(players.length / 2) zip roster.takeRight(players.length / 2)
    games = matchups.map(matchup => (TicTacToe.uuid, matchup))
    results = games.map{game => 
      actorSystem.actorOf(Props(new GameActor)) ? new Game(game._1, game._2)
    }
    futureResults = Future.sequence(results)
  } yield Await.result(futureResults, 5 seconds)

  println(results.flatten.groupBy(x => x).map(a => (a._1, a._2.length)).toSeq.sortWith(_._2 > _._2))

  actorSystem.shutdown()
}
