package com.example.do_an_tot_nghiep.security;

import com.example.do_an_tot_nghiep.model.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class EmployeeDetails implements UserDetails {
    private final Employee employee;
    private final Integer employeeId;
    private final String username;
    private final String password;
    private final String fullName;
    private final String email;
    private final String position;
    private final String avatarUrl;
    private final String roleName;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public EmployeeDetails(Employee employee) {
        this.employee = employee;
        this.employeeId = employee.getEmployeeId();
        this.username = employee.getUsername();
        this.password = employee.getPasswordHash();
        this.fullName = employee.getFullName();
        this.email = employee.getEmail();
        this.position = employee.getPosition();
        this.avatarUrl = employee.getAvatarUrl();

        if (employee.getRole() != null && employee.getRole().getRoleName() != null) {
            this.roleName = employee.getRole().getRoleName();
            this.authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + employee.getRole().getRoleName().toUpperCase())
            );
        } else {
            this.roleName = "UNKNOWN";
            this.authorities = Collections.emptyList();
        }

        this.enabled = employee.getStatus() == Employee.EmployeeStatus.ACTIVE;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Integer getEmployeeId() {
        return employee != null ? employee.getEmployeeId() : null;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    // ✅ THÊM GETTER NÀY
    public String getPosition() {
        return position;
    }

    // ✅ BONUS: Thêm getter cho email nếu cần
    public String getEmail() {
        return email;
    }

    public Employee getEmployee() {
        return employee;
    }
}