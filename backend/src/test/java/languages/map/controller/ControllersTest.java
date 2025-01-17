package languages.map.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import languages.map.controllers.ChunkController;
import languages.map.models.Chunk;
import languages.map.services.ChunkService;
import org.bson.types.ObjectId;;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChunkController.class)
public class ControllersTest {

    private static final Logger logger = LoggerFactory.getLogger(ControllersTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChunkService chunkService;

    @Configuration
    @EnableWebSecurity
    static class TestConfig {
        @Bean
        public ChunkService chunkService() {
            return mock(ChunkService.class);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf().disable().build(); // Disabling CSRF for the test
        }
    }

    @BeforeEach
    void setUp() {
        reset(chunkService);
    }

    @Test
    void testAccessDeniedWithInvalidCsrf() throws Exception {
        logger.info("Running testAccessDeniedWithInvalidCsrf...");

        mockMvc.perform(post("/api/chunk")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());

        logger.info("testAccessDeniedWithInvalidCsrf completed.");
    }

    @Test
    void testAccessDeniedWithoutCsrf() throws Exception {
        logger.info("Running testAccessDeniedWithoutCsrf...");

        mockMvc.perform(post("/api/chunk")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());

        logger.info("testAccessDeniedWithoutCsrf completed.");
    }

    @Test
    void testCreateChunkWithCsrf() throws Exception {
        logger.info("Running testCreateChunkWithCsrf...");

        Chunk chunk = new Chunk();
        chunk.setId(new ObjectId());  // Example chunk data
        chunk.setLanguages_id(new ObjectId[]{new ObjectId(), new ObjectId()});  // Setting multiple language IDs
        chunk.setCenter(new GeoJsonPoint(1, 1));  // Setting geo location
        chunk.setZoom_level(10);  // Setting zoom level

        when(chunkService.createChunk(any())).thenReturn(chunk);

        mockMvc.perform(post("/api/chunk")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(chunk)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.languages_id").isArray())
                .andExpect(jsonPath("$.languages_id.length()").value(2))  // Expecting 2 language IDs
                .andExpect(jsonPath("$.center.coordinates[0]").value(1))  // Checking geo location
                .andExpect(jsonPath("$.center.coordinates[1]").value(1))  // Checking geo location
                .andExpect(jsonPath("$.zoom_level").value(10));

        logger.info("testCreateChunkWithCsrf completed.");
    }

    @Test
    void testGetChunks() throws Exception {
        logger.info("Running testGetChunks...");

        when(chunkService.getChunks()).thenReturn(List.of(new Chunk()));

        mockMvc.perform(get("/api/chunks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].languages_id").isArray())
                .andExpect(jsonPath("$[0].languages_id.length()").value(2))  // Expecting 2 language IDs
                .andExpect(jsonPath("$[0].center.coordinates[0]").value(1))  // Checking geo location
                .andExpect(jsonPath("$[0].center.coordinates[1]").value(1))  // Checking geo location
                .andExpect(jsonPath("$[0].zoom_level").value(10));

        logger.info("testGetChunks completed.");
    }
}
