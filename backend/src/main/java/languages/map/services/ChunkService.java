package languages.map.services;

import languages.map.models.Chunk;
import languages.map.repositories.ChunkRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChunkService {
    private final ChunkRepository chunkRepository;

    public ChunkService(ChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    public List<Chunk> getChunks(){
        return chunkRepository.findAll();
    }
    public Chunk getChunk(ObjectId id){
        return chunkRepository
                .getChunkById(id)
                .orElseThrow(
                    ()->new RuntimeException("can't find chunk by id: "+id)
                );
    }
    public Chunk createChunk(Chunk chunk){
        return chunkRepository.save(chunk);
    }

}
