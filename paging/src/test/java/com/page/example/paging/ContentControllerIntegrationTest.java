package com.page.example.paging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ContentControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @SuppressWarnings("unchecked")
        public KafkaTemplate<String, String> kafkaTemplate() {
            // mock KafkaTemplate so tests don't require a running broker
            return (KafkaTemplate<String, String>) Mockito.mock(KafkaTemplate.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postMovie_then_checkLink_and_simulateProcessing() throws Exception {
        // prepare movie
        Movie m = new Movie("IT-Movie-1", "Drama");

        // POST -> should return 202 and a check link
        String payload = objectMapper.writeValueAsString(m);

        var postResult = mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.check").exists())
                .andReturn();

        String check = objectMapper.readTree(postResult.getResponse().getContentAsString()).get("check").asText();
        assertThat(check).isNotEmpty();

    // initial check should be pending / 404 (not processed yet) using path variable
    mockMvc.perform(get("/movies/{name}", m.getName()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("pending"));

        // simulate background consumer processing by saving the movie
        movieRepo.save(m);

    // now the movie should be retrievable by path variable
    mockMvc.perform(get("/movies/{name}", m.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(m.getName()))
                .andExpect(jsonPath("$.genre").value(m.getGenre()));
    }
}
