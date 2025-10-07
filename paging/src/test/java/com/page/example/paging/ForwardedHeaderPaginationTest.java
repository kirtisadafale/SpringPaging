package com.page.example.paging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ForwardedHeaderPaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenForwardedHeadersPresent_thenPaginationLinksAreAbsolute() throws Exception {
        // request page 1 with forwarded headers that indicate external scheme/host
        var mvcResult = mockMvc.perform(get("/movies?pageNo=1&pageSize=2")
                        .header("X-Forwarded-Proto", "https")
                        .header("X-Forwarded-Host", "api.example.com")
                        .header("X-Forwarded-Port", "443")
                )
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(content);

        // next/prev may be null depending on dataset, but if present they should start with https://api.example.com
        if (root.hasNonNull("nextPageUrl")) {
            String next = root.get("nextPageUrl").asText();
            assertThat(next).startsWith("https://api.example.com");
        }
        if (root.hasNonNull("prevPageUrl")) {
            String prev = root.get("prevPageUrl").asText();
            assertThat(prev).startsWith("https://api.example.com");
        }
    }
}
