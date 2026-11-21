package fr.miage.blindbattle.messaging;

public final class Destinations {
    public static final String PREFIX = "${blindbattle.jms.prefix:}";
    public static final String USER_EVENTS = "user.events";
    public static final String GAME_COMMANDS = "game.commands";
    public static final String GAME_EVENTS = "game.events";

    private Destinations() {}
}
