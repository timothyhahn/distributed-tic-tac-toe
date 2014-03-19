package dtictactoe

// Akka Imports
import akka.actor.Actor

case class Board(board: List[List[Char]])

class DumbActor extends TicTacToeActor {

  def randomMove(moves: List[(Int, Int)]): (Int, Int) = moves(TicTacToe.rng.nextInt(moves.length)) 

  def calculateMove(board: List[List[Char]]): (Int, Int) = randomMove(validMoves(board))

}

trait TicTacToeActor extends Actor {

  def calculateMove(board: List[List[Char]]): (Int, Int)

  def validMoves(board: List[List[Char]]): List[(Int, Int)] = {
    (for {
      x <- 0 to 2
      y <- 0 to 2
      if(board(x)(y) == ' ')
    } yield (x, y)).toList
  }

  def receive = {
    case board: Board =>
      sender ! calculateMove(board.board)
  }
}
