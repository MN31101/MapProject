package languages.map.controllers;

import languages.map.dto.BoundingBoxRequest;
import languages.map.models.LanguagesZone;
import languages.map.services.LanguagesZoneService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@CrossOrigin(origins = {"http://127.0.0.1:8000", "http://localhost:8000"})
@RestController
public class LanguagesZoneController {
    private static final Logger logger = LoggerFactory.getLogger(LanguagesZoneController.class);
    private final LanguagesZoneService languagesZoneService;

    public LanguagesZoneController(LanguagesZoneService languagesZoneService) {
        this.languagesZoneService = languagesZoneService;
    }
    /**
     * @param year - year that wanted
     * @return - List of Areas that related to year and map
     */
    @GetMapping("/all/{year}")
    public ResponseEntity<List<LanguagesZone>> getLanguagesZone(@PathVariable Integer year){
        logger.info("Fetching all language zones for year: {}", year);
        try {
            List<LanguagesZone> zones = languagesZoneService.getLanguagesZoneByYear(year);
            logger.debug("Retrieved {} zones for year {}", zones.size(), year);
            return ResponseEntity.ok().body(zones);
        } catch (Exception e) {
            logger.error("Failed to fetch language zones for year: {}", year, e);
            throw e;
        }
    }

    @PostMapping("/areas/{year}")
    public ResponseEntity<List<LanguagesZone>> getLanguagesZonesRelatedToChunk(
            @RequestBody BoundingBoxRequest boundingBoxRequest,
            @PathVariable Integer year) {
        logger.info("Fetching language zones for year {} within bounding box", year);
        logger.debug("Bounding box request: {}", boundingBoxRequest);

        if (boundingBoxRequest.getLeftTopPointLatLon() == null
                || boundingBoxRequest.getRightBottomPointLatLon() == null
                || boundingBoxRequest.getLeftTopPointLatLon().length != 2
                || boundingBoxRequest.getRightBottomPointLatLon().length != 2) {
            logger.error("Invalid bounding box coordinates provided");
            throw new IllegalArgumentException("Invalid bounding box coordinates");
        }
        try {
            List<LanguagesZone> zones = languagesZoneService.getLanguagesZone(boundingBoxRequest, year);
            logger.debug("Retrieved {} zones", zones.size());
            return ResponseEntity.ok().body(zones);
        } catch (Exception e) {
            logger.error("Failed to fetch language zones for bounding box", e);
            throw e;
        }
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
        logger.info("Creating new language zone");
        logger.debug("Language zone data: {}", languagesZone);
        try {
            LanguagesZone created = languagesZoneService.saveLanguageZone(languagesZone);
            logger.info("Successfully created language zone with id: {}", created.getId());
            return ResponseEntity.ok().body(created);
        } catch (Exception e) {
            logger.error("Failed to create language zone", e);
            throw e;
        }
    }


    /**
     * @param area_id id of languagesZone which wanted to update
     * @param languagesZone body of future LanguagesZone
     * @return return into repository new LanguagesZone`s body
     */
    @PutMapping("/area/{area_id}")
    public ResponseEntity<LanguagesZone> updateLanguagesZone(
            @PathVariable ObjectId area_id,
            @RequestBody LanguagesZone languagesZone){
        logger.info("Updating language zone with id: {}", area_id);
        logger.debug("Updated language zone data: {}", languagesZone);
        try {
            LanguagesZone updated = languagesZoneService.updateLanguageZone(area_id, languagesZone);
            logger.info("Successfully updated language zone with id: {}", area_id);
            return ResponseEntity.ok().body(updated);
        } catch (Exception e) {
            logger.error("Failed to update language zone with id: {}", area_id, e);
            throw e;
        }
    }

    @DeleteMapping("/area")
    public ResponseEntity<LanguagesZone> deleteLanguageZone(@RequestBody LanguagesZone languageZone){
        logger.info("Deleting language zone with id: {}", languageZone.getId());
        logger.debug("Deleted language zone data: {}", languageZone);
        try {
            languagesZoneService.deleteLanguageZone(languageZone);
            logger.info("Successfully delete language zone with id: {}", languageZone.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to delete language zone with id: {}", languageZone.getId(), e);
            throw e;
        }
    }
}
