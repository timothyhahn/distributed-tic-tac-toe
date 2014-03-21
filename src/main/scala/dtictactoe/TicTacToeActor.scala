package dtictactoe

// Akka Imports
import akka.actor.Actor

// Scala Imports
import scala.collection.breakOut

case class Board(board: List[List[Char]], player: Char)

class DumbActor extends TicTacToeActor {

  def randomMove(moves: List[(Int, Int)]): (Int, Int) = moves(TicTacToe.rng.nextInt(moves.length)) 

  def calculateMove(board: List[List[Char]], player: Char): (Int, Int) = randomMove(validMoves(board))

}

class SmartActor extends DumbActor {

  def score(board: List[List[Char]], move: (Int, Int), player: Char): Int = {
    val playedBoard = TicTacToe.applyMove(board, move, player)
    val isWonByMe = TicTacToe.isWonBy(playedBoard, player)
    val isWonByOther = TicTacToe.isWonBy(playedBoard, TicTacToe.otherPlayer(player))
    (isWonByMe, isWonByOther) match {
      case(true, _) =>
        1
      case(_, true) =>
        -1
      case _ =>
        0
    }
  }

  def bestMoves(board: List[List[Char]], moves: List[(Int, Int)], player: Char): List[(Int, Int)] = {
    val scores = moves.map(move => (move, score(board, move, player)))(breakOut).toMap
    val bestScore = scores.values.max
    scores.filter(score => score._2 == bestScore).keys.toList
  }

  override def calculateMove(board: List[List[Char]], player: Char): (Int, Int) = randomMove(bestMoves(board, validMoves(board), player))

}


trait TicTacToeActor extends Actor {

  def calculateMove(board: List[List[Char]], player: Char): (Int, Int)

  def validMoves(board: List[List[Char]]): List[(Int, Int)] = {
    (for {
      x <- 0 to 2
      y <- 0 to 2
      if(board(x)(y) == ' ')
    } yield (x, y)).toList
  }

  def receive = {
    case board: Board =>
      sender ! calculateMove(board.board, board.player)
  }
}
