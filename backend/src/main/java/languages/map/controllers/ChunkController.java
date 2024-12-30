package languages.map.controllers;

import languages.map.models.Chunk;
import languages.map.services.ChunkService;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
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
    public ResponseEntity<List<Chunk>> getChunks(){
        return ResponseEntity.ok().body(chunkService.getChunks());
    }

    /**
     * @param chunk_id id of chunk
     * @return chunk by id
     */
    @GetMapping("/chunks/{chunk_id}")
    public ResponseEntity<Chunk> getChunk(@PathVariable ObjectId chunk_id){
        return ResponseEntity.ok().body(chunkService.getChunk(chunk_id));
    }

    /**
     * @param chunk Chunk`s body which wanted to create
     * @return into repository new Chunk
     */
    @PostMapping("chunk")
    public ResponseEntity<Chunk> createChunk(@RequestBody Chunk chunk){
        return ResponseEntity.ok().body(chunkService.createChunk(chunk));
    }

}
