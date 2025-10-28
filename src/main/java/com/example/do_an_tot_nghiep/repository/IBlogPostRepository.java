package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface IBlogPostRepository extends JpaRepository<BlogPost, Integer> {
    /**
     * Lấy top 3 bài viết đã xuất bản
     */
    @Query("SELECT bp FROM BlogPost bp " +
            "LEFT JOIN FETCH bp.author e " +
            "WHERE bp.status = 'Published' " +
            "ORDER BY bp.publishedAt DESC")
    List<BlogPost> findTop3PublishedPosts();

    /**
     * Lấy tất cả bài viết đã xuất bản
     */
    @Query("SELECT bp FROM BlogPost bp " +
            "LEFT JOIN FETCH bp.author e " +
            "WHERE bp.status = 'Published' " +
            "ORDER BY bp.publishedAt DESC")
    List<BlogPost> findAllPublishedPosts();

    /**
     * Tìm bài viết theo từ khóa
     */
    @Query("SELECT bp FROM BlogPost bp " +
            "LEFT JOIN FETCH bp.author e " +
            "WHERE bp.status = 'Published' " +
            "AND (LOWER(bp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(bp.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY bp.publishedAt DESC")
    List<BlogPost> searchPublishedPosts(@Param("keyword") String keyword);
}

/**
 * BrandRepository - Quản lý thương hiệu (nếu cần)
 */

