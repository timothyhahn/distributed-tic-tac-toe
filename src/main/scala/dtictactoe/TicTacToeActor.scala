package dtictactoe

// Akka Imports
import akka.actor.Actor

case object Ping
case object Pong

class TicTacToeActor extends Actor {
  def receive = {
    case Ping =>
      println("Received Ping")
      sender ! Pong

  }
}
