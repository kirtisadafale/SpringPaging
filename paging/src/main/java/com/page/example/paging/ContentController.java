package com.page.example.paging;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
public class ContentController {

    @Autowired
    private MovieRepo movieRepo;

    @GetMapping("/movies")
    public PagedResponse<Movie> getMovies(@RequestParam(defaultValue = "0") int pageNo,
                                         @RequestParam(defaultValue = "10") int pageSize) {

        System.out.println("Page No: " + pageNo + " Page Size: " + pageSize);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        var page = movieRepo.findAll(pageable);
        return new PagedResponse<>(page.getContent(), page.getNumber(), page.getSize());

    }

    @PostMapping("/movies")
    public Movie AddMovies(@RequestBody Movie movie) {
        Movie entity = movieRepo.save(movie);  
        return entity;
    }
    

}

