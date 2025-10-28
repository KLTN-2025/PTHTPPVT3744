package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.dto.CustomerDTO;
import com.example.do_an_tot_nghiep.model.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;


@Repository
public interface ICustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByUsername(String username);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByCustomerCode(String customerCode);

    Optional<Customer> findByReferralCode(String referralCode);

    List<Customer> findByCustomerTier(Customer.CustomerTier tier);

    List<Customer> findByStatus(Customer.CustomerStatus status);

    @Query("SELECT c FROM Customer c WHERE c.totalSpent >= :minAmount ORDER BY c.totalSpent DESC")
    List<Customer> findVipCustomers(@Param("minAmount") java.math.BigDecimal minAmount);

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Customer> searchCustomers(@Param("keyword") String keyword);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt >= :fromDate")
    Long countNewCustomers(@Param("fromDate") java.time.LocalDateTime fromDate);
    @Query("SELECT c FROM Customer c WHERE c.status = 'ACTIVE' " +
            "ORDER BY c.totalSpent DESC")
    List<Customer> findTopCustomers(Pageable pageable);

    /**
     * Kiểm tra email đã tồn tại
     */
    boolean existsByEmail(String email);
}
