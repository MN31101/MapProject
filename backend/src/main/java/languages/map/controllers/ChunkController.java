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

    @GetMapping("/chunks")
    public ResponseEntity<List<Chunk>> getChunks(){
        return ResponseEntity.ok().body(chunkService.getChunks());
    }
    @GetMapping("/chunks/{chunk_id}")
    public ResponseEntity<Chunk> getChunk(@PathVariable ObjectId chunk_id){
        return ResponseEntity.ok().body(chunkService.getChunk(chunk_id));
    }

}
