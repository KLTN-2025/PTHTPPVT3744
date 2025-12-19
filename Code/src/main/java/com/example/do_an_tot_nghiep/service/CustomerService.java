package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.CustomerDTO;
import com.example.do_an_tot_nghiep.dto.CustomerRegistrationRequest;
import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.repository.ICustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    // ===================== REGISTER =====================
    @Transactional
    @Override
    public CustomerDTO registerCustomer(CustomerRegistrationRequest request) {

        if (customerRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Customer customer = Customer.builder()
                .customerCode(generateCustomerCode())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .provider("LOCAL")
                .hasCustomPassword(true)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())

                // 🔒 KHÓA CỨNG BRONZE
                .customerTier(Customer.CustomerTier.BRONZE)

                .loyaltyPoints(0)
                .totalSpent(BigDecimal.ZERO)
                .totalOrders(0)
                .status(Customer.CustomerStatus.ACTIVE)
                .emailVerified(false)
                .phoneVerified(false)
                .referralCode(generateReferralCode())
                .build();

        customer = customerRepository.save(customer);
        return convertToDTO(customer);
    }

    // ===================== CRUD / ADMIN SAVE =====================
    @Override
    @Transactional
    public CustomerDTO save(CustomerDTO customerDTO) {

        Customer customer;

        // ===== UPDATE =====
        if (customerDTO.getCustomerId() != null) {
            customer = customerRepository.findById(customerDTO.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            updateCustomerFromDTO(customer, customerDTO);

        }
        // ===== CREATE =====
        else {
            customer = Customer.builder()
                    .customerCode(
                            customerDTO.getCustomerCode() != null
                                    ? customerDTO.getCustomerCode()
                                    : generateCustomerCode()
                    )
                    .username(customerDTO.getUsername())
                    .passwordHash(customerDTO.getPasswordHash())
                    .provider(customerDTO.getProvider())
                    .providerId(customerDTO.getProviderId())
                    .hasCustomPassword(customerDTO.getHasCustomPassword() != null
                            ? customerDTO.getHasCustomPassword()
                            : false)
                    .fullName(customerDTO.getFullName())
                    .email(customerDTO.getEmail())
                    .phone(customerDTO.getPhone())
                    .address(customerDTO.getAddress())
                    .avatarUrl(customerDTO.getAvatarUrl())
                    .dateOfBirth(customerDTO.getDateOfBirth())
                    .gender(customerDTO.getGender() != null
                            ? Customer.Gender.valueOf(customerDTO.getGender())
                            : null)

                    // 🔒 TẠO MỚI → LUÔN BRONZE (KHÔNG DÙNG DTO)
                    .customerTier(Customer.CustomerTier.BRONZE)

                    .loyaltyPoints(0)
                    .totalSpent(BigDecimal.ZERO)
                    .totalOrders(0)
                    .status(Customer.CustomerStatus.ACTIVE)
                    .emailVerified(false)
                    .phoneVerified(false)
                    .referralCode(generateReferralCode())
                    .build();
        }

        // 🔒 ÉP LẠI LẦN CUỐI (CHỐNG GHI ĐÈ)
        if (customer.getCustomerId() == null) {
            customer.setCustomerTier(Customer.CustomerTier.BRONZE);
        }

        customer = customerRepository.save(customer);
        return convertToDTO(customer);
    }

    // ===================== UPDATE FROM DTO =====================
    private void updateCustomerFromDTO(Customer customer, CustomerDTO dto) {

        if (dto.getUsername() != null) customer.setUsername(dto.getUsername());
        if (dto.getPasswordHash() != null) customer.setPasswordHash(dto.getPasswordHash());
        if (dto.getProvider() != null) customer.setProvider(dto.getProvider());
        if (dto.getProviderId() != null) customer.setProviderId(dto.getProviderId());
        if (dto.getHasCustomPassword() != null) customer.setHasCustomPassword(dto.getHasCustomPassword());
        if (dto.getFullName() != null) customer.setFullName(dto.getFullName());
        if (dto.getEmail() != null) customer.setEmail(dto.getEmail());
        if (dto.getPhone() != null) customer.setPhone(dto.getPhone());
        if (dto.getAddress() != null) customer.setAddress(dto.getAddress());
        if (dto.getAvatarUrl() != null) customer.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getDateOfBirth() != null) customer.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) customer.setGender(Customer.Gender.valueOf(dto.getGender()));

        // ⚠️ UPDATE mới cho phép set tier (admin)
        if (dto.getCustomerTier() != null) {
            customer.setCustomerTier(Customer.CustomerTier.valueOf(dto.getCustomerTier()));
        }

        if (dto.getLoyaltyPoints() != null) customer.setLoyaltyPoints(dto.getLoyaltyPoints());
        if (dto.getTotalSpent() != null) customer.setTotalSpent(dto.getTotalSpent());
        if (dto.getTotalOrders() != null) customer.setTotalOrders(dto.getTotalOrders());
        if (dto.getStatus() != null) customer.setStatus(Customer.CustomerStatus.valueOf(dto.getStatus()));
        if (dto.getEmailVerified() != null) customer.setEmailVerified(dto.getEmailVerified());
        if (dto.getPhoneVerified() != null) customer.setPhoneVerified(dto.getPhoneVerified());
        if (dto.getReferralCode() != null) customer.setReferralCode(dto.getReferralCode());
    }

    // ===================== BUSINESS LOGIC =====================
    @Transactional
    @Override
    public void updateCustomerTier(Integer customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        BigDecimal totalSpent = customer.getTotalSpent();
        Customer.CustomerTier newTier;

        if (totalSpent != null && totalSpent.compareTo(new BigDecimal("50000000")) >= 0) {
            newTier = Customer.CustomerTier.PLATINUM;
        } else if (totalSpent != null && totalSpent.compareTo(new BigDecimal("15000000")) >= 0) {
            newTier = Customer.CustomerTier.GOLD;
        } else if (totalSpent != null && totalSpent.compareTo(new BigDecimal("5000000")) >= 0) {
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

        int safePoints = points != null ? points : 0;
        customer.setLoyaltyPoints((customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0) + safePoints);

        customerRepository.save(customer);
    }

    @Transactional
    @Override
    public void redeemLoyaltyPoints(Integer customerId, Integer points) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        int current = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
        int need = points != null ? points : 0;

        if (need <= 0) return;

        if (current < need) {
            throw new RuntimeException("Insufficient loyalty points");
        }

        customer.setLoyaltyPoints(current - need);
        customerRepository.save(customer);
    }

    @Override
    public List<CustomerDTO> getTopCustomers(int limit) {
        // Nếu repo bạn có findTopCustomers(PageRequest) giống code cũ:
        return customerRepository.findTopCustomers(PageRequest.of(0, limit))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===================== HELPERS =====================
    @Override
    public String generateCustomerCode() {
        return "CUS" + System.currentTimeMillis();
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
                .customerTier(customer.getCustomerTier() != null ? customer.getCustomerTier().name() : null)
                .loyaltyPoints(customer.getLoyaltyPoints())
                .totalSpent(customer.getTotalSpent())
                .totalOrders(customer.getTotalOrders())
                .status(customer.getStatus() != null ? customer.getStatus().name() : null)
                .emailVerified(customer.getEmailVerified())
                .phoneVerified(customer.getPhoneVerified())
                .provider(customer.getProvider())
                .providerId(customer.getProviderId())
                .hasCustomPassword(customer.getHasCustomPassword())
                .referralCode(customer.getReferralCode())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    // ===================== OTHER =====================
    @Override
    public CustomerDTO getCustomerById(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return convertToDTO(customer);
    }

    @Override
    public CustomerDTO findByEmail(String email) {
        return customerRepository.findByEmail(email)
                .map(this::convertToDTO)
                .orElse(null);
    }

    // ✅ Set password cho OAuth2 user (giống bản bạn từng có)
    @Override
    @Transactional
    public CustomerDTO setPassword(Integer customerId, String newPassword) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        customer.setHasCustomPassword(true);

        customer = customerRepository.save(customer);
        return convertToDTO(customer);
    }

    @Override
    @Transactional
    public void updateLastLogin(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setLastLogin(LocalDateTime.now());
        customerRepository.save(customer);
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
    public Page<Customer> findAll(PageRequest of) {
        return customerRepository.findAll(of);
    }

    @Override
    public Page<Customer> findCustomers(String keyword, String tier, String status, int page, int pageSize) {
        int safePage = Math.max(page, 0); // ✅ CHỐT CHẶN CUỐI
        return customerRepository.searchCustomers(
                keyword,
                tier != null && !tier.isEmpty()
                        ? Customer.CustomerTier.valueOf(tier)
                        : null,
                status != null && !status.isEmpty()
                        ? Customer.CustomerStatus.valueOf(status)
                        : null,
                PageRequest.of(safePage, pageSize)
        );
    }


    @Override
    public void deleteCustomerById(Integer id) {
        customerRepository.deleteById(id);
    }

    @Override
    public void deleteCustomers(List<Integer> ids) {
        customerRepository.deleteAllById(ids);
    }
}
