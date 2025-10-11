package com.example.do_an_tot_nghiep.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "banner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id")
    private Integer bannerId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "link_url", length = 255)
    private String linkUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "position")
    private BannerPosition position = BannerPosition.Home_Slider;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "click_count")
    private Integer clickCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum BannerPosition {
        Home_Slider, Sidebar, Top, Bottom,Category
    }
}
