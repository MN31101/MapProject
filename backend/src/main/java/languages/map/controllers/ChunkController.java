package languages.map.controllers;

import languages.map.models.Chunk;
import languages.map.services.ChunkService;
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
public class ChunkController {
    private static final Logger logger = LoggerFactory.getLogger(ChunkController.class);
    private final ChunkService chunkService;

    public ChunkController(ChunkService chunkService) {
        this.chunkService = chunkService;
    }
    /**
     * @return list of chunks for map
     */
    @GetMapping("/chunks")
    public ResponseEntity<List<Chunk>> getChunks(){
        logger.info("Fetching all chunks");
        List<Chunk> chunks = chunkService.getChunks();
        logger.debug("Retrieved {} chunks", chunks.size());
        return ResponseEntity.ok().body(chunks);
    }
    /**
     * @param chunk_id id of chunk
     * @return chunk by id
     */
    @GetMapping("/chunks/{chunk_id}")
    public ResponseEntity<Chunk> getChunk(@PathVariable ObjectId chunk_id){
        logger.info("Fetching chunk with id: {}", chunk_id);
        try {
            Chunk chunk = chunkService.getChunk(chunk_id);
            logger.debug("Retrieved chunk: {}", chunk);
            return ResponseEntity.ok().body(chunk);
        } catch (RuntimeException e) {
            logger.error("Failed to fetch chunk with id: {}", chunk_id, e);
            throw e;
        }
    }


    /**
     * @param chunk Chunk`s body which wanted to create
     * @return into repository new Chunk
     */
    @PostMapping("/chunk")
    public ResponseEntity<Chunk> createChunk(@RequestBody Chunk chunk){
        logger.info("Creating new chunk");
        logger.debug("Chunk data: {}", chunk);
        try {
            Chunk created = chunkService.createChunk(chunk);
            logger.info("Successfully created chunk with id: {}", created.getId());
            return ResponseEntity.ok().body(created);
        } catch (Exception e) {
            logger.error("Failed to create chunk", e);
            throw e;
        }
    }
}
