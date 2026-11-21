package fr.miage.blindbattle.messaging.pipeline;

import fr.miage.blindbattle.messaging.Destinations;
import fr.miage.blindbattle.messaging.GameCommand;
import fr.miage.blindbattle.messaging.GameCommandType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class GameCommandRouter {
    private static final Logger LOG = LoggerFactory.getLogger(GameCommandRouter.class);
    private final JsonMapper mapper;
    private final Map<GameCommandType, GameCommandHandler> handlers;

    public GameCommandRouter(JsonMapper mapper, List<GameCommandHandler> handlers) {
        this.mapper = mapper;
        this.handlers = handlers.stream().collect(Collectors.toMap(GameCommandHandler::type, Function.identity()));
    }

    @JmsListener(destination = Destinations.PREFIX + Destinations.GAME_COMMANDS)
    public void route(String body) {
        GameCommand command = mapper.readValue(body, GameCommand.class);
        GameCommandHandler handler = handlers.get(command.type());
        if (handler == null) {
            LOG.warn("Aucun traitement pour la commande {} de la partie {}", command.type(), command.gameCode());
            return;
        }
        LOG.info("Routage {} partie={} vers {}", command.type(), command.gameCode(), handler.getClass().getSimpleName());
        handler.handle(body);
    }
}
