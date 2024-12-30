package languages.map.repositories;

import languages.map.models.Chunk;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChunkRepository extends MongoRepository<Chunk, ObjectId> {
    Optional<Chunk> getChunkById(ObjectId id);
}
