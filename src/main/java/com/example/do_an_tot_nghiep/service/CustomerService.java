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
import java.time.LocalDateTime;
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
                .provider("LOCAL")  // ✅ Đăng ký thông thường
                .hasCustomPassword(true)
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
                .provider(customer.getProvider())  // ✅ Thêm
                .providerId(customer.getProviderId())  // ✅ Thêm
                .hasCustomPassword(customer.getHasCustomPassword())  // ✅ Thêm
                .referralCode(customer.getReferralCode())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    @Override
    public boolean existsByUsername(String username) {
        return customerRepository.findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByPhone(String phone) {
        return customerRepository.findByPhone(phone).isPresent();
    }

    @Override
    @Transactional
    public CustomerDTO save(CustomerDTO customerDTO) {
        Customer customer;

        // ✅ Nếu đã có customerId -> Update
        if (customerDTO.getCustomerId() != null) {
            customer = customerRepository.findById(customerDTO.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Update fields
            updateCustomerFromDTO(customer, customerDTO);

        } else {
            // ✅ Tạo mới
            customer = Customer.builder()
                    .customerCode(customerDTO.getCustomerCode() != null ?
                            customerDTO.getCustomerCode() : generateCustomerCode())
                    .username(customerDTO.getUsername())
                    .passwordHash(customerDTO.getPasswordHash())
                    .provider(customerDTO.getProvider())
                    .providerId(customerDTO.getProviderId())
                    .hasCustomPassword(customerDTO.getHasCustomPassword() != null ?
                            customerDTO.getHasCustomPassword() : false)
                    .fullName(customerDTO.getFullName())
                    .email(customerDTO.getEmail())
                    .phone(customerDTO.getPhone())
                    .address(customerDTO.getAddress())
                    .avatarUrl(customerDTO.getAvatarUrl())
                    .dateOfBirth(customerDTO.getDateOfBirth())
                    .gender(customerDTO.getGender() != null ?
                            Customer.Gender.valueOf(customerDTO.getGender()) : null)
                    .customerTier(customerDTO.getCustomerTier() != null ?
                            Customer.CustomerTier.valueOf(customerDTO.getCustomerTier()) :
                            Customer.CustomerTier.BRONZE)
                    .loyaltyPoints(customerDTO.getLoyaltyPoints() != null ?
                            customerDTO.getLoyaltyPoints() : 0)
                    .totalSpent(customerDTO.getTotalSpent() != null ?
                            customerDTO.getTotalSpent() : BigDecimal.ZERO)
                    .totalOrders(customerDTO.getTotalOrders() != null ?
                            customerDTO.getTotalOrders() : 0)
                    .status(customerDTO.getStatus() != null ?
                            Customer.CustomerStatus.valueOf(customerDTO.getStatus()) :
                            Customer.CustomerStatus.ACTIVE)
                    .emailVerified(customerDTO.getEmailVerified() != null ?
                            customerDTO.getEmailVerified() : false)
                    .phoneVerified(customerDTO.getPhoneVerified() != null ?
                            customerDTO.getPhoneVerified() : false)
                    .referralCode(customerDTO.getReferralCode() != null ?
                            customerDTO.getReferralCode() : generateReferralCode())
                    .build();
        }

        customer = customerRepository.save(customer);
        return convertToDTO(customer);
    }

    /**
     * ✅ Helper method để update customer từ DTO
     */
    private void updateCustomerFromDTO(Customer customer, CustomerDTO dto) {
        if (dto.getUsername() != null) {
            customer.setUsername(dto.getUsername());
        }
        if (dto.getPasswordHash() != null) {
            customer.setPasswordHash(dto.getPasswordHash());
        }
        if (dto.getProvider() != null) {
            customer.setProvider(dto.getProvider());
        }
        if (dto.getProviderId() != null) {
            customer.setProviderId(dto.getProviderId());
        }
        if (dto.getHasCustomPassword() != null) {
            customer.setHasCustomPassword(dto.getHasCustomPassword());
        }
        if (dto.getFullName() != null) {
            customer.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null) {
            customer.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            customer.setPhone(dto.getPhone());
        }
        if (dto.getAddress() != null) {
            customer.setAddress(dto.getAddress());
        }
        if (dto.getAvatarUrl() != null) {
            customer.setAvatarUrl(dto.getAvatarUrl());
        }
        if (dto.getDateOfBirth() != null) {
            customer.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getGender() != null) {
            customer.setGender(Customer.Gender.valueOf(dto.getGender()));
        }
        if (dto.getCustomerTier() != null) {
            customer.setCustomerTier(Customer.CustomerTier.valueOf(dto.getCustomerTier()));
        }
        if (dto.getLoyaltyPoints() != null) {
            customer.setLoyaltyPoints(dto.getLoyaltyPoints());
        }
        if (dto.getTotalSpent() != null) {
            customer.setTotalSpent(dto.getTotalSpent());
        }
        if (dto.getTotalOrders() != null) {
            customer.setTotalOrders(dto.getTotalOrders());
        }
        if (dto.getStatus() != null) {
            customer.setStatus(Customer.CustomerStatus.valueOf(dto.getStatus()));
        }
        if (dto.getEmailVerified() != null) {
            customer.setEmailVerified(dto.getEmailVerified());
        }
        if (dto.getPhoneVerified() != null) {
            customer.setPhoneVerified(dto.getPhoneVerified());
        }
        if (dto.getReferralCode() != null) {
            customer.setReferralCode(dto.getReferralCode());
        }
    }

    @Override
    public CustomerDTO findByEmail(String email) {
        return customerRepository.findByEmail(email)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * ✅ Method mới: Set password cho OAuth2 user
     */
    @Transactional
    public CustomerDTO setPassword(Integer customerId, String newPassword) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Encode và set password
        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        customer.setHasCustomPassword(true);

        customer = customerRepository.save(customer);

        System.out.println("✅ Password set for customer: " + customer.getEmail() +
                " (ID: " + customerId + ")");

        return convertToDTO(customer);
    }

    /**
     * ✅ Method mới: Update last login
     */
    @Transactional
    public void updateLastLogin(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setLastLogin(LocalDateTime.now());
        customerRepository.save(customer);
    }
}