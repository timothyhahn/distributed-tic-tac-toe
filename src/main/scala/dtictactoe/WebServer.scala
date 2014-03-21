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
      case GET(Path("/scores")) =>
        actorSystem.actorSelection("user/RedisHandler") ! httpRequest
      case _ =>
        httpRequest.response.write(HttpResponseStatus.NOT_FOUND)
    }

    case WebSocketHandshake(wsHandshake) => wsHandshake match {
      case Path("/ws") => 
        wsHandshake.authorize()
    }

    case WebSocketFrame(wsFrame) =>
      actorSystem.actorSelection("user/GameTrigger") ! StartGame 
  })

  def start() {
    val webServer = new WebServer(WebServerConfig(hostname="0.0.0.0", port=sys.env.get("PORT").getOrElse("8888").toInt), routes, actorSystem)
    actorSystem.actorOf(Props(new WebSocketHandler(webServer.webSocketConnections)), "WebSocketHandler")
    Runtime.getRuntime.addShutdownHook(new Thread { override def run { webServer.stop(); actorSystem.shutdown() } })

    webServer.start()
    System.out.println("Web Server started")
  }
  
}
