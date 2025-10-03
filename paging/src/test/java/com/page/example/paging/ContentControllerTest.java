package com.page.example.paging;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContentController.class)
@Import(ContentControllerTest.TestConfig.class)
class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MovieRepo movieRepo() {
            MovieRepo repo = Mockito.mock(MovieRepo.class);
            Mockito.when(repo.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Movie("Test", "genre"))));
            return repo;
        }
    }

    @Test
    void getMovies_withParams_returnsOk() throws Exception {
        mockMvc.perform(get("/movies?pageNo=1&pageSize=2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void getMovies_withoutParams_returnsOk() throws Exception {
        mockMvc.perform(get("/movies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
}
