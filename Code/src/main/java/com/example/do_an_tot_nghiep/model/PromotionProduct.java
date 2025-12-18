package com.example.do_an_tot_nghiep.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promotion_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private MedicalDevice device;
}
