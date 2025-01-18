package languages.map.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import languages.map.configurations.SecurityConfig;
import languages.map.controllers.ChunkController;
import languages.map.controllers.LanguagesZoneController;
import languages.map.dto.BoundingBoxRequest;
import languages.map.models.Chunk;
import languages.map.models.LanguagesZone;
import languages.map.serializers.ObjectIdSerializer;
import languages.map.services.ChunkService;
import languages.map.services.LanguagesZoneService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.core.geo.GeoJsonModule;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ChunkController.class, LanguagesZoneController.class})
@ContextConfiguration(classes = {ControllersTest.TestConfig.class, SecurityConfig.class})
public class ControllersTest {

    @Configuration
    @ComponentScan(basePackages = "languages.map.controllers")
    static class TestConfig {
        @Bean
        public ChunkService chunkService() {
            return mock(ChunkService.class);
        }

        @Bean
        public LanguagesZoneService languagesZoneService() {
            return mock(LanguagesZoneService.class);
        }

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.registerModule(new GeoJsonModule());
            mapper.registerModule(new SimpleModule().addSerializer(ObjectId.class, new ObjectIdSerializer()));
            return mapper;
        }

    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChunkService chunkService;

    @Autowired
    private LanguagesZoneService languagesZoneService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private Chunk testChunk;
    private LanguagesZone testZone;
    private BoundingBoxRequest testBoundingBox;

    @BeforeEach
    void setUp() {

        testChunk = new Chunk();
        testChunk.setId(new ObjectId());
        testChunk.setZoom_level(5);
        testChunk.setCenter(new GeoJsonPoint(10.0, 20.0));
        testChunk.setLanguages_id(new ObjectId[]{new ObjectId()});

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
        context.getBeansOfType(HandlerMapping.class)
                .values()
                .forEach(mapping -> {
                    if (mapping instanceof RequestMappingHandlerMapping) {
                        ((RequestMappingHandlerMapping) mapping)
                                .getHandlerMethods()
                                .forEach((key, value) ->
                                        System.out.println("Mapped: " + key + " to " + value));
                    }
                });
    }

    @Test
    void getAllChunks_ShouldReturnChunksList() throws Exception {
        when(chunkService.getChunks()).thenReturn(List.of(testChunk));

        mockMvc.perform(get("/api/chunks")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(testChunk.getId().toString()));
    }

    @Test
    void getChunkById_ShouldReturnChunk() throws Exception {
        when(chunkService.getChunk(any(ObjectId.class))).thenReturn(testChunk);

        mockMvc.perform(get("/api/chunks/" + testChunk.getId()))
                .andDo(print())
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
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testChunk.getId().toString()));
    }



    @Test
    void getLanguagesZonesByYear_ShouldReturnZonesList() throws Exception {
        when(languagesZoneService.getLanguagesZoneByYear(2024)).thenReturn(List.of(testZone));

        mockMvc.perform(get("/api/all/2024"))
                .andDo(print())
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
                .andDo(print())
                .andExpect(jsonPath("$[0].id").value(testZone.getId().toString()));
    }

    @Test
    void getLanguagesZoneById_ShouldReturnZone() throws Exception {
        when(languagesZoneService.getLanguageZoneById(any(ObjectId.class))).thenReturn(testZone);

        mockMvc.perform(get("/api/area/" + testZone.getId()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(testZone.getId().toString()));
    }

    @Test
    void createLanguageZone_ShouldReturnCreatedZone() throws Exception {
        when(languagesZoneService.saveLanguageZone(any(LanguagesZone.class))).thenReturn(testZone);

        mockMvc.perform(post("/api/area")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testZone)))
                .andExpect(status().isOk())
                .andDo(print())
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
                .andDo(print())
                .andExpect(jsonPath("$.id").value(testZone.getId().toString()));
    }
 /*   @Test
    void deleteLanguageZone_ShouldReturnOk() throws Exception {
        // Mock the service method
        doNothing().when(languagesZoneService).deleteLanguageZone(any(LanguagesZone.class));

        mockMvc.perform(delete("/area")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}")) // Assuming the body contains the ID to delete
                .andExpect(status().isOk());

        // Verify that the service delete method was called
        verify(languagesZoneService, times(1)).deleteLanguageZone(any(LanguagesZone.class));
    }

    @Test
    void deleteLanguageZone_ShouldReturnInternalServerError_OnFailure() throws Exception {
        // Mock the service method to throw an exception
        doThrow(new RuntimeException("Delete failed")).when(languagesZoneService).deleteLanguageZone(any(LanguagesZone.class));

        mockMvc.perform(delete("/api/area")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());

        // Verify that the service delete method was called
        verify(languagesZoneService, times(1)).deleteLanguageZone(any(LanguagesZone.class));
    }

  */
}
