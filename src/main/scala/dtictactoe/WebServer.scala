package dtictactoe

import org.mashupbots.socko.events.HttpResponseStatus
import org.mashupbots.socko.routes._
import org.mashupbots.socko.webserver.{WebServer, WebServerConfig}

import akka.actor.{ActorSystem, Props}

class Server(actorSystem: ActorSystem) {
  val routes = Routes({
    case HttpRequest(httpRequest) => httpRequest match {
      case GET(Path("/")) => 
        actorSystem.actorOf(Props[StaticHandler]) ! httpRequest
      case _ =>
        httpRequest.response.write(HttpResponseStatus.NOT_FOUND)
    }

    case WebSocketHandshake(wsHandshake) => wsHandshake match {
      case Path("/ws") => 
        wsHandshake.authorize()
    }

    case WebSocketFrame(wsFrame) =>
      actorSystem.actorOf(Props[RedisHandler]) ! wsFrame
  })

  def start() {
    val webServer = new WebServer(WebServerConfig(), routes, actorSystem)
    Runtime.getRuntime.addShutdownHook(new Thread { override def run { webServer.stop() } })

    webServer.start()
    System.out.println("Web Server started")
  }
  
}
