package com.page.example.paging;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import org.slf4j.MDC;

@Service
public class MovieService {

    private final Logger log = LoggerFactory.getLogger(MovieService.class);

    private final MovieRepo movieRepo;
    private final MovieAuditRepo movieAuditRepo;

    public MovieService(MovieRepo movieRepo, MovieAuditRepo movieAuditRepo) {
        this.movieRepo = movieRepo;
        this.movieAuditRepo = movieAuditRepo;
    }

    /**
     * Process an incoming movie event: if movie exists, write SKIPPED audit; otherwise save movie and CREATED audit.
     * This operation is transactional so both movie and audit persist together.
     */
    @Transactional
    public Movie processIncomingMovie(Movie incoming) {
        Optional<Movie> existing = movieRepo.findByName(incoming.getName());
        if (existing.isPresent()) {
            Movie exist = existing.get();
            String cid = MDC.get("correlationId");
            MovieAudit audit = new MovieAudit(exist.getId(), exist.getName(), exist.getGenre(), "SKIPPED", Instant.now(), cid);
            movieAuditRepo.save(audit);
            log.info("Processed incoming movie event - SKIPPED, name={}", exist.getName());
            return exist;
        }

        Movie saved = movieRepo.save(incoming);
        String cid = MDC.get("correlationId");
        MovieAudit audit = new MovieAudit(saved.getId(), saved.getName(), saved.getGenre(), "CREATED", Instant.now(), cid);
        movieAuditRepo.save(audit);
        log.info("Processed incoming movie event - CREATED, id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

}
