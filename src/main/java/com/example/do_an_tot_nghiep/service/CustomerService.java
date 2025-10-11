package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.CustomerDTO;
import com.example.do_an_tot_nghiep.dto.CustomerRegistrationRequest;
import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.repository.ICustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerService implements ICustomerService {
    @Autowired
    private ICustomerRepository customerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public CustomerDTO registerCustomer(CustomerRegistrationRequest request) {
        // Validate username and email uniqueness
        if (customerRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Generate customer code
        String customerCode = generateCustomerCode();

        // Create customer entity
        Customer customer = Customer.builder()
                .customerCode(customerCode)
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .customerTier(Customer.CustomerTier.BRONZE)
                .loyaltyPoints(0)
                .totalSpent(BigDecimal.ZERO)
                .totalOrders(0)
                .status(Customer.CustomerStatus.ACTIVE)
                .referralCode(generateReferralCode())
                .build();

        customer = customerRepository.save(customer);

        return convertToDTO(customer);
    }
    @Override
    public CustomerDTO getCustomerById(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return convertToDTO(customer);
    }

    public CustomerDTO getCustomerByUsername(String username) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return convertToDTO(customer);
    }

    @Transactional
    @Override
    public void updateCustomerTier(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        BigDecimal totalSpent = customer.getTotalSpent();
        Customer.CustomerTier newTier;

        if (totalSpent.compareTo(new BigDecimal("50000000")) >= 0) {
            newTier = Customer.CustomerTier.PLATINUM;
        } else if (totalSpent.compareTo(new BigDecimal("15000000")) >= 0) {
            newTier = Customer.CustomerTier.GOLD;
        } else if (totalSpent.compareTo(new BigDecimal("5000000")) >= 0) {
            newTier = Customer.CustomerTier.SILVER;
        } else {
            newTier = Customer.CustomerTier.BRONZE;
        }

        customer.setCustomerTier(newTier);
        customerRepository.save(customer);
    }

    @Transactional
    @Override
    public void addLoyaltyPoints(Integer customerId, Integer points, String description) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
        customerRepository.save(customer);

        // Create loyalty history record
        // (Implement this in LoyaltyHistoryService)
    }

    @Transactional
    @Override
    public void redeemLoyaltyPoints(Integer customerId, Integer points) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (customer.getLoyaltyPoints() < points) {
            throw new RuntimeException("Insufficient loyalty points");
        }

        customer.setLoyaltyPoints(customer.getLoyaltyPoints() - points);
        customerRepository.save(customer);
    }
    @Override
    public List<CustomerDTO> getTopCustomers(int limit) {
        return customerRepository.findTopCustomers(PageRequest.of(0, limit))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Override
    public String generateCustomerCode() {
        String prefix = "CUS";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + timestamp.substring(timestamp.length() - 8);
    }
    @Override
    public String generateReferralCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    @Override
    public CustomerDTO convertToDTO(Customer customer) {
        return CustomerDTO.builder()
                .customerId(customer.getCustomerId())
                .customerCode(customer.getCustomerCode())
                .username(customer.getUsername())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .avatarUrl(customer.getAvatarUrl())
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender() != null ? customer.getGender().name() : null)
                .customerTier(customer.getCustomerTier().name())
                .loyaltyPoints(customer.getLoyaltyPoints())
                .totalSpent(customer.getTotalSpent())
                .totalOrders(customer.getTotalOrders())
                .status(customer.getStatus().name())
                .emailVerified(customer.getEmailVerified())
                .phoneVerified(customer.getPhoneVerified())
                .build();
    }
}

