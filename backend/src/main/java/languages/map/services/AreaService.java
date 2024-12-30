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

    /**
     * @param map_id id a map on which one related all areas
     * @return a List of Areas
     */
    public List<Area> getAreas(ObjectId map_id){
        return areaRepository.findAllByMap_id(map_id).orElseThrow(()->new RuntimeException("No areas by map_id: " + map_id));
    }

    /**
     * @param id id of the Area
     * @return return Area`s body
     */
    public Area getAreaById(ObjectId id) {
        return areaRepository.findById(id).orElseThrow(() -> new RuntimeException("No areas by id: " + id));
    }

    /**
     * @param map_id id of map with what areas related
     * @param year year of areas
     * @return a list of Areas for specific year and map
     */
    public List<Area> getAreasByYear(ObjectId map_id, Integer year){
        return areaRepository.findAllByMap_idAAndYear(map_id, year).orElseThrow(() -> new RuntimeException("No areas by year: "+year));
    }

    /**
     * @param area body of new Area.
     * @return return into repository new Area body to create new Area
     */
    public Area saveArea(Area area){
        return areaRepository.save(area);
    }


    /**
     * @param id - id current Area
     * @param newArea - body of new Area
     * @return return into repository new Area by id
     */
    public Area updateArea(ObjectId id, Area newArea) {
        return areaRepository
                .updateAreaById(id, newArea)
                .orElseThrow(
                        ()->new RuntimeException("cannot find area by area_id: "+ id)
                );
    }

}
