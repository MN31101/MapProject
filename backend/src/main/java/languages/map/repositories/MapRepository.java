package languages.map.repositories;

import languages.map.models.Map;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapRepository extends MongoRepository<Map, ObjectId> {
}
