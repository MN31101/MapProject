package languages.map.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.io.IOException;

public class GeoJsonPolygonSerializer extends JsonSerializer<GeoJsonPolygon> {
    @Override
    public void serialize(GeoJsonPolygon value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", "Polygon");
        gen.writeArrayFieldStart("coordinates");

        // Write the exterior ring
        gen.writeStartArray();
        for (org.springframework.data.geo.Point point : value.getPoints()) {
            gen.writeStartArray();
            gen.writeNumber(point.getX());
            gen.writeNumber(point.getY());
            gen.writeEndArray();
        }
        gen.writeEndArray();

        gen.writeEndArray();
        gen.writeEndObject();
    }
}