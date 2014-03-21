package dtictactoe

import org.mashupbots.socko.events.{HttpRequestEvent, WebSocketFrameEvent}

import akka.actor.Actor
import com.redis._

case class Record(player: String, score: Int, actorType: String)

class RedisHandler extends Actor {

  val r = new RedisClient("localhost", 6379)
  

  def receive = {
    case Record(player: String, score: Int, actorType: String) =>
      r.hset("players", player, actorType)
      r.hincrby("scores", player, score)

    case event: WebSocketFrameEvent =>
      event.writeText(r.hgetall("scores").getOrElse(Map[String, String]()).toString)
  }

}
