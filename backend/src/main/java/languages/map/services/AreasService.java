package languages.map.services;

import languages.map.models.Areas;
import languages.map.repositories.AreasRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AreasService {
    private final AreasRepository areasRepository;

    public AreasService(AreasRepository areasRepository) {
        this.areasRepository = areasRepository;
    }
    public List<Areas> getAreas(){
        return areasRepository.findAll();
    }
    public Areas getAreasById(ObjectId id) {
        return areasRepository.findById(id).orElseThrow(() -> new RuntimeException("No areas by id: " + id));
    }
    public Areas saveAreas(Areas areas){
        return areasRepository.save(areas);
    }
    public Areas getAreasByYear(int year){
        return areasRepository.findByYear(year).orElseThrow(() -> new RuntimeException("No areas by year: " + year));
    }
}
