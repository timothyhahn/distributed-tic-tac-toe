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

  var gamesPlayed = 0
  while(gamesPlayed < 10) {
    val roster = TicTacToe.rng.shuffle(players)
    val matchups = roster.take(players.length / 2) zip roster.takeRight(players.length / 2)
    val games = matchups.map(matchup => (TicTacToe.uuid, matchup))
    val results = games.map{game => 
      actorSystem.actorOf(Props(new GameActor)) ? new Game(game._1, game._2)
    }

    val futureResults = Future.sequence(results)
    val result = Await.result(futureResults, 5 seconds)
    println(result)

    gamesPlayed += 1
  }

  actorSystem.shutdown()
}
