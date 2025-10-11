package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBlogPostRepository extends JpaRepository<BlogPost, Integer> {
}
