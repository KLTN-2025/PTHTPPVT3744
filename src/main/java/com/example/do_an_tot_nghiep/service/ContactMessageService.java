package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.ContactMessageDTO;
import com.example.do_an_tot_nghiep.model.ContactMessage;
import com.example.do_an_tot_nghiep.model.ContactMessage.MessageStatus;
import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.model.Employee;
import com.example.do_an_tot_nghiep.repository.IContactMessageRepository;
import com.example.do_an_tot_nghiep.repository.ICustomerRepository;
import com.example.do_an_tot_nghiep.repository.IEmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ContactMessageService implements IContactMessageService {

    @Autowired
    private IContactMessageRepository contactMessageRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private IEmployeeRepository employeeRepository;

    @Override
    public ContactMessage saveContactMessage(ContactMessageDTO dto) {
        ContactMessage contactMessage = new ContactMessage();

        // Set customer nếu có
        if (dto.getCustomerId() != null) {
            Optional<Customer> customer = customerRepository.findById(dto.getCustomerId());
            customer.ifPresent(contactMessage::setCustomer);
        }

        // Set thông tin cơ bản
        contactMessage.setName(dto.getName().trim());
        contactMessage.setEmail(dto.getEmail().trim().toLowerCase());
        contactMessage.setPhone(dto.getPhone().trim());
        contactMessage.setSubject(dto.getSubject().trim());
        contactMessage.setMessage(dto.getMessage().trim());
        contactMessage.setStatus(MessageStatus.New);

        return contactMessageRepository.save(contactMessage);
    }

    @Override
    public Optional<ContactMessage> findById(Integer messageId) {
        return contactMessageRepository.findById(messageId);
    }

    @Override
    public List<ContactMessage> findAll() {
        return contactMessageRepository.findAll();
    }

    @Override
    public List<ContactMessage> findByStatus(String status) {
        return contactMessageRepository.findByStatus(status);
    }

    @Override
    public List<ContactMessage> findByCustomerId(Integer customerId) {
        return contactMessageRepository.findByCustomer_CustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    public List<ContactMessage> findPendingMessages() {
        return contactMessageRepository.findByStatusInOrderByCreatedAtDesc(
                Arrays.asList("New", "Processing")
        );
    }

    @Override
    public void updateStatus(Integer messageId, String newStatus) {
        Optional<ContactMessage> messageOpt = contactMessageRepository.findById(messageId);
        if (messageOpt.isPresent()) {
            ContactMessage message = messageOpt.get();
            message.setStatus(MessageStatus.valueOf(newStatus));
            contactMessageRepository.save(message);
        } else {
            throw new RuntimeException("Không tìm thấy tin nhắn với ID: " + messageId);
        }
    }

    @Override
    public void assignToEmployee(Integer messageId, Integer employeeId) {
        Optional<ContactMessage> messageOpt = contactMessageRepository.findById(messageId);
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);

        if (messageOpt.isPresent() && employeeOpt.isPresent()) {
            ContactMessage message = messageOpt.get();
            message.setAssignedTo(employeeOpt.get());
            message.setStatus(MessageStatus.Processing);
            contactMessageRepository.save(message);
        } else {
            throw new RuntimeException("Không tìm thấy tin nhắn hoặc nhân viên");
        }
    }

    @Override
    public void replyMessage(Integer messageId, Integer employeeId, String replyContent) {
        Optional<ContactMessage> messageOpt = contactMessageRepository.findById(messageId);
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);

        if (messageOpt.isPresent() && employeeOpt.isPresent()) {
            ContactMessage message = messageOpt.get();
            message.setRepliedBy(employeeOpt.get());
            message.setReplyContent(replyContent.trim());
            message.setRepliedAt(LocalDateTime.now());
            message.setStatus(MessageStatus.Resolved);
            contactMessageRepository.save(message);
        } else {
            throw new RuntimeException("Không tìm thấy tin nhắn hoặc nhân viên");
        }
    }

    @Override
    public long countNewMessages() {
        return contactMessageRepository.countByStatus("New");
    }

    @Override
    public void deleteMessage(Integer messageId) {
        if (contactMessageRepository.existsById(messageId)) {
            contactMessageRepository.deleteById(messageId);
        } else {
            throw new RuntimeException("Không tìm thấy tin nhắn với ID: " + messageId);
        }
    }
}