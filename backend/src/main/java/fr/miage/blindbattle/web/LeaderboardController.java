package fr.miage.blindbattle.web;

import fr.miage.blindbattle.dto.LeaderboardEntryView;
import fr.miage.blindbattle.dto.PageView;
import fr.miage.blindbattle.service.GameResultService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {
    private final GameResultService results;

    public LeaderboardController(GameResultService results) { this.results = results; }

    @GetMapping
    public PageView<LeaderboardEntryView> leaderboard(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return results.leaderboard(page, size);
    }
}
