package com.example.do_an_tot_nghiep.security;

import com.example.do_an_tot_nghiep.model.Employee;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class EmployeeDetails implements UserDetails {

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
        this.employeeId = employee.getEmployeeId();
        this.username = employee.getUsername();
        this.password = employee.getPasswordHash();
        this.fullName = employee.getFullName();
        this.email = employee.getEmail();
        this.position = employee.getPosition();
        this.avatarUrl = employee.getAvatarUrl();
        this.roleName = employee.getRole().getRoleName();
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + employee.getRole().getRoleName().toUpperCase())
        );
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
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
