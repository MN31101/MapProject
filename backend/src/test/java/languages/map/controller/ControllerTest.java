package languages.map.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import languages.map.controllers.ChunkController;
import languages.map.controllers.LanguagesZoneController;
import languages.map.dto.BoundingBoxRequest;
import languages.map.models.Chunk;
import languages.map.models.LanguagesZone;
import languages.map.services.ChunkService;
import languages.map.services.LanguagesZoneService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class ControllersTest {

    private MockMvc mockMvc;

    @Mock
    private ChunkService chunkService;

    @Mock
    private LanguagesZoneService languagesZoneService;

    @InjectMocks
    private ChunkController chunkController;

    @InjectMocks
    private LanguagesZoneController languagesZoneController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Chunk testChunk;
    private LanguagesZone testZone;
    private BoundingBoxRequest testBoundingBox;

    @BeforeEach
    void setUp() {
        openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(chunkController, languagesZoneController)
                .build();

        // Підготовка тестового чанка
        testChunk = new Chunk();
        testChunk.setId(new ObjectId());
        testChunk.setZoom_level(5);
        testChunk.setCenter(new GeoJsonPoint(10.0, 20.0));
        testChunk.setLanguages_id(new ObjectId[]{new ObjectId()});

        // Підготовка тестової мовної зони
        testZone = new LanguagesZone();
        testZone.setId(new ObjectId());
        testZone.setName("Test Zone");
        testZone.setDescription("Test Description");
        testZone.setYear(2024);
        testZone.setIntensity(0.8);
        testZone.setColor(new Integer[]{255, 0, 0});
        testZone.setCoords(List.of(new GeoJsonPolygon(List.of(
                new org.springframework.data.geo.Point(0, 0),
                new org.springframework.data.geo.Point(0, 1),
                new org.springframework.data.geo.Point(1, 1),
                new org.springframework.data.geo.Point(1, 0),
                new org.springframework.data.geo.Point(0, 0)
        ))));

        // Підготовка тестового BoundingBox
        testBoundingBox = new BoundingBoxRequest();
        try {
            var field1 = testBoundingBox.getClass().getDeclaredField("leftTopPointLatLon");
            var field2 = testBoundingBox.getClass().getDeclaredField("rightBottomPointLatLon");
            field1.setAccessible(true);
            field2.setAccessible(true);
            field1.set(testBoundingBox, new double[]{0.0, 1.0});
            field2.set(testBoundingBox, new double[]{1.0, 0.0});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Тести для ChunkController
    @Test
    void getAllChunks_ShouldReturnChunksList() throws Exception {
        when(chunkService.getChunks()).thenReturn(List.of(testChunk));

        mockMvc.perform(get("/api/chunks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(testChunk.getId().toString()));
    }

    @Test
    void getChunkById_ShouldReturnChunk() throws Exception {
        when(chunkService.getChunk(any(ObjectId.class))).thenReturn(testChunk);

        mockMvc.perform(get("/api/chunks/" + testChunk.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testChunk.getId().toString()));
    }

    @Test
    void createChunk_ShouldReturnCreatedChunk() throws Exception {
        when(chunkService.createChunk(any(Chunk.class))).thenReturn(testChunk);

        mockMvc.perform(post("/api/chunk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testChunk)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testChunk.getId().toString()));
    }

    // Тести для LanguagesZoneController
    @Test
    void getLanguagesZonesByYear_ShouldReturnZonesList() throws Exception {
        when(languagesZoneService.getLanguagesZoneByYear(2024)).thenReturn(List.of(testZone));

        mockMvc.perform(get("/api/all/2024"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(testZone.getId().toString()));
    }

    @Test
    void getLanguagesZonesRelatedToChunk_ShouldReturnZonesList() throws Exception {
        when(languagesZoneService.getLanguagesZone(any(BoundingBoxRequest.class), any(Integer.class)))
                .thenReturn(List.of(testZone));

        mockMvc.perform(post("/api/areas/2024")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBoundingBox)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testZone.getId().toString()));
    }

    @Test
    void getLanguagesZonesRelatedToChunk_WithInvalidBoundingBox_ShouldReturnBadRequest() throws Exception {
        BoundingBoxRequest invalidRequest = new BoundingBoxRequest(); // Порожній запит

        mockMvc.perform(post("/api/areas/2024")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLanguagesZoneById_ShouldReturnZone() throws Exception {
        when(languagesZoneService.getLanguageZoneById(any(ObjectId.class))).thenReturn(testZone);

        mockMvc.perform(get("/api/area/" + testZone.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testZone.getId().toString()));
    }

    @Test
    void createLanguageZone_ShouldReturnCreatedZone() throws Exception {
        when(languagesZoneService.saveLanguageZone(any(LanguagesZone.class))).thenReturn(testZone);

        mockMvc.perform(post("/api/area")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testZone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testZone.getId().toString()));
    }

    @Test
    void updateLanguageZone_ShouldReturnUpdatedZone() throws Exception {
        when(languagesZoneService.updateLanguageZone(any(ObjectId.class), any(LanguagesZone.class)))
                .thenReturn(testZone);

        mockMvc.perform(put("/api/area/" + testZone.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testZone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testZone.getId().toString()));
    }
}