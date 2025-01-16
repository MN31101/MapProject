package languages.map.repositories;

import languages.map.models.LanguagesZone;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguagesZoneRepository extends MongoRepository<LanguagesZone, ObjectId> {
    Optional<List<LanguagesZone>> findAllByYear(Integer year);
    // Optional<List<LanguagesZone>> findAllByYearAndCoordsWithin(Integer year, GeoJsonPolygon boundingBox);

}
