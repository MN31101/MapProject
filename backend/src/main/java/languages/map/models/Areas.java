package languages.map.models;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "areas")
public class Areas {
    @Id
    ObjectId id;
    String name;
    String description;
    int year;
    byte intensity;
    short[] color = new short[3];
    double[][] coords;
}
