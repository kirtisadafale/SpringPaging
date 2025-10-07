package com.page.example.paging;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.kafka.core.KafkaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;



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
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         HttpServletRequest request) {

        System.out.println("Page No: " + pageNo + " Page Size: " + pageSize);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        var page = movieRepo.findAll(pageable);
        long totalElements = page.getTotalElements();
        int totalPages = page.getTotalPages();
        // compute 1-based showing range
        long showingFrom = (long) page.getNumber() * page.getSize() + 1;
        long showingTo = showingFrom + page.getNumberOfElements() - 1;
        if (page.getNumberOfElements() == 0) {
            showingFrom = 0;
            showingTo = 0;
        }

    // Convert to 1-based page number for client friendliness
    int clientPageNo = page.getNumber() + 1;

    // determine next/previous
    boolean hasNext = page.hasNext();
    boolean hasPrevious = page.hasPrevious();

    // build absolute base URL using request scheme + Host header
    String hostHeader = request.getHeader("Host");
    String baseUrl = request.getScheme() + "://" + (hostHeader != null ? hostHeader : request.getServerName() + ":" + request.getServerPort());

    String nextUrl = null;
    String prevUrl = null;
    if (hasNext) {
        nextUrl = baseUrl + UriComponentsBuilder.fromPath("/movies")
                .queryParam("pageNo", clientPageNo)
                .queryParam("pageSize", page.getSize())
                .build()
                .encode()
                .toUriString();
    }
    if (hasPrevious) {
        prevUrl = baseUrl + UriComponentsBuilder.fromPath("/movies")
                .queryParam("pageNo", clientPageNo-2)
                .queryParam("pageSize", page.getSize())
                .build()
                .encode()
                .toUriString();
    }

    PagedResponse<Movie> resp = new PagedResponse<>(page.getContent(), clientPageNo, page.getSize(), totalElements, totalPages, showingFrom, showingTo);
    resp.setHasNext(hasNext);
    resp.setHasPrevious(hasPrevious);
    resp.setNextPageUrl(nextUrl);
    resp.setPrevPageUrl(prevUrl);

    return resp;

    }

    @PostMapping("/movies")
    public ResponseEntity<Object> AddMovies(@RequestBody Movie movie, HttpServletRequest request) {
        // validate input
        if (movie == null || movie.getName() == null || movie.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "movie and movie.name must be provided"));
        }

        
        try {
            // movie will be saved by the Kafka consumer; controller only enqueues the event
            String payload = objectMapper.writeValueAsString(movie);
            kafkaTemplate.send(moviesTopic, payload);
            log.info("Enqueued movie create event to topic {}", moviesTopic);

            // build a link where the caller can check for the movie after processing
        // Build absolute URL using request scheme(eg."http") + Host header so clients can follow the Location
        String hostHeader = request.getHeader("Host");
        String baseUrl = request.getScheme() + "://" + (hostHeader != null ? hostHeader : request.getServerName() + ":" + request.getServerPort());
        // use path variable for the check link (e.g. /movies/{name}) and encode it
        String checkLink = baseUrl + UriComponentsBuilder.fromPath("/movies/{name}")
            .buildAndExpand(movie.getName())
            .encode()
            .toUriString();

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .header(HttpHeaders.LOCATION, checkLink)
                    .body(Map.of("status", "queued", "check", checkLink));
        } catch (Exception e) {
            log.error("Failed to enqueue movie event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "failed to enqueue movie event"));
        }
    }
    
    @GetMapping("/movies/{name}")
    public ResponseEntity<Object> findMovieByName(@PathVariable(name = "name") String name) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "name path variable is required"));
        }

        var maybe = movieRepo.findByName(name);
        if (maybe.isEmpty()) {
            // not processed yet
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "pending", "message", "movie not yet processed"));
        }

        return ResponseEntity.ok(maybe.get());
    }
    

}

