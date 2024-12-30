package languages.map.controllers;

import languages.map.models.Map;
import languages.map.services.MapService;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@RestController
public class MapController {
    private final MapService mapService;
    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/maps")
    public ResponseEntity<List<Map>> getMaps(){
        return ResponseEntity.ok().body(mapService.getMaps());
    }
    @GetMapping("/map/{map_id}")
    public ResponseEntity<Map> getMap(@PathVariable ObjectId map_id){
        return ResponseEntity.ok().body(mapService.getMapById(map_id));
    }

}
