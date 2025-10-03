package com.page.example.paging;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieAuditRepo extends CrudRepository<MovieAudit, Long> {
}
