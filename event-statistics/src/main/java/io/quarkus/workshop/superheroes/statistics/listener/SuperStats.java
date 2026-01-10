package io.quarkus.workshop.superheroes.statistics.listener;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.quarkus.logging.Log;

import io.quarkus.sample.superheroes.fight.schema.Fight;
import io.quarkus.workshop.superheroes.statistics.domain.Score;
import io.quarkus.workshop.superheroes.statistics.domain.TeamScore;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.annotations.Broadcast;

/**
 * Consumer of {@link Fight} events from Kafka. There are 2 consumers for performing different aggregations. Each consumer writes out to its own in-memory channel.
 */
@ApplicationScoped
public class SuperStats {
  static final String FIGHTS_CHANNEL_NAME = "fights";
  static final String TEAM_STATS_CHANNEL_NAME = "team-stats";
  static final String TOP_WINNERS_CHANNEL_NAME = "winner-stats";

  private final Ranking topWinners = new Ranking(10);
  private final TeamStats stats = new TeamStats();

  private final MutinyEmitter<TeamScore> teamStatsEmitter;

  public SuperStats(@Broadcast @Channel(TEAM_STATS_CHANNEL_NAME) MutinyEmitter<TeamScore> teamStatsEmitter) {
    this.teamStatsEmitter = teamStatsEmitter;
  }

  /**
   * Processes a stream of {@link Fight}s. Computes {@link #computeTeamStats(io.quarkus.sample.superheroes.fight.schema.Fight) team stats}
   * and {@link #computeTopWinners(io.smallrye.mutiny.Multi) top winners}
   * @param fights
   * @return
   */
  @Incoming(FIGHTS_CHANNEL_NAME)
  @Outgoing(TOP_WINNERS_CHANNEL_NAME)
  @Broadcast
  public Multi<Message<Iterable<Score>>> processFight(Multi<Message<Fight>> fights) {
    Multi<Fight> fightStream = fights
      .call(message -> computeTeamStats(message.getPayload()))
      .map(Message::getPayload);
    
    return computeTopWinners(fightStream)
      .map(Message::of);
  }

  /**
   * Transforms the {@link Fight} stream into a stream of {@link io.quarkus.workshop.superheroes.statistics.domain.TeamScore scores}.
   * Each score indicates the running percentage of battles won by heroes.
   * @param fight The {@link Fight} to compute
   * @return A continuous stream of percentages of battles won by heroes sent to the {@code team-stats} in-memory channel.
   */
  Uni<Void> computeTeamStats(Fight fight) {
    Log.debugf("[computeTeamStats] - Got message: %s", fight);

    return Uni.createFrom().item(() -> this.stats.add(fight))
      .invoke(score -> Log.debugf("[computeTeamStats] - Computed the team statistics: %s", score))
      .chain(this.teamStatsEmitter::send);
  }

  /**
   * Transforms the {@link Fight} stream into a running stream of top winners.
   * <p>
   *   The incoming stream is first grouped by {@link Fight#getWinnerName}. Then the number of wins for that winner is computed.
   * </p>
   * @param fights The {@link Fight} continuous stream
   * @return A continuous stream of the top 10 winners and the number of wins for each winner
   */
  private Multi<Iterable<Score>> computeTopWinners(Multi<Fight> fights) {
    return fights
      .invoke(fight -> Log.debugf("[computeTopWinners] - Got message: %s", fight))
      .group().by(Fight::getWinnerName)
      .flatMap(group ->
        group.onItem().scan(Score::new, this::incrementScore)
          .filter(score -> score.name() != null)
      )
      .map(this.topWinners::onNewScore)
      .invoke(winners -> Log.debugf("[computeTopWinners] - Computed the top winners: %s", winners));
  }

  private Score incrementScore(Score score, Fight fight) {
    return new Score(fight.getWinnerName(), score.score() + 1);
  }

  TeamStats getTeamStats() {
    return this.stats;
  }
}
