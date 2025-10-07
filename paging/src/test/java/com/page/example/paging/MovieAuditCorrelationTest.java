package com.page.example.paging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class MovieAuditCorrelationTest {

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private MovieAuditRepo movieAuditRepo;

    @Autowired
    private MovieService movieService;

    @Test
    void whenProcessingMovie_thenAuditContainsCorrelationId() {
        String cid = "test-cid-123";
        MDC.put("correlationId", cid);

        Movie m = new Movie("TraceMovie-1", "SciFi");
        Movie saved = movieService.processIncomingMovie(m);

        // find audit rows for this movie
        var audits = movieAuditRepo.findAll();
        boolean found = false;
        for (MovieAudit a : audits) {
            if (a.getMovieId().equals(saved.getId())) {
                found = true;
                assertThat(a.getCorrelationId()).isEqualTo(cid);
            }
        }
        assertThat(found).isTrue();
        MDC.remove("correlationId");
    }
}
