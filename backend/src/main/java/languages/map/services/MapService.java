package languages.map.services;

import languages.map.models.Map;
import languages.map.repositories.MapRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapService {
    private MapService(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }
    private final MapRepository mapRepository;
    public List<Map> getMaps(){
        return mapRepository.findAll();
    }
    public Map getMapById(ObjectId id) {
        return mapRepository.findById(id).orElseThrow(() -> new RuntimeException("No map by id: " + id));
    }

    public Map saveMap(Map map){
        return mapRepository.save(map);
    }
}
