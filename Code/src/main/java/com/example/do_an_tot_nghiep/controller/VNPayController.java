package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.model.Order;
import com.example.do_an_tot_nghiep.repository.IOrderRepository;
import com.example.do_an_tot_nghiep.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/payment")
public class VNPayController {

    @Value("${TMN_CODE}")
    private String tmnCode;

    @Value("${HASH_SECRET}")
    private String hashSecret;

    @Value("${VNPAY_URL}")
    private String vnpayUrl;

    @Value("${RETURN_URL}")
    private String returnUrl;

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    /**
     * ✅ Tạo URL thanh toán VNPay - Dựa trên code demo chính thức
     */
    @GetMapping("/vnpay")
    public String createPayment(@RequestParam Integer orderId, HttpServletRequest request) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            // ✅ Số tiền (VNPay yêu cầu nhân 100)
            long amount = order.getTotalPrice().multiply(new java.math.BigDecimal(100)).longValue();

            // ✅ Lấy IP
            String vnp_IpAddr = getIpAddress(request);

            // ✅ Tạo thời gian
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());

            // ✅ Tạo params theo đúng chuẩn VNPay
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", tmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", order.getOrderCode());
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + order.getOrderCode());
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", returnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // ✅ Build hash data và query theo chuẩn VNPay
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(hashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = vnpayUrl + "?" + queryUrl;

            // Debug log
            System.out.println("=== VNPAY CREATE PAYMENT ===");
            System.out.println("TMN Code: " + tmnCode);
            System.out.println("Order Code: " + order.getOrderCode());
            System.out.println("Amount: " + amount);
            System.out.println("Hash Data: " + hashData.toString());
            System.out.println("Secure Hash: " + vnp_SecureHash);
            System.out.println("Payment URL: " + paymentUrl);

            return "redirect:" + paymentUrl;

        } catch (Exception e) {
            System.err.println("Error creating VNPay payment: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/checkout?error=payment_failed";
        }
    }

    /**
     * ✅ Xử lý callback từ VNPay (Return URL)
     */
    @GetMapping("/vnpay/return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        try {
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            // Build hash data theo chuẩn VNPay
            List<String> fieldNames = new ArrayList<>(fields.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String signValue = hmacSHA512(hashSecret, hashData.toString());

            System.out.println("=== VNPAY CALLBACK ===");
            System.out.println("Hash Data: " + hashData.toString());
            System.out.println("Calculated Hash: " + signValue);
            System.out.println("VNPay Hash: " + vnp_SecureHash);
            System.out.println("Match: " + signValue.equals(vnp_SecureHash));

            if (signValue.equals(vnp_SecureHash)) {
                String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
                String orderCode = request.getParameter("vnp_TxnRef");
                String transactionNo = request.getParameter("vnp_TransactionNo");

                if ("00".equals(vnp_ResponseCode)) {
                    // ✅ Thanh toán thành công
                    Order order = orderRepository.findByOrderCode(orderCode)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

                    order.setPaymentStatus(Order.PaymentStatus.PAID);
                    order.setTransactionId(transactionNo);
                    orderRepository.save(order);

                    return "redirect:/order-success?orderCode=" + orderCode;
                } else {
                    // ❌ Thanh toán thất bại
                    String errorMessage = getVNPayErrorMessage(vnp_ResponseCode);

                    Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
                    if (order != null) {
                        order.setPaymentStatus(Order.PaymentStatus.UNPAID);
                        order.setStatus(Order.OrderStatus.CANCELLED);
                        order.setCancelReason("Thanh toán VNPay thất bại: " + errorMessage);
                        order.setCancelledAt(java.time.LocalDateTime.now());
                        orderRepository.save(order);
                    }

                    return "redirect:/checkout?error=payment_failed&message=" +
                            URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString());
                }
            } else {
                // ❌ Chữ ký không hợp lệ
                return "redirect:/checkout?error=invalid_signature";
            }

        } catch (Exception e) {
            System.err.println("Error processing VNPay return: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/checkout?error=system_error";
        }
    }

    /**
     * ✅ Xử lý IPN từ VNPay
     */
    @GetMapping("/vnpay/ipn")
    @ResponseBody
    public Map<String, String> vnpayIPN(HttpServletRequest request) {
        try {
            System.out.println("=== VNPAY IPN RECEIVED ===");

            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            // Build hash data
            List<String> fieldNames = new ArrayList<>(fields.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String signValue = hmacSHA512(hashSecret, hashData.toString());

            System.out.println("IPN Calculated Hash: " + signValue);
            System.out.println("IPN VNPay Hash: " + vnp_SecureHash);
            System.out.println("IPN Match: " + signValue.equals(vnp_SecureHash));

            if (signValue.equals(vnp_SecureHash)) {
                String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
                String orderCode = request.getParameter("vnp_TxnRef");
                String transactionNo = request.getParameter("vnp_TransactionNo");
                String amount = request.getParameter("vnp_Amount");

                Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);

                if (orderOpt.isEmpty()) {
                    return Map.of("RspCode", "01", "Message", "Order not found");
                }

                Order order = orderOpt.get();

                long expectedAmount = order.getTotalPrice().multiply(new java.math.BigDecimal(100)).longValue();
                long receivedAmount = Long.parseLong(amount);

                if (expectedAmount != receivedAmount) {
                    return Map.of("RspCode", "04", "Message", "Invalid amount");
                }

                if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
                    return Map.of("RspCode", "02", "Message", "Order already confirmed");
                }

                if ("00".equals(vnp_ResponseCode)) {
                    order.setPaymentStatus(Order.PaymentStatus.PAID);
                    order.setTransactionId(transactionNo);
                    orderRepository.save(order);

                    System.out.println("IPN: Payment successful - " + orderCode);
                    return Map.of("RspCode", "00", "Message", "Success");
                } else {
                    order.setPaymentStatus(Order.PaymentStatus.UNPAID);
                    order.setStatus(Order.OrderStatus.CANCELLED);
                    order.setCancelReason("VNPay payment failed: " + vnp_ResponseCode);
                    order.setCancelledAt(java.time.LocalDateTime.now());
                    orderRepository.save(order);

                    return Map.of("RspCode", "00", "Message", "Confirmed");
                }
            } else {
                return Map.of("RspCode", "97", "Message", "Invalid signature");
            }

        } catch (Exception e) {
            System.err.println("IPN Error: " + e.getMessage());
            e.printStackTrace();
            return Map.of("RspCode", "99", "Message", "Unknown error");
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * ✅ HMAC SHA512 - Theo chuẩn VNPay
     */
    private String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }

            Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * ✅ Lấy IP address
     */
    private String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
            if ("0:0:0:0:0:0:0:1".equals(ipAdress) || "::1".equals(ipAdress)) {
                ipAdress = "127.0.0.1";
            }
        } catch (Exception e) {
            ipAdress = "127.0.0.1";
        }
        return ipAdress;
    }

    /**
     * Lấy thông báo lỗi từ VNPay response code
     */
    private String getVNPayErrorMessage(String responseCode) {
        return switch (responseCode) {
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09" -> "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
            case "10" -> "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11" -> "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "12" -> "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13" -> "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "24" -> "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51" -> "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65" -> "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75" -> "Ngân hàng thanh toán đang bảo trì.";
            case "79" -> "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định. Xin quý khách vui lòng thực hiện lại giao dịch";
            default -> "Thanh toán thất bại! Mã lỗi: " + responseCode;
        };
    }
}