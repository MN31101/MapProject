package languages.map.services;

import languages.map.models.Area;
import languages.map.repositories.AreaRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AreaService {
    private final AreaRepository areaRepository;

    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }
    public List<Area> getAreas(ObjectId map_id){
        return areaRepository.findAllByMap_id(map_id).orElseThrow(()->new RuntimeException("No areas by map_id: " + map_id));
    }
    public Area getAreaById(ObjectId id) {
        return areaRepository.findById(id).orElseThrow(() -> new RuntimeException("No areas by id: " + id));
    }
    public List<Area> getAreasByYear(ObjectId map_id, Integer year){
        return areaRepository.findAllByMap_idAAndYear(map_id, year).orElseThrow(() -> new RuntimeException("No areas by year: "+year));
    }

    public Area saveArea(Area area, ObjectId map_id){
        return areaRepository.save(area, map_id);
    }

    /*

    !!!!!!!!!!!!!



     */
    public Area updateArea(ObjectId id, Area newArea) {
        return areaRepository
                .updateAreaById(id, newArea)
                .orElseThrow(
                        ()->new RuntimeException("cannot find area by area_id: "+ id)
                );
    }

}
