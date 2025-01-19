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
    private final ChunkService chunkService;

    public ChunkController(ChunkService chunkService) {
        this.chunkService = chunkService;
    }

    /**
     * @return list of chunks for map
     */
    @GetMapping("/chunks")
    public ResponseEntity<List<Chunk>> getChunks() {
        try {
            List<Chunk> chunks = chunkService.getChunks();
            return ResponseEntity.ok().body(chunks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Handle error gracefully
        }
    }

    /**
     * @param chunk_id id of chunk
     * @return chunk by id
     */
    @GetMapping("/chunks/{chunk_id}")
    public ResponseEntity<Chunk> getChunk(@PathVariable ObjectId chunk_id) {
        try {
            Chunk chunk = chunkService.getChunk(chunk_id);
            return ResponseEntity.ok().body(chunk);
        } catch (RuntimeException e) {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Returning not found for error handling
        }
    }

    /**
     * @param chunk Chunk's body which is wanted to create
     * @return Chunk saved into repository
     */
    @PostMapping("/chunk")
    public ResponseEntity<Chunk> createChunk(@RequestBody Chunk chunk) {
        try {
            Chunk created = chunkService.createChunk(chunk);
            return ResponseEntity.ok().body(created);
        } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);  // Handle error gracefully
        }
    }
}

