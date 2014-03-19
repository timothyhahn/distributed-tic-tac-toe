package dtictactoe

// Akka Imports
import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout

// Scala Imports
import scala.concurrent.Await
import scala.concurrent.duration._


case class Game(uuid: String, matchup: (String, String))

class GameActor extends Actor {
  implicit val timeout = Timeout(1 seconds)

  def receive = {
    case game: Game =>
      val matchup = game.matchup
      var board = TicTacToe.emptyBoard
      var xTurn = true 

      while(!TicTacToe.isGameOver(board)) {
        val actorName = xTurn match{
          case true => matchup._1
          case false => matchup._2
        }
        val actorSelection = context.system.actorSelection(s"user/$actorName")
        val player = xTurn match {
          case true => 'x'
          case false => 'o'
        }

        val future = actorSelection ? new Board(board)
        val move = Await.result(future, timeout.duration).asInstanceOf[(Int, Int)]
        board = TicTacToe.applyMove(board, move, player)
        xTurn = !xTurn
      }

      sender ! ((TicTacToe.isWonBy(board, 'x'), TicTacToe.isWonBy(board, 'o')) match {
        case(true, _) =>
          matchup._1
        case(_, true) =>
          matchup._2
        case _ =>
          "DRAW"
      })
  }

}
