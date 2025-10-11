package com.example.do_an_tot_nghiep.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private PointType type = PointType.EARNED;

    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum PointType {
        EARNED("Earned"),
        REDEEMED("Redeemed"),
        EXPIRED("Expired"),
        BONUS("Bonus"),
        REFUND("Refund");

        private String value;

        PointType(String value) {
            this.value = value;
        }
    }
}