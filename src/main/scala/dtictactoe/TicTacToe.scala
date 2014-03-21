package dtictactoe

object TicTacToe {
  def isFull(board: List[List[Char]]): Boolean = !(board.flatten exists(_ == ' '))

  def isWonBy(board: List[List[Char]], player: Char): Boolean = {
    (board ++ board.transpose  // Horizontal and Vertical Wins
      :+ board.flatten.grouped(4).map(_.head)  // Diagonal Wins
      :+ board.flatten.drop(2).dropRight(1).grouped(2).map(_.head)).exists(set => set.foldLeft(true)((a,b) => a && b == player))
  }

  def isWon(board: List[List[Char]]): Boolean = isWonBy(board, 'x') || isWonBy(board, 'o')

  def isGameOver(board: List[List[Char]]): Boolean = isFull(board) || isWon(board)

  def applyMove(board: List[List[Char]], move: (Int, Int), player: Char): List[List[Char]] = {
    move match {
      case (x: Int, y: Int) =>
        board.patch(x, List(board(x).patch(y, Seq(player), 1)), 1)
    }
  }

  def otherPlayer(player: Char): Char = {
    player match {
      case 'x' =>
        'o'
      case 'o' =>
        'x'
      case _ =>
        ' '
    }
  }

  def uuid = java.util.UUID.randomUUID.toString

  val emptyBoard: List[List[Char]] = List.fill[Char](3,3) { ' ' }

  val rng = new util.Random(System.currentTimeMillis)
}
