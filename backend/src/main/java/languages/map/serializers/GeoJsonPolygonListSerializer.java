package languages.map.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.io.IOException;
import java.util.List;

public class GeoJsonPolygonListSerializer extends JsonSerializer<List<GeoJsonPolygon>> {
    @Override
    public void serialize(@NotNull List<GeoJsonPolygon> value, @NotNull JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartArray();
        for (GeoJsonPolygon polygon : value) {
            gen.writeStartObject();
            gen.writeStringField("type", "Polygon");
            gen.writeArrayFieldStart("coordinates");

            gen.writeStartArray();
            for (org.springframework.data.geo.Point point : polygon.getPoints()) {
                gen.writeStartArray();
                gen.writeNumber(point.getX());
                gen.writeNumber(point.getY());
                gen.writeEndArray();
            }
            gen.writeEndArray();

            gen.writeEndArray();
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }
}