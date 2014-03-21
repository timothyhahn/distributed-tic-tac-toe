package dtictactoe

import org.mashupbots.socko.events.HttpRequestEvent

import akka.actor.Actor

class StaticHandler extends Actor {

  def receive = {
    case event: HttpRequestEvent =>
      if(event.request.is100ContinueExpected) {
        event.response.write100Continue()
      }
      event.response.write(scala.io.Source.fromURL(getClass.getResource("/index.html")).mkString, "text/html; charset=UTF-8")
      context.stop(self)
  }

}
