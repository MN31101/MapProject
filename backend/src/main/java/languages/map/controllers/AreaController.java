package languages.map.controllers;

import languages.map.models.Area;
import languages.map.services.AreaService;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@RestController
public class AreaController {
    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }
    @GetMapping("/areas/{map_id}/{year}")
    public ResponseEntity<List<Area>> getAreas(@PathVariable ObjectId map_id,
                                               @PathVariable Integer year){
        return ResponseEntity.ok().body(areaService.getAreasByYear(map_id, year));
    }
    @GetMapping("/area/{area_id}")
    public ResponseEntity<Area> getArea(@PathVariable ObjectId area_id){
        return ResponseEntity.ok().body(areaService.getAreaById(area_id));
    }
    @PostMapping("/area/{map_id}")
    public ResponseEntity<Area> createArea(@PathVariable ObjectId map_id,
                                           @RequestBody Area area){
        return ResponseEntity.ok().body(areaService.saveArea(area, map_id));
    }
    @PutMapping("/area/{area_id}")
    public ResponseEntity<Area> updateArea(@PathVariable ObjectId area_id,
                                           @RequestBody Area area){
        return ResponseEntity.ok().body(areaService.updateArea(area_id, area));
    }
}
