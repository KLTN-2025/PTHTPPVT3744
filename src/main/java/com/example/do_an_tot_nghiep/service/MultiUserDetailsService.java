package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.model.Employee;
import com.example.do_an_tot_nghiep.repository.ICustomerRepository;
import com.example.do_an_tot_nghiep.repository.IEmployeeRepository;
import com.example.do_an_tot_nghiep.security.CustomerUserDetails;
import com.example.do_an_tot_nghiep.security.EmployeeDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MultiUserDetailsService implements UserDetailsService {

    @Autowired
    private IEmployeeRepository employeeRepo;

    @Autowired
    private ICustomerRepository customerRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1️⃣ Thử tìm trong bảng Employee
        Employee employee = employeeRepo.findByUsername(username).orElse(null);
        if (employee != null) {
            return new EmployeeDetails(employee);
        }

        // 2️⃣ Nếu không có, thử tìm trong bảng Customer
        Customer customer = customerRepo.findByUsername(username).orElse(null);
        if (customer != null) {
            return new CustomerUserDetails(customer);
        }

        // 3️⃣ Không tồn tại
        throw new UsernameNotFoundException("Tài khoản không tồn tại: " + username);
    }
}
