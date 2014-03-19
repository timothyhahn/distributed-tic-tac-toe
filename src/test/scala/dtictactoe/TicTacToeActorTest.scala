package dtictactoe

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestActorRef}
import akka.pattern.ask
import akka.util.Timeout

import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import scala.concurrent.Await
import scala.concurrent.duration._


class DumbActorTest extends TestKit(ActorSystem("testSystem")) 
  with FlatSpecLike {

    implicit val timeout = Timeout(1 seconds)

    trait BoardTester {
      val emptyBoard: List[List[Char]] = List.fill[Char](3,3) { ' ' }
    }

    "A TicTacToeActor" should "know what valid moves are" in new BoardTester {

      class SimplestActor extends TicTacToeActor{ def calculateMove(board: List[List[Char]]) = (0, 0) }

      val ticTacToeActorRef: TestActorRef[SimplestActor] = TestActorRef(Props(new SimplestActor))
      val ticTacToeActor: SimplestActor = ticTacToeActorRef.underlyingActor

      val validEmptyMoves =  (for {
        x <- 0 to 2
        y <- 0 to 2
      } yield (x, y)).toList

      ticTacToeActor.validMoves(emptyBoard) should equal(validEmptyMoves)
    }
    
    "A DumbActor" should "return a valid move" in new BoardTester {
      val dumbActorRef = TestActorRef(Props(new DumbActor))
      val future = dumbActorRef ? new Board(emptyBoard)
      val result = Await.result(future, timeout.duration).asInstanceOf[(Int, Int)]
      result should not equal((-1, -1))
    }

}
