package languages.map.services;

import languages.map.models.Area;
import languages.map.repositories.AreaRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return areaRepository.findAllByMapId(map_id).orElseThrow(()->new RuntimeException("No areas by map_id: " + map_id));
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
        return areaRepository.findAllByMapIdAndYear(map_id, year).orElseThrow(() -> new RuntimeException("No areas by year: "+year));
    }

    /**
     * @param area body of new Area.
     * @return return into repository new Area body to create new Area
     */
    public Area saveArea(Area area){
        return areaRepository.save(area);
    }

    public Area updateArea(ObjectId id, Area newArea) {
        return areaRepository.findById(id).map(area -> {
            area.setName(newArea.getName());
            area.setDescription(newArea.getDescription());
            area.setCoords(newArea.getCoords());
            area.setIntensity(newArea.getIntensity());
            area.setColor(newArea.getColor());
            area.setYear(newArea.getYear());
            return areaRepository.save(area);
        }).orElseGet(() -> {
            newArea.setId(id);
            return areaRepository.save(newArea);
        });
    }


}
