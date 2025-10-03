package com.page.example.paging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Component
public class MovieKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(MovieKafkaListener.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieService movieService;

    @KafkaListener(topics = "movies", groupId = "movie-group")
    public void listen(String message) {
        log.info("Received message on 'movies' topic: {}", message);
        try {
            Movie incoming = objectMapper.readValue(message, Movie.class);
            if (incoming.getName() == null || incoming.getName().isBlank()) {
                log.warn("Received movie event with empty name, skipping: {}", message);
                return;
            }

            Movie processed = movieService.processIncomingMovie(incoming);
            log.info("Processed movie event result id={}, name={}", processed.getId(), processed.getName());
        } catch (Exception e) {
            log.error("Failed to process movie event: {}", e.getMessage(), e);
        }
    }

}
