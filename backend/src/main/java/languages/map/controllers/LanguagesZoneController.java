package languages.map.controllers;

import languages.map.dto.BoundingBoxRequest;
import languages.map.models.LanguagesZone;
import languages.map.services.LanguagesZoneService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/all/{year}")
    public ResponseEntity<List<LanguagesZone>> getLanguagesZone(@PathVariable Integer year){
        logger.info("Fetching all language zones for year: {}", year);
        try {
            List<LanguagesZone> zones = languagesZoneService.getLanguagesZoneByYear(year);
            logger.info("Retrieved {} zones for year {}", zones.size(), year);  // Changed from debug to info
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

        if (boundingBoxRequest.getLeftTopPointLatLon() == null
                || boundingBoxRequest.getRightBottomPointLatLon() == null
                || boundingBoxRequest.getLeftTopPointLatLon().length != 2
                || boundingBoxRequest.getRightBottomPointLatLon().length != 2) {
            logger.warn("Invalid bounding box coordinates provided");  // Changed to warn
            throw new IllegalArgumentException("Invalid bounding box coordinates");
        }
        try {
            List<LanguagesZone> zones = languagesZoneService.getLanguagesZone(boundingBoxRequest, year);
            return ResponseEntity.ok().body(zones);
        } catch (Exception e) {
            logger.error("Failed to fetch language zones for bounding box", e);
            throw e;
        }
    }

    @PostMapping("/area")
    public ResponseEntity<LanguagesZone> createLanguageZone(@RequestBody LanguagesZone languagesZone){
        try {
            LanguagesZone created = languagesZoneService.saveLanguageZone(languagesZone);
            return ResponseEntity.ok().body(created);
        } catch (Exception e) {
            logger.error("Failed to create language zone", e);
            throw e;
        }
    }

    @PutMapping("/area/{area_id}")
    public ResponseEntity<LanguagesZone> updateLanguagesZone(
            @PathVariable ObjectId area_id,
            @RequestBody LanguagesZone languagesZone){
        try {
            LanguagesZone updated = languagesZoneService.updateLanguageZone(area_id, languagesZone);
            return ResponseEntity.ok().body(updated);
        } catch (Exception e) {
            logger.error("Failed to update language zone with id: {}", area_id, e);
            throw e;
        }
    }

    @DeleteMapping("/area")
    public ResponseEntity<?> deleteLanguageZone(@RequestBody LanguagesZone languageZone) {
        try {
            languagesZoneService.deleteLanguageZone(languageZone);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to delete language zone with id: {}", languageZone.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete language zone");
        }
    }

    @DeleteMapping("/areas/all")
    public ResponseEntity<?> deleteAllLanguageZone(@RequestBody LanguagesZone languageZone) {
        try {
            languagesZoneService.deleteAllLanguageZone();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to delete all language zones", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete all language zones");
        }
    }
}

