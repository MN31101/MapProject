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
    private ObjectId id;
    @JsonSerialize(using = GeoJsonPolygonListSerializer.class)
    private List<GeoJsonPolygon> coords;
    private String description;
    private String name;
    private Double intensity;
    private Integer[] color = new Integer[3];
    private Integer year;
}
