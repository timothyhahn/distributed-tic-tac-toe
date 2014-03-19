package dtictactoe

// Akka Imports
import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout

// Scala Imports
import scala.concurrent.Await
import scala.concurrent.duration._


case class Game(matchup: (String, String))

class GameActor extends Actor {
  implicit val timeout = Timeout(2 seconds)
  def playGame(matchup: (String, String), board: List[List[Char]], xTurn: Boolean): String = {
    if(TicTacToe.isGameOver(board)) {
      ((TicTacToe.isWonBy(board, 'x'), TicTacToe.isWonBy(board, 'o')) match {
        case(true, _) =>
          matchup._1
        case(_, true) =>
          matchup._2
        case _ =>
          "DRAW"
      }) 
    } else {
      val actorName = xTurn match {
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
      playGame(matchup, TicTacToe.applyMove(board, move, player), !xTurn)
    }
  }
  

  def receive = {
    case Game(matchup: (String, String)) =>
      sender ! playGame(matchup, TicTacToe.emptyBoard, true)
  }
}
