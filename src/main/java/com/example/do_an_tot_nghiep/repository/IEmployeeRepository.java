package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IEmployeeRepository extends JpaRepository<Employee, Integer>,
        JpaSpecificationExecutor<Employee> {

    // Authentication
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByEmail(String email);

    // Status queries
    List<Employee> findByStatus(Employee.EmployeeStatus status);
    long countByStatus(Employee.EmployeeStatus status);

    // Role queries
    long countByRole_RoleName(String roleName);

    // Search
    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Employee> searchEmployees(@Param("keyword") String keyword);

    // Statistics
    @Query("SELECT COUNT(e) FROM Employee e WHERE " +
            "MONTH(e.hireDate) = MONTH(CURRENT_DATE) AND " +
            "YEAR(e.hireDate) = YEAR(CURRENT_DATE)")
    long countNewEmployeesThisMonth();

    // Department queries
    @Query("SELECT DISTINCT e.department FROM Employee e WHERE e.department IS NOT NULL ORDER BY e.department")
    List<String> findDistinctDepartments();

    // Position queries
    @Query("SELECT DISTINCT e.position FROM Employee e WHERE e.position IS NOT NULL ORDER BY e.position")
    List<String> findDistinctPositions();
}