package fr.miage.blindbattle.messaging;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SseBroadcasterTest {
    private final SseBroadcaster broadcaster = new SseBroadcaster();

    @Test void closedScreensAreForgotten() {
        broadcaster.subscribe("ABCDEF").complete();
        broadcaster.subscribe("ABCDEF");
        broadcaster.broadcast("ABCDEF", "1", "PLAYER_JOINED", "{}");
        assertThat(broadcaster.subscribers("ABCDEF")).isEqualTo(1);
    }

    @Test void gamesAreIsolated() {
        broadcaster.subscribe("AAAAAA");
        assertThat(broadcaster.subscribers("BBBBBB")).isZero();
    }
}
