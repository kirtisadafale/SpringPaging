package com.page.example.paging;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepo extends PagingAndSortingRepository<Movie, Long> {
    // expose save explicitly to satisfy the compiler during incremental builds
    Movie save(Movie movie);
}
