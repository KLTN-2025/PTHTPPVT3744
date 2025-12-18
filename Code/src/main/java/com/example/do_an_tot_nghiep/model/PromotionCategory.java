package com.example.do_an_tot_nghiep.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promotion_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
