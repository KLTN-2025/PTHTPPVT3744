package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.ChatIntent;
import org.springframework.stereotype.Service;

@Service
public class IntentDetector {

    public ChatIntent detect(String message) {
        String msg = message.toLowerCase();

        if (msg.contains("thanh toán") || msg.contains("vnpay")) {
            return ChatIntent.PAYMENT;
        }

        if (msg.contains("đơn hàng") || msg.contains("theo dõi")) {
            return ChatIntent.TRACK_ORDER;
        }

        if (msg.contains("mua") || msg.contains("đặt") || msg.contains("chốt")) {
            return ChatIntent.CREATE_ORDER;
        }

        return ChatIntent.CONSULT;
    }
}

