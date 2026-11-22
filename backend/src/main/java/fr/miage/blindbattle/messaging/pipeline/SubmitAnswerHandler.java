package fr.miage.blindbattle.messaging.pipeline;

import fr.miage.blindbattle.messaging.GameCommandType;
import fr.miage.blindbattle.service.AnswerService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class SubmitAnswerHandler implements GameCommandHandler {
    private final JsonMapper mapper;
    private final AnswerService answers;

    public SubmitAnswerHandler(JsonMapper mapper, AnswerService answers) {
        this.mapper = mapper;
        this.answers = answers;
    }

    @Override
    public GameCommandType type() { return GameCommandType.SUBMIT_ANSWER; }

    @Override
    public void handle(String body) { answers.process(mapper.readValue(body, SubmitAnswer.class)); }
}
