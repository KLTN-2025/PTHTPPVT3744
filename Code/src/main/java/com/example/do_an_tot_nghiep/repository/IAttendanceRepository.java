package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Attendance;
import com.example.do_an_tot_nghiep.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAttendanceRepository extends JpaRepository<Attendance, Integer> {
    List<Attendance> findByEmployee(Employee employee);

    @Query("SELECT a FROM Attendance a WHERE a.employee = :employee " +
            "AND DATE(a.checkIn) BETWEEN :startDate AND :endDate")
    List<Attendance> findByEmployeeAndDateRange(
            @Param("employee") Employee employee,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate
    );

    @Query("SELECT a FROM Attendance a WHERE a.employee = :employee " +
            "AND DATE(a.checkIn) = :date")
    Optional<Attendance> findByEmployeeAndDate(
            @Param("employee") Employee employee,
            @Param("date") java.time.LocalDate date
    );

    @Query("SELECT SUM(a.workHours) FROM Attendance a WHERE a.employee = :employee " +
            "AND MONTH(a.checkIn) = :month AND YEAR(a.checkIn) = :year")
    java.math.BigDecimal getTotalWorkHoursByMonth(
            @Param("employee") Employee employee,
            @Param("month") Integer month,
            @Param("year") Integer year
    );
}
