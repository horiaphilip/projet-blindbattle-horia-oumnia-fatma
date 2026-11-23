package fr.miage.blindbattle.web;

import fr.miage.blindbattle.domain.Difficulty;
import fr.miage.blindbattle.dto.ThemeDetailView;
import fr.miage.blindbattle.dto.ThemeView;
import fr.miage.blindbattle.service.CatalogueService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final CatalogueService service;

    public ThemeController(CatalogueService service) { this.service = service; }

    @GetMapping
    public List<ThemeView> list() { return service.themes(); }

    @GetMapping("/{id}")
    public ThemeDetailView get(@PathVariable("id") String id,
            @RequestParam(value = "difficulty", defaultValue = "EASY") Difficulty difficulty) {
        return service.theme(id, difficulty);
    }
}
