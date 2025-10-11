package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPromotionRepository extends JpaRepository<Promotion, Integer> {
    Optional<Promotion> findByCodeAndIsActiveTrue(String promotionCode);
}
