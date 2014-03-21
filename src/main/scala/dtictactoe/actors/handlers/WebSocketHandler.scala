package dtictactoe

import org.mashupbots.socko.webserver.WebSocketConnections

import akka.actor.Actor

class WebSocketHandler(connections: WebSocketConnections) extends Actor {
  def receive = {
    case message: String =>
      connections.writeText(message)
  }
}
