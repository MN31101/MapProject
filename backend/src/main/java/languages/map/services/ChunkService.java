package languages.map.services;

import languages.map.models.Chunk;
import languages.map.repositories.ChunkRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChunkService {
    private static final Logger logger = LoggerFactory.getLogger(ChunkService.class);
    private final ChunkRepository chunkRepository;

    public ChunkService(ChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    public List<Chunk> getChunks() {
        logger.info("Fetching all chunks");
        List<Chunk> chunks = chunkRepository.findAll();
        logger.debug("Total chunks found: {}", chunks.size());
        return chunks;
    }

    public Chunk getChunk(ObjectId id) {
        logger.info("Fetching chunk with id: {}", id);
        try {
            Chunk chunk = chunkRepository.getChunkById(id)
                    .orElseThrow(() -> new RuntimeException("can't find chunk by id: " + id));
            logger.debug("Retrieved chunk: {}", chunk);
            return chunk;
        } catch (RuntimeException e) {
            logger.error("Failed to fetch chunk with id: {}", id, e);
            throw e;
        }
    }

    public Chunk createChunk(Chunk chunk) {
        logger.info("Creating new chunk");
        logger.debug("Chunk data: {}", chunk);
        try {
            Chunk createdChunk = chunkRepository.save(chunk);
            logger.info("Successfully created chunk with id: {}", createdChunk.getId());
            return createdChunk;
        } catch (Exception e) {
            logger.error("Failed to create chunk", e);
            throw e;
        }
    }
}
