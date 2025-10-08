package com.page.example.paging;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface TokenBucketRepository extends CrudRepository<TokenBucket, String> {

    /**
     * Load a token bucket for the given client id using a pessimistic write lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TokenBucket t where t.clientId = :clientId")
    Optional<TokenBucket> findByClientIdForUpdate(@Param("clientId") String clientId);
}
