package languages.map.controllers;

import languages.map.dto.BoundingBoxRequest;
import languages.map.models.LanguagesZone;
import languages.map.services.LanguagesZoneService;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@RestController
public class LanguagesZoneController {
    private final LanguagesZoneService languagesZoneService;

    public LanguagesZoneController(LanguagesZoneService languagesZoneService) {
        this.languagesZoneService = languagesZoneService;
    }

    /**
     * @param year - year that wanted
     * @return - List of Areas that related to year and map
     */
    @GetMapping("/areas/all/{year}")
    public ResponseEntity<List<LanguagesZone>> getLanguagesZone(@PathVariable Integer year){
        return ResponseEntity.ok().body(languagesZoneService.getLanguagesZoneByYear(year));
    }

    @GetMapping("/areas/{year}")
    public ResponseEntity<List<LanguagesZone>> getLanguagesZonesRelatedToChunk( @RequestBody BoundingBoxRequest boundingBoxRequest,
                                                                                @PathVariable Integer year){
        return ResponseEntity.ok().body(languagesZoneService.getLanguagesZone(boundingBoxRequest, year));
    }

    /**
     * @param area_id - id of LanguagesZone
     * @return - LanguagesZone`s body by id
     */
    @GetMapping("/area/{area_id}")
    public ResponseEntity<LanguagesZone> getLanguagesZone(@PathVariable ObjectId area_id){
        return ResponseEntity.ok().body(languagesZoneService.getLanguageZoneById(area_id));
    }

    /**
     * @param languagesZone new Body of future LanguagesZone
     * @return return into repository new LanguagesZone
     */
    @PostMapping("/area")
    public ResponseEntity<LanguagesZone> createLanguageZone(@RequestBody LanguagesZone languagesZone){
        return ResponseEntity.ok().body(languagesZoneService.saveLanguageZone(languagesZone));
    }

    /**
     * @param area_id id of languagesZone which wanted to update
     * @param languagesZone body of future LanguagesZone
     * @return return into repository new LanguagesZone`s body
     */
    @PutMapping("/area/{area_id}")
    public ResponseEntity<LanguagesZone> updateLanguagesZone(@PathVariable ObjectId area_id,
                                                             @RequestBody LanguagesZone languagesZone){
        return ResponseEntity.ok().body(languagesZoneService.updateLanguageZone(area_id, languagesZone));
    }
}
