package com.page.example.paging;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;

/**
 * Persistent token bucket for rate limiting.
 */
@Entity
@Table(name = "token_bucket")
public class TokenBucket {

    @Id
    @Column(name = "client_id", length = 200)
    private String clientId;

    @Column(name = "tokens")
    private double tokens;

    @Column(name = "capacity")
    private int capacity;

    @Column(name = "refill_rate")
    private double refillRate; // tokens per second

    @Column(name = "last_refill")
    private Instant lastRefill;

    protected TokenBucket() {
        // JPA
    }

    public TokenBucket(String clientId, int capacity, double refillRate, Instant now) {
        this.clientId = clientId;
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = capacity; // start full
        this.lastRefill = now;
    }

    public String getClientId() {
        return clientId;
    }

    public double getTokens() {
        return tokens;
    }

    public void setTokens(double tokens) {
        this.tokens = tokens;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getRefillRate() {
        return refillRate;
    }

    public Instant getLastRefill() {
        return lastRefill;
    }

    public void setLastRefill(Instant lastRefill) {
        this.lastRefill = lastRefill;
    }
}
