package fr.miage.blindbattle.web;

import fr.miage.blindbattle.dto.TrackView;
import fr.miage.blindbattle.service.CatalogueService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tracks")
public class TrackController {
    private final CatalogueService service;

    public TrackController(CatalogueService service) { this.service = service; }

    @GetMapping("/search")
    public List<TrackView> search(@RequestParam(value = "q", defaultValue = "") String query) {
        return service.searchTracks(query);
    }
}
