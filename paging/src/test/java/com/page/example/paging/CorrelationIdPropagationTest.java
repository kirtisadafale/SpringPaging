package com.page.example.paging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.messaging.Message;

@SpringBootTest
@AutoConfigureMockMvc
public class CorrelationIdPropagationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @SuppressWarnings("unchecked")
        public KafkaTemplate<String, String> kafkaTemplate() {
            return (KafkaTemplate<String, String>) Mockito.mock(KafkaTemplate.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void postMovie_includesCorrelationIdHeaderOnKafkaMessage() throws Exception {
        Movie m = new Movie("CID-Movie", "Drama");
        String payload = objectMapper.writeValueAsString(m);

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                )
                .andExpect(status().isAccepted());

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        Mockito.verify(kafkaTemplate, Mockito.times(1)).send(captor.capture());
        Message<?> sent = captor.getValue();
        assertThat(sent.getHeaders()).containsKey("X-Correlation-ID");
        Object cid = sent.getHeaders().get("X-Correlation-ID");
        assertThat(cid).isNotNull();
        assertThat(cid.toString()).isNotEmpty();
    }
}
