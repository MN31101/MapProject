package languages.map.repositories;

import languages.map.models.Areas;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AreasRepository extends MongoRepository<Areas, ObjectId> {
    Optional<Areas> findByYear(int year);
}
