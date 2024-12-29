package languages.map.models;

import com.mongodb.client.model.geojson.Point;
import lombok.Data;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJson;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "chunk")
public class Chunk {
    @Id
    ObjectId id0;
    Binary size;
    GeoJsonPoint point;
    Integer zoom_level;
}
