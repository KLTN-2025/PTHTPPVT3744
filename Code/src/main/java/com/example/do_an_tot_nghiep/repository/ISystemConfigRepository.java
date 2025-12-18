package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISystemConfigRepository extends JpaRepository<SystemConfig, Integer> {
}
