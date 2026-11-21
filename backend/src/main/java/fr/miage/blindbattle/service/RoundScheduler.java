package fr.miage.blindbattle.service;

import fr.miage.blindbattle.messaging.GameCommand;
import fr.miage.blindbattle.messaging.GameCommandSender;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class RoundScheduler {
    private final TaskScheduler scheduler;
    private final GameCommandSender commands;
    private final Duration pause;

    public RoundScheduler(TaskScheduler scheduler, GameCommandSender commands, @Value("${blindbattle.round.pause}") Duration pause) {
        this.scheduler = scheduler;
        this.commands = commands;
        this.pause = pause;
    }

    public void timeoutAt(String gameCode, long roundId, Instant at) {
        afterCommit(() -> scheduler.schedule(() -> commands.send(gameCode, GameCommand.roundTimeout(gameCode, roundId)), at));
    }

    public void nextRoundAfterPause(String gameCode) {
        afterCommit(() -> scheduler.schedule(() -> commands.send(gameCode, GameCommand.nextRound(gameCode)), Instant.now().plus(pause)));
    }

    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { action.run(); }
        });
    }
}
