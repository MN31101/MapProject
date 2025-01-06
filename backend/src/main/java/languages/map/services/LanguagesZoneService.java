package languages.map.services;

import languages.map.dto.BoundingBoxRequest;
import languages.map.models.LanguagesZone;
import languages.map.repositories.LanguagesZoneRepository;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LanguagesZoneService {
    private final LanguagesZoneRepository languagesZoneRepository;

    public LanguagesZoneService(LanguagesZoneRepository languagesZoneRepository) {
        this.languagesZoneRepository = languagesZoneRepository;
    }


    public List<LanguagesZone> getLanguagesZone(@NotNull BoundingBoxRequest boundingBoxRequest, Integer year){
        if (boundingBoxRequest.getLeftTopPointLatLon() == null
                || boundingBoxRequest.getRightBottomPointLatLon() == null
                || boundingBoxRequest.getLeftTopPointLatLon().length != 2
                || boundingBoxRequest.getRightBottomPointLatLon().length != 2) {
            throw new IllegalArgumentException("Invalid bounding box coordinates");
        }

        final var boundingBox = getPoints(boundingBoxRequest);

        return languagesZoneRepository.findAllByYearAndCoordsWithin(year, boundingBox)
                .orElseThrow(() -> new RuntimeException("No language zones found for year: " + year));
    }

    private static @NotNull GeoJsonPolygon getPoints(@NotNull BoundingBoxRequest boundingBoxRequest) {
        double x1 = boundingBoxRequest.getLeftTopPointLatLon()[0];
        double y1 = boundingBoxRequest.getLeftTopPointLatLon()[1];
        double x2 = boundingBoxRequest.getRightBottomPointLatLon()[0];
        double y2 = boundingBoxRequest.getRightBottomPointLatLon()[1];


        return new GeoJsonPolygon(
                List.of(
                        new GeoJsonPoint(x1, y1),
                        new GeoJsonPoint(x1, y2),
                        new GeoJsonPoint(x2, y2),
                        new GeoJsonPoint(x2,y1),
                        new GeoJsonPoint(x1,y1)
                )
        );
    }

    /**
     * @param id id of the LanguagesZone
     * @return return LanguagesZone`s body
     */
    public LanguagesZone getLanguageZoneById(ObjectId id) {
        return languagesZoneRepository.findById(id).orElseThrow(() -> new RuntimeException("No areas by id: " + id));
    }

    /**
     * @param year year of areas
     * @return a list of Areas for specific year and map
     */
    public List<LanguagesZone> getLanguagesZoneByYear(Integer year){
        return languagesZoneRepository.findAllByYear(year).orElseThrow(() -> new RuntimeException("No areas by year: "+year));
    }

    /**
     * @param languagesZone body of new LanguagesZone.
     * @return return into repository new LanguagesZone body to create new LanguagesZone
     */
    public LanguagesZone saveLanguageZone(LanguagesZone languagesZone){
        return languagesZoneRepository.save(languagesZone);
    }

    public LanguagesZone updateLanguageZone(ObjectId id, LanguagesZone newLanguagesZone) {
        return languagesZoneRepository.findById(id).map(area -> {
            area.setName(newLanguagesZone.getName());
            area.setDescription(newLanguagesZone.getDescription());
            area.setCoords(newLanguagesZone.getCoords());
            area.setIntensity(newLanguagesZone.getIntensity());
            area.setColor(newLanguagesZone.getColor());
            area.setYear(newLanguagesZone.getYear());
            return languagesZoneRepository.save(area);
        }).orElseGet(() -> {
            newLanguagesZone.setId(id);
            return languagesZoneRepository.save(newLanguagesZone);
        });
    }


}
