package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Cart;
import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.model.MedicalDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICartRepository extends JpaRepository<Cart, Integer> {
    List<Cart> findByCustomer(Customer customer);

    Optional<Cart> findByCustomerAndDevice(Customer customer, MedicalDevice device);

    void deleteByCustomer(Customer customer);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.customer = :customer")
    Long countByCustomer(@Param("customer") Customer customer);


}
