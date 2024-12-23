package languages.map.controllers;

import languages.map.models.Areas;
import languages.map.services.AreasService;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AreasController {
    private final AreasService areasService;

    public AreasController(AreasService areasService) {
        this.areasService = areasService;
    }
    @GetMapping({"/areas"})
    public ResponseEntity<List<Areas>> getAreas(){
        return ResponseEntity.ok().body(areasService.getAreas());
    }
    @GetMapping("/areas/{id}")
    public ResponseEntity<Areas> getAreasById(@PathVariable ObjectId id){
        return ResponseEntity.ok().body(areasService.getAreasById(id));
    }
    @PostMapping("/areas")
    public ResponseEntity<Areas> saveAreas(@RequestBody Areas areas){
        return ResponseEntity.ok().body(areasService.saveAreas(areas));
    }

    @PutMapping("/areas/{id}")
    public ResponseEntity<Areas> updateAreas(@RequestBody Areas areas, @PathVariable ObjectId id){
        return ResponseEntity.ok().body(areasService.updateAreas(id, areas));
    }
}
