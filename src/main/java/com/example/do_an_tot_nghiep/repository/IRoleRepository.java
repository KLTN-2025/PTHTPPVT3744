package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Integer> {
    Optional<Object> findByRoleName(String roleName);
}
