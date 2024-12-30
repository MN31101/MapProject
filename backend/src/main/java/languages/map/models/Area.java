package languages.map.models;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "area")
public class Area {
    @Id
    ObjectId id;
    ObjectId mapId;
    ObjectId[] chunksId;
    GeoJsonPolygon[] coords;
    String description;
    String name;
    Double intensity;
    Integer[] color = new Integer[3];
    Integer year;
}
