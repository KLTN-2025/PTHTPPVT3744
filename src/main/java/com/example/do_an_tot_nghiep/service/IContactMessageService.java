package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.model.ContactMessage;
import com.example.do_an_tot_nghiep.dto.ContactMessageDTO;

import java.util.List;
import java.util.Optional;

public interface IContactMessageService {

    /**
     * Lưu tin nhắn liên hệ mới
     */
    ContactMessage saveContactMessage(ContactMessageDTO dto);

    /**
     * Tìm tin nhắn theo ID
     */
    Optional<ContactMessage> findById(Integer messageId);

    /**
     * Lấy tất cả tin nhắn
     */
    List<ContactMessage> findAll();

    /**
     * Tìm tin nhắn theo trạng thái
     */
    List<ContactMessage> findByStatus(String status);

    /**
     * Tìm tin nhắn của khách hàng
     */
    List<ContactMessage> findByCustomerId(Integer customerId);

    /**
     * Tìm tin nhắn chưa xử lý (NEW, PROCESSING)
     */
    List<ContactMessage> findPendingMessages();

    /**
     * Cập nhật trạng thái tin nhắn
     */
    void updateStatus(Integer messageId, String newStatus);

    /**
     * Gán tin nhắn cho nhân viên
     */
    void assignToEmployee(Integer messageId, Integer employeeId);

    /**
     * Trả lời tin nhắn
     */
    void replyMessage(Integer messageId, Integer employeeId, String replyContent);

    /**
     * Đếm số tin nhắn mới
     */
    long countNewMessages();

    /**
     * Xóa tin nhắn
     */
    void deleteMessage(Integer messageId);
}