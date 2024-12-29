package languages.map.models;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "area")
public class Areas {
    @Id
    ObjectId id;
    ObjectId map_id;
    GeoJsonPolygon[] coords;
    Double intensity;
    String description;
    String name;
    Integer[] color = new Integer[3];
    Integer year;
}
