package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Employee;
import com.example.do_an_tot_nghiep.model.EmployeeSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IEmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, Integer> {
    List<EmployeeSchedule> findByEmployee(Employee employee);

    List<EmployeeSchedule> findByWorkDate(java.time.LocalDate workDate);

    @Query("SELECT es FROM EmployeeSchedule es WHERE es.employee = :employee " +
            "AND es.workDate BETWEEN :startDate AND :endDate")
    List<EmployeeSchedule> findByEmployeeAndDateRange(
            @Param("employee") Employee employee,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate
    );

    Optional<EmployeeSchedule> findByEmployeeAndWorkDate(Employee employee, java.time.LocalDate workDate);

    List<EmployeeSchedule> findByShift(EmployeeSchedule.Shift shift);
}
