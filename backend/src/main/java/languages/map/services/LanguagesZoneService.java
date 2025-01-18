package languages.map.services;

import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LanguagesZoneService {
    private static final Logger logger = LoggerFactory.getLogger(LanguagesZoneService.class);
    private final LanguagesZoneRepository languagesZoneRepository;

    public LanguagesZoneService(LanguagesZoneRepository languagesZoneRepository) {
        this.languagesZoneRepository = languagesZoneRepository;
    }

    public List<LanguagesZone> getLanguagesZone(@NotNull BoundingBoxRequest boundingBoxRequest, Integer year) {
        logger.info("Fetching languages zones for year: {}", year);
        final var boundingBox = getBoundingBox(boundingBoxRequest);
        List<LanguagesZone> zones = getLanguagesZoneByYear(year);

        logger.debug("Total zones found: {}", zones.size());

        return zones.stream()
                .map(zone -> processZone(zone, boundingBox))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private LanguagesZone processZone(LanguagesZone zone, Geometry boundingBox) {
        try {
            logger.info("Processing zone with id: {}", zone.getId());
            List<GeoJsonPolygon> clippedPolygons = clipPolygons(zone, boundingBox);
            zone.setCoords(clippedPolygons);
            logger.debug("Clipped polygons for zone id {}: {}", zone.getId(), clippedPolygons.size());
            return zone;
        } catch (Exception e) {
            logger.error("Error clipping geometry for zone: {}", zone.getId(), e);
            throw new RuntimeException("Error clipping geometry for zone: " + zone.getId(), e);
        }
    }

    private List<GeoJsonPolygon> clipPolygons(LanguagesZone zone, Geometry boundingBox) {
        List<GeoJsonPolygon> allPolygons = new ArrayList<>();
        for (GeoJsonPolygon geoJsonPolygon : zone.getCoords()) {
            allPolygons.addAll(clipPolygon(geoJsonPolygon, boundingBox));  // Add the list of clipped polygons
        }
        logger.debug("Clipped {} polygons for zone {}", allPolygons.size(), zone.getId());
        return allPolygons;
    }


    private List<GeoJsonPolygon> clipPolygon(GeoJsonPolygon geoJsonPolygon, Geometry boundingBox) {
        try {
            String wkt = createWKT(geoJsonPolygon);
            Geometry zoneGeometry = new WKTReader().read(wkt);

            if (!zoneGeometry.isValid()) {
                zoneGeometry = zoneGeometry.buffer(0);
            }

            Geometry clippedGeometry = OverlayOp.overlayOp(zoneGeometry, boundingBox, OverlayOp.INTERSECTION);

            if (!clippedGeometry.isEmpty()) {
                if (clippedGeometry instanceof MultiPolygon multiPolygon) {
                    List<GeoJsonPolygon> polygons = new ArrayList<>();
                    for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                        Geometry poly = multiPolygon.getGeometryN(i);
                        List<Point> newPoints = createCutPoints(zoneGeometry, poly);
                        polygons.add(new GeoJsonPolygon(newPoints));
                    }
                    logger.debug("Created {} clipped polygons from multi-polygon", polygons.size());
                    return polygons;
                } else {
                    List<Point> newPoints = createCutPoints(zoneGeometry, clippedGeometry);
                    logger.debug("Created 1 clipped polygon");
                    return List.of(new GeoJsonPolygon(newPoints));
                }
            }
            logger.warn("Clipped geometry is empty for zone geometry");
            return List.of();
        } catch (Exception e) {
            logger.error("Error processing geometry for polygon", e);
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
            newPoints.add(firstPoint); // Ensure the polygon is closed
        }

        logger.debug("Created {} cut points for clipped geometry", newPoints.size());
        return newPoints;
    }


    private String createWKT(GeoJsonPolygon geoJsonPolygon) {
        String wkt = String.format("POLYGON((%s))",
                geoJsonPolygon.getPoints().stream()
                        .map(p -> p.getX() + " " + p.getY())
                        .collect(Collectors.joining(", ")));
        logger.debug("Generated WKT: {}", wkt);
        return wkt;
    }


    private static Geometry getBoundingBox(@NotNull BoundingBoxRequest boundingBoxRequest) {
        double x1 = boundingBoxRequest.getLeftTopPointLatLon()[0];
        double y1 = boundingBoxRequest.getLeftTopPointLatLon()[1];
        double x2 = boundingBoxRequest.getRightBottomPointLatLon()[0];
        double y2 = boundingBoxRequest.getRightBottomPointLatLon()[1];

        GeometryFactory geometryFactory = new GeometryFactory();
        Geometry boundingBox = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(x1, y1),
                new Coordinate(x1, y2),
                new Coordinate(x2, y2),
                new Coordinate(x2, y1),
                new Coordinate(x1, y1)
        });

        logger.debug("Created bounding box with coordinates: {}, {}", x1, y1);
        return boundingBox;
    }

    /**
     * @param id id of the LanguagesZone
     * @return return LanguagesZone`s body
     */
    public LanguagesZone getLanguageZoneById(ObjectId id) {
        logger.info("Fetching languages zone with id: {}", id);
        return languagesZoneRepository.findById(id).orElseThrow(() -> {
            logger.error("No languages zone found with id: {}", id);
            return new RuntimeException("No areas by id: " + id);
        });
    }

    /**
     * @param year year of areas
     * @return a list of Areas for specific year and map
     */
    public List<LanguagesZone> getLanguagesZoneByYear(Integer year) {
        logger.info("Fetching languages zones for year: {}", year);
        List<LanguagesZone> zones = languagesZoneRepository.findAllByYear(year)
                .orElseThrow(() -> {
                    logger.error("No languages zones found for year: {}", year);
                    return new RuntimeException("No areas by year: " + year);
                });
        logger.debug("Total zones found for year {}: {}", year, zones.size());
        return zones;
    }

    /**
     * @param languagesZone body of new LanguagesZone.
     * @return return into repository new LanguagesZone body to create new LanguagesZone
     */
    public LanguagesZone saveLanguageZone(LanguagesZone languagesZone) {
        if (languagesZone.getId() == null) {
            languagesZone.setId(new ObjectId()); // Ensure an ID is set before saving
        }
        logger.info("Saving new languages zone: {}", languagesZone);
        LanguagesZone savedZone = languagesZoneRepository.save(languagesZone);
        logger.info("Successfully saved languages zone with id: {}", savedZone.getId());
        return savedZone;
    }
    public LanguagesZone updateLanguageZone(ObjectId id, LanguagesZone newLanguagesZone) {
        logger.info("Updating languages zone with id: {}", id);
        return languagesZoneRepository.findById(id).map(area -> {
            logger.debug("Found existing zone, updating it");
            area.setName(newLanguagesZone.getName());
            area.setDescription(newLanguagesZone.getDescription());
            area.setCoords(newLanguagesZone.getCoords());
            area.setIntensity(newLanguagesZone.getIntensity());
            area.setColor(newLanguagesZone.getColor());
            area.setYear(newLanguagesZone.getYear());
            LanguagesZone updatedZone = languagesZoneRepository.save(area);
            logger.info("Successfully updated languages zone with id: {}", updatedZone.getId());
            return updatedZone;
        }).orElseGet(() -> {
            logger.warn("No existing zone found, creating new one");
            newLanguagesZone.setId(id);
            LanguagesZone createdZone = languagesZoneRepository.save(newLanguagesZone);
            logger.info("Successfully created languages zone with id: {}", createdZone.getId());
            return createdZone;
        });
    }

}
