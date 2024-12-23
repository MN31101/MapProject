package languages.map.controllers;

import languages.map.models.Map;
import languages.map.services.MapService;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MapController {
    private final MapService mapService;
    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/maps")
    public ResponseEntity<List<Map>> getAllMaps(){
        return ResponseEntity.ok().body(mapService.getMaps());
    }
    @GetMapping("/maps/{id}")
    public ResponseEntity<Map> getMapById(@PathVariable ObjectId id){
        return ResponseEntity.ok().body(mapService.getMapById(id));
    }
    @PostMapping("/maps")
    public ResponseEntity<Map> saveMap(@RequestBody Map map){
        return ResponseEntity.ok().body(mapService.saveMap(map));
    }


}
