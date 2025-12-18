package com.example.do_an_tot_nghiep.dto;


public interface OrderStatsDTO {
    Long getTotalOrders();
    Long getPendingOrders();
    Long getCompletedOrders();
    Long getCancelledOrders();
    Long getReturnedOrders();
}

