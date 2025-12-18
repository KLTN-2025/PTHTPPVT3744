package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.model.Employee;
import com.example.do_an_tot_nghiep.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByCustomerOrderByCreatedAtDesc(Customer customer);

    List<Notification> findByEmployeeOrderByCreatedAtDesc(Employee employee);

    @Query("SELECT n FROM Notification n WHERE n.customer = :customer AND n.isRead = :isRead " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByCustomerAndIsRead(@Param("customer") Customer customer,
                                               @Param("isRead") Boolean isRead);

    @Query("SELECT n FROM Notification n WHERE n.employee = :employee AND n.isRead = :isRead " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByEmployeeAndIsRead(@Param("employee") Employee employee,
                                               @Param("isRead") Boolean isRead);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.customer = :customer AND n.isRead = false")
    Long countUnreadByCustomer(@Param("customer") Customer customer);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.employee = :employee AND n.isRead = false")
    Long countUnreadByEmployee(@Param("employee") Employee employee);
}
