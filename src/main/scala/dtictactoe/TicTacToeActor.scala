package dtictactoe

// Akka Imports
import akka.actor.Actor

case class Board(board: List[List[Char]])

class DumbActor extends TicTacToeActor {
  def calculateMove(board: List[List[Char]]): (Int, Int) = {
    (1, 1)
  }

}

trait TicTacToeActor extends Actor {
  def calculateMove(board: List[List[Char]]): (Int, Int)
  def receive = {
    case board: Board =>
      sender ! calculateMove(board.board)
  }
}
