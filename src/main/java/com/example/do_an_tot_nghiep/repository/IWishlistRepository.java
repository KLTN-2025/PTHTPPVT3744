package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.model.MedicalDevice;
import com.example.do_an_tot_nghiep.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWishlistRepository extends JpaRepository<Wishlist, Integer> {
    List<Wishlist> findByCustomer(Customer customer);

    Optional<Wishlist> findByCustomerAndDevice(Customer customer, MedicalDevice device);

    boolean existsByCustomerAndDevice(Customer customer, MedicalDevice device);

    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.customer = :customer")
    Long countByCustomer(@Param("customer") Customer customer);
}
