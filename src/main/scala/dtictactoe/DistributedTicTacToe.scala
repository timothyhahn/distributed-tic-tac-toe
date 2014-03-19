package dtictactoe

// Akka Imports
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

// Scala Imports
import scala.concurrent.Await
import scala.concurrent.duration._

object DistributedTicTacToe extends App {
  implicit val timeout = Timeout(1 seconds)


  val emptyBoard: List[List[Char]] = List.fill[Char](3,3) { ' ' }

  val actorSystem = ActorSystem()
  val firstActor = actorSystem.actorOf(Props[DumbActor])
  val future = firstActor ? new Board(emptyBoard)
  val result = Await.result(future, timeout.duration).asInstanceOf[(Int, Int)]
  println(s"Board Received! $result")
  actorSystem.shutdown
  
}
