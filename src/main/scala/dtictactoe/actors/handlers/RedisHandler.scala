package dtictactoe

import org.mashupbots.socko.events.{HttpRequestEvent, WebSocketFrameEvent}

import akka.actor.Actor
import com.redis._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import java.net.URI

case class Player(name: String, actorType: String)
case class Win(player: String)

class RedisHandler extends Actor {

  val uri = new URI(sys.env.get("REDISCLOUD_URL").getOrElse("http://localhost:6379"))
  val r = new RedisClient(uri.getHost, uri.getPort)

  def convertScore(score: String): Int = {
    try{
      score.toInt
    } catch{
      case nfe: NumberFormatException =>
        return 0
    }
  }

  def receive = {
    case Player(name: String, actorType: String) =>
      r.hset("players", name, actorType)
    case Win(player: String) =>
      r.hincrby("scores", player, 1)
    case event: HttpRequestEvent =>
      // Haha this is terrible :)
      val players = (r.hgetall("players").getOrElse(Map[String, String]()).toSeq ++ r.hgetall("scores").getOrElse(Map[String, String]()).toSeq).groupBy(_._1).mapValues(_.map(_._2).toList).map(player =>
        (player._1.toString, convertScore(player._2.last.toString), player._2.head.toString)).filter(_._1 != "DRAW").toList.sortWith(_._2 > _._2)
      val teams = players.groupBy(_._3).map(team => (team._1, team._2.foldLeft(0)((a, b) => a + b._2)))
      val json = ("scores" ->
      ("players" -> players.map {
        player => 
        (("name" -> player._1) ~
         ("score" -> player._2) ~
         ("type" -> player._3))
        }) ~
      ("teams" -> teams.map {
        team =>
        (("name" -> team._1) ~
         ("score" -> team._2))
        }))
      event.response.write(compact(render(json)))
  }

}
