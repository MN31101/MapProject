package languages.map.models;

import lombok.Data;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "map")
public class Map {
    @Id
    ObjectId id;
    Binary preview;
    String title;
    String description;
}
