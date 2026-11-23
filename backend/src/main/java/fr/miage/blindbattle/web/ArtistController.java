package fr.miage.blindbattle.web;

import fr.miage.blindbattle.dto.ArtistView;
import fr.miage.blindbattle.dto.TrackView;
import fr.miage.blindbattle.service.CatalogueService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/artists")
public class ArtistController {
    private final CatalogueService service;

    public ArtistController(CatalogueService service) { this.service = service; }

    @GetMapping("/search")
    public List<ArtistView> search(@RequestParam(value = "q", defaultValue = "") String query) {
        return service.searchArtists(query);
    }

    @GetMapping("/{id}/top")
    public List<TrackView> top(@PathVariable("id") long id) { return service.artistTop(id); }

    @GetMapping("/{id}/related")
    public List<ArtistView> related(@PathVariable("id") long id) { return service.related(id); }
}
