package languages.map.services;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.springframework.data.geo.Point;
import languages.map.dto.BoundingBoxRequest;
import languages.map.models.LanguagesZone;
import languages.map.repositories.LanguagesZoneRepository;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LanguagesZoneService {
    private final LanguagesZoneRepository languagesZoneRepository;

    public LanguagesZoneService(LanguagesZoneRepository languagesZoneRepository) {
        this.languagesZoneRepository = languagesZoneRepository;
    }


    public List<LanguagesZone> getLanguagesZone(@NotNull BoundingBoxRequest boundingBoxRequest, Integer year) {
        final var boundingBox = getBoundingBox(boundingBoxRequest);
        List<LanguagesZone> zones = getLanguagesZoneByYear(year);

        return zones.stream()
                .map(zone -> processZone(zone, boundingBox))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private LanguagesZone processZone(LanguagesZone zone, Geometry boundingBox) {
        try {
            List<GeoJsonPolygon> clippedPolygons = clipPolygons(zone, boundingBox);
            zone.setCoords(clippedPolygons);
            return zone;
        } catch (Exception e) {
            throw new RuntimeException("Error clipping geometry for zone: " + zone.getId(), e);
        }
    }

    private List<GeoJsonPolygon> clipPolygons(LanguagesZone zone, Geometry boundingBox) {
        return zone.getCoords().stream()
                .map(geoJsonPolygon -> clipPolygon(geoJsonPolygon, boundingBox))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private GeoJsonPolygon clipPolygon(GeoJsonPolygon geoJsonPolygon, Geometry boundingBox) {
        try {
            String wkt = createWKT(geoJsonPolygon);
            Geometry zoneGeometry = new WKTReader().read(wkt);


            if (!zoneGeometry.isValid()) {
                zoneGeometry = zoneGeometry.buffer(0);
            }

            Geometry clippedGeometry = OverlayOp.overlayOp(zoneGeometry, boundingBox, OverlayOp.INTERSECTION);

            if (!clippedGeometry.isEmpty()) {
                List<Point> newPoints = createCutPoints(zoneGeometry, clippedGeometry);
                return new GeoJsonPolygon(newPoints);
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error processing geometry", e);
        }
    }

    private List<Point> createCutPoints(Geometry zoneGeometry, Geometry clippedGeometry) {
        List<Point> newPoints = new ArrayList<>();
        Coordinate[] clippedCoords = clippedGeometry.getCoordinates();

        for (Coordinate coord : clippedCoords) {
            newPoints.add(new Point(coord.x, coord.y));
        }

        if (!newPoints.isEmpty()) {
            Point firstPoint = newPoints.get(0);
            newPoints.add(firstPoint);
        }

        return newPoints;
    }


    private String createWKT(GeoJsonPolygon geoJsonPolygon) {
        return String.format("POLYGON((%s))",
                geoJsonPolygon.getPoints().stream()
                        .map(p -> p.getX() + " " + p.getY())
                        .collect(Collectors.joining(", ")));
    }


    private boolean containsCoordinate(Coordinate[] coords, Coordinate coord) {
        for (Coordinate c : coords) {
            if (c.equals(coord)) {
                return true;
            }
        }
        return false;
    }



    private static Geometry getBoundingBox(@NotNull BoundingBoxRequest boundingBoxRequest) {
        double x1 = boundingBoxRequest.getLeftTopPointLatLon()[0];
        double y1 = boundingBoxRequest.getLeftTopPointLatLon()[1];
        double x2 = boundingBoxRequest.getRightBottomPointLatLon()[0];
        double y2 = boundingBoxRequest.getRightBottomPointLatLon()[1];

        GeometryFactory geometryFactory = new GeometryFactory();

        return geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(x1, y1),
                new Coordinate(x1, y2),
                new Coordinate(x2, y2),
                new Coordinate(x2, y1),
                new Coordinate(x1, y1)
        });
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
