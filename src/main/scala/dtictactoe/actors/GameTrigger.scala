package dtictactoe

import akka.actor.Actor

case object StartGame
class GameTrigger(players: List[(String, String)]) extends Actor {
  def receive = {
    case StartGame =>
      val ids = players.map(player => player._1)
      val roster = TicTacToe.rng.shuffle(ids)
      val matchups = roster.take(ids.length / 2) zip roster.takeRight(ids.length / 2)
      val games = matchups.map(matchup => (TicTacToe.uuid, matchup))
      games.foreach(game => context.system.actorSelection("user/GameSupervisor") ! new Game(game._2))
  }
}


