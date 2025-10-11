package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.CustomerDTO;
import com.example.do_an_tot_nghiep.dto.CustomerRegistrationRequest;
import com.example.do_an_tot_nghiep.model.Customer;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ICustomerService {
    @Transactional
    CustomerDTO registerCustomer(CustomerRegistrationRequest request);

    CustomerDTO getCustomerById(Integer customerId);

    @Transactional
    void updateCustomerTier(Integer customerId);

    @Transactional
    void addLoyaltyPoints(Integer customerId, Integer points, String description);

    @Transactional
    void redeemLoyaltyPoints(Integer customerId, Integer points);

    List<CustomerDTO> getTopCustomers(int i);

    String generateCustomerCode();

    String generateReferralCode();

    CustomerDTO convertToDTO(Customer customer);
}
