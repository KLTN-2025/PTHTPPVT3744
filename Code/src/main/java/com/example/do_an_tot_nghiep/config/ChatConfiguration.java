package com.example.do_an_tot_nghiep.config;

import com.example.do_an_tot_nghiep.service.ConversationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ChatConfiguration {

    private final ConversationHistoryService historyService;

    /**
     * Dọn dẹp các conversation không hoạt động quá 24 giờ
     * Chạy mỗi 6 giờ
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 giờ
    public void cleanupInactiveConversations() {
        log.info("Starting cleanup of inactive conversations");
        try {
            historyService.cleanupOldConversations(24); // Xóa conversation cũ hơn 24h
            log.info("Completed cleanup of inactive conversations");
        } catch (Exception e) {
            log.error("Error during conversation cleanup", e);
        }
    }
}
