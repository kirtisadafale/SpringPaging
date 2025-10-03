package com.page.example.paging;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepo extends PagingAndSortingRepository<Movie, Long> {
    // expose save explicitly to satisfy the compiler during incremental builds
    Movie save(Movie movie);

    // find by name and check existence to support "insert-if-missing" semantics
    Optional<Movie> findByName(String name);

    boolean existsByName(String name);
}
