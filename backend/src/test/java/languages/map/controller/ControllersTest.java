package languages.map.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import languages.map.controllers.ChunkController;
import languages.map.controllers.LanguagesZoneController;
import languages.map.models.Chunk;
import languages.map.services.ChunkService;
import languages.map.services.LanguagesZoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ChunkController.class, LanguagesZoneController.class})
public class ControllersTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChunkService chunkService;

    @Autowired
    private LanguagesZoneService languagesZoneService;

    @Configuration
    static class TestConfig {
        @Bean
        public ChunkService chunkService() {
            return mock(ChunkService.class);
        }

        @Bean
        public LanguagesZoneService languagesZoneService() {
            return mock(LanguagesZoneService.class);
        }
    }

    @BeforeEach
    void setUp() {
        reset(chunkService, languagesZoneService);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetChunks() throws Exception {
        when(chunkService.getChunks()).thenReturn(List.of(new Chunk()));

        mockMvc.perform(get("/8080/api/chunks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].name").value("chunkName"));

        verify(chunkService).getChunks();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateChunk() throws Exception {
        Chunk chunk = new Chunk();
        when(chunkService.createChunk(any())).thenReturn(chunk);

        mockMvc.perform(post("/8080/api/chunks")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(chunk)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("newChunk"));

        verify(chunkService).createChunk(any());
    }

    @Test
    void testAccessDenied() throws Exception {
        mockMvc.perform(post("/8080/api/chunks")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
