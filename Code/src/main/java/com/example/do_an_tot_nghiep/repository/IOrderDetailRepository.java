package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Order;
import com.example.do_an_tot_nghiep.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByOrder(Order order);
}
