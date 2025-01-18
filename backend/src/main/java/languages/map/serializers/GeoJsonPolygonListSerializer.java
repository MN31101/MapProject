package languages.map.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.geo.Point;

import java.io.IOException;
import java.util.List;

public class GeoJsonPolygonListSerializer extends JsonSerializer<List<GeoJsonPolygon>> {

    @Override
    public void serialize(List<GeoJsonPolygon> polygons, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartArray(); // Start array for the list of polygons

        for (GeoJsonPolygon polygon : polygons) {
            serializePolygon(polygon, gen);
        }

        gen.writeEndArray(); // End array for the list of polygons
    }

    private void serializePolygon(GeoJsonPolygon polygon, JsonGenerator gen) throws IOException {
        gen.writeStartObject(); // Start object for a single polygon
        gen.writeStringField("type", "Polygon");
        gen.writeArrayFieldStart("coordinates");

        gen.writeStartArray(); // Start array for polygon coordinates
        for (Point point : polygon.getPoints()) {
            serializePoint(point, gen);
        }
        gen.writeEndArray(); // End array for polygon coordinates

        gen.writeEndArray();
        gen.writeEndObject(); // End object for a single polygon
    }

    private void serializePoint(Point point, JsonGenerator gen) throws IOException {
        gen.writeStartArray(); // Start array for a single point
        gen.writeNumber(point.getX());
        gen.writeNumber(point.getY());
        gen.writeEndArray(); // End array for a single point
    }
}
