package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IContactMessageRepository extends JpaRepository<ContactMessage, Integer> {

    // Tìm tin nhắn theo trạng thái
    List<ContactMessage> findByStatus(String status);

    // Tìm tin nhắn của khách hàng
    List<ContactMessage> findByCustomer_CustomerIdOrderByCreatedAtDesc(Integer customerId);

    // Tìm tin nhắn chưa xử lý
    List<ContactMessage> findByStatusInOrderByCreatedAtDesc(List<String> statuses);

    // Đếm số tin nhắn mới
    long countByStatus(String status);

    // Tìm tin nhắn được gán cho nhân viên
    List<ContactMessage> findByAssignedTo_EmployeeIdOrderByCreatedAtDesc(Integer employeeId);

    // Tìm tất cả tin nhắn sắp xếp theo ngày mới nhất
    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    // Tìm tin nhắn theo email
    List<ContactMessage> findByEmailOrderByCreatedAtDesc(String email);

    // Tìm tin nhắn theo số điện thoại
    List<ContactMessage> findByPhoneOrderByCreatedAtDesc(String phone);
}