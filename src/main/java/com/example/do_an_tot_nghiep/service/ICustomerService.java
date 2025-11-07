package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.CustomerDTO;
import com.example.do_an_tot_nghiep.dto.CustomerRegistrationRequest;
import com.example.do_an_tot_nghiep.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ICustomerService {
    CustomerDTO registerCustomer(CustomerRegistrationRequest request);
    CustomerDTO getCustomerById(Integer customerId);
    void updateCustomerTier(Integer customerId);
    void addLoyaltyPoints(Integer customerId, Integer points, String description);
    void redeemLoyaltyPoints(Integer customerId, Integer points);
    List<CustomerDTO> getTopCustomers(int limit);
    String generateCustomerCode();
    String generateReferralCode();
    CustomerDTO convertToDTO(Customer customer);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    CustomerDTO save(CustomerDTO customerDTO);
    CustomerDTO findByEmail(String email);

    // ✅ Thêm methods mới
    CustomerDTO setPassword(Integer customerId, String newPassword);
    void updateLastLogin(Integer customerId);

    Page<Customer> findAll(PageRequest of);


    Page<Customer> findCustomers(String keyword, String tier, String status, int page, int pageSize);

    void deleteCustomerById(Integer id);

    void deleteCustomers(List<Integer> ids);
}