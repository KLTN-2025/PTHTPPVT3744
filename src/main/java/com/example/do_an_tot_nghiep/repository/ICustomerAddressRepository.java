package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.model.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICustomerAddressRepository extends JpaRepository<CustomerAddress, Integer> {
    List<CustomerAddress> findByCustomer(Customer customer);

    Optional<CustomerAddress> findByCustomerAndIsDefaultTrue(Customer customer);

    @Query("SELECT ca FROM CustomerAddress ca WHERE ca.customer = :customer ORDER BY ca.isDefault DESC, ca.createdAt DESC")
    List<CustomerAddress> findByCustomerOrderByDefault(@Param("customer") Customer customer);
}
