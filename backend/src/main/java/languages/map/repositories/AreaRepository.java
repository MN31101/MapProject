package languages.map.repositories;

import languages.map.models.Area;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaRepository extends MongoRepository<Area, ObjectId> {
    Optional<List<Area>> findAllByMapId(ObjectId map_id);
    Optional<List<Area>> findAllByMapIdAndYear(ObjectId map_id, Integer year);
}
