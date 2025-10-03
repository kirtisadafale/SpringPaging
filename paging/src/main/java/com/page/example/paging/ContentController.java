package com.page.example.paging;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.kafka.core.KafkaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
public class ContentController {

    @Autowired
    private MovieRepo movieRepo;

    private final Logger log = LoggerFactory.getLogger(ContentController.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.kafka.topic.movies:movies}")
    private String moviesTopic;

    @GetMapping("/movies")
    public PagedResponse<Movie> getMovies(@RequestParam(defaultValue = "0") int pageNo,
                                         @RequestParam(defaultValue = "10") int pageSize) {

        System.out.println("Page No: " + pageNo + " Page Size: " + pageSize);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        var page = movieRepo.findAll(pageable);
        return new PagedResponse<>(page.getContent(), page.getNumber(), page.getSize());

    }

    @PostMapping("/movies")
    public ResponseEntity<Object> AddMovies(@RequestBody Movie movie) {
        // validate input
        if (movie == null || movie.getName() == null || movie.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "movie and movie.name must be provided"));
        }

        // if a movie with the same name exists, return it instead of creating a duplicate
        var existing = movieRepo.findByName(movie.getName());
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }

        try {
            // movie will be saved by the Kafka consumer; controller only enqueues the event
            String payload = objectMapper.writeValueAsString(movie);
            kafkaTemplate.send(moviesTopic, payload);
            log.info("Enqueued movie create event to topic {}", moviesTopic);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("status", "queued"));
        } catch (Exception e) {
            log.error("Failed to enqueue movie event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "failed to enqueue movie event"));
        }
    }
    

}

