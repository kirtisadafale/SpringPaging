package com.page.example.paging;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "movie_audit")
public class MovieAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "name")
    private String name;

    @Column(name = "genre")
    private String genre;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "event_time")
    private Instant eventTime;

    @Column(name = "correlation_id")
    private String correlationId;

    public MovieAudit() {
    }

    public MovieAudit(Long movieId, String name, String genre, String eventType, Instant eventTime) {
        this.movieId = movieId;
        this.name = name;
        this.genre = genre;
        this.eventType = eventType;
        this.eventTime = eventTime;
    }

    public MovieAudit(Long movieId, String name, String genre, String eventType, Instant eventTime, String correlationId) {
        this.movieId = movieId;
        this.name = name;
        this.genre = genre;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.correlationId = correlationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
