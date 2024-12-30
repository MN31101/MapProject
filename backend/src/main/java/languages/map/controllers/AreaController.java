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

    /**
     * @param map_id - id map for what Areas wanted
     * @param year - year that wanted
     * @return - List of Areas that related to year and map
     */
    @GetMapping("/areas/{map_id}/{year}")
    public ResponseEntity<List<Area>> getAreas(@PathVariable ObjectId map_id,
                                               @PathVariable Integer year){
        return ResponseEntity.ok().body(areaService.getAreasByYear(map_id, year));
    }

    /**
     * @param area_id - id of Area
     * @return - Area`s body by id
     */
    @GetMapping("/area/{area_id}")
    public ResponseEntity<Area> getArea(@PathVariable ObjectId area_id){
        return ResponseEntity.ok().body(areaService.getAreaById(area_id));
    }

    /**
     * @param area new Body of future Area
     * @return return into repository new Area
     */
    @PostMapping("/area")
    public ResponseEntity<Area> createArea(@RequestBody Area area){
        return ResponseEntity.ok().body(areaService.saveArea(area));
    }

    /**
     * @param area_id id of area which wanted to update
     * @param area body of future Area
     * @return return into repository new Area`s body
     */
    @PutMapping("/area/{area_id}")
    public ResponseEntity<Area> updateArea(@PathVariable ObjectId area_id,
                                           @RequestBody Area area){
        return ResponseEntity.ok().body(areaService.updateArea(area_id, area));
    }
}
