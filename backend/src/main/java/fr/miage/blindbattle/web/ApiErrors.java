package fr.miage.blindbattle.web;

import fr.miage.blindbattle.service.EmailAlreadyUsed;
import fr.miage.blindbattle.service.GameAlreadyStarted;
import fr.miage.blindbattle.service.GameNotFound;
import fr.miage.blindbattle.service.InvalidCredentials;
import fr.miage.blindbattle.service.NicknameTaken;
import fr.miage.blindbattle.service.NoPlayers;
import fr.miage.blindbattle.service.NotHost;
import fr.miage.blindbattle.service.NotOwner;
import fr.miage.blindbattle.service.StatsNotReady;
import fr.miage.blindbattle.service.TrackPoolTooSmall;
import fr.miage.blindbattle.service.UserNotFound;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler({UserNotFound.class, StatsNotReady.class, GameNotFound.class})
    public ProblemDetail notFound(RuntimeException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, error.getMessage());
    }

    @ExceptionHandler({EmailAlreadyUsed.class, GameAlreadyStarted.class, NicknameTaken.class, NoPlayers.class,
            TrackPoolTooSmall.class})
    public ProblemDetail conflict(RuntimeException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, error.getMessage());
    }

    @ExceptionHandler(InvalidCredentials.class)
    public ProblemDetail unauthorized(InvalidCredentials error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, error.getMessage());
    }

    @ExceptionHandler({NotOwner.class, NotHost.class})
    public ProblemDetail forbidden(RuntimeException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, error.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalid(MethodArgumentNotValidException error) {
        Map<String, String> errors = new LinkedHashMap<>();
        error.getBindingResult().getFieldErrors().forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Requête invalide");
        problem.setProperty("errors", errors);
        return problem;
    }
}
