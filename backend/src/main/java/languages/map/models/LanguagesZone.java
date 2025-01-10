package languages.map.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import languages.map.serializers.GeoJsonPolygonListSerializer;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "languagesZone")
public class LanguagesZone {
    @Id
    ObjectId id;
    @JsonSerialize(using = GeoJsonPolygonListSerializer.class)
    List<GeoJsonPolygon> coords;
    String description;
    String name;
    Double intensity;
    Integer[] color = new Integer[3];
    Integer year;
}
