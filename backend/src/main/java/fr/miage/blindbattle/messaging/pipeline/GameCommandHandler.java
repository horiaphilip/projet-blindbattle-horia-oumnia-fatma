package fr.miage.blindbattle.messaging.pipeline;

import fr.miage.blindbattle.messaging.GameCommandType;

public interface GameCommandHandler {
    GameCommandType type();
    void handle(String body);
}
