package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.CategoryDTO;
import com.example.do_an_tot_nghiep.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ICategoryService {

    /**
     * Lấy tất cả danh mục (không phân trang) - dùng cho dropdown
     * @return List danh sách danh mục
     */
    List<CategoryDTO> getAllCategories();

    /**
     * Lấy tất cả danh mục có phân trang
     * @param pageable thông tin phân trang
     * @return Page danh sách danh mục
     */
    Page<CategoryDTO> getAllCategoriesPage(Pageable pageable);

    /**
     * Lấy danh mục cha (parentId = null)
     * @return List danh sách danh mục gốc
     */
    List<CategoryDTO> getAllParentCategories();

    /**
     * Tìm kiếm danh mục theo từ khóa
     * @param keyword từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page danh sách danh mục
     */
    Page<CategoryDTO> searchCategories(String keyword, Pageable pageable);

    /**
     * Lọc danh mục theo trạng thái
     * @param isActive trạng thái hoạt động
     * @param pageable thông tin phân trang
     * @return Page danh sách danh mục
     */
    Page<CategoryDTO> getCategoriesByStatus(Boolean isActive, Pageable pageable);

    /**
     * Lấy chi tiết danh mục theo ID
     * @param id ID danh mục
     * @return CategoryDTO thông tin danh mục
     * @throws RuntimeException nếu không tìm thấy
     */
    CategoryDTO getCategoryById(Integer id);

    /**
     * Tạo danh mục mới
     * @param dto thông tin danh mục
     * @param imageFile file hình ảnh (có thể null)
     * @return CategoryDTO danh mục đã tạo
     * @throws RuntimeException nếu có lỗi
     */
    CategoryDTO createCategory(CategoryDTO dto, MultipartFile imageFile);

    /**
     * Cập nhật danh mục
     * @param id ID danh mục
     * @param dto thông tin cập nhật
     * @param imageFile file hình ảnh mới (có thể null)
     * @return CategoryDTO danh mục đã cập nhật
     * @throws RuntimeException nếu có lỗi
     */
    CategoryDTO updateCategory(Integer id, CategoryDTO dto, MultipartFile imageFile);

    /**
     * Xóa danh mục đơn
     * @param id ID danh mục cần xóa
     * @throws RuntimeException nếu có ràng buộc
     */
    void deleteCategory(Integer id);

    /**
     * Xóa nhiều danh mục
     * @param ids danh sách ID cần xóa
     * @return số lượng danh mục đã xóa thành công
     * @throws RuntimeException nếu không thể xóa tất cả
     */
    int deleteCategories(List<Integer> ids);

    /**
     * Bật/tắt trạng thái danh mục
     * @param id ID danh mục
     * @throws RuntimeException nếu không tìm thấy
     */
    void toggleStatus(Integer id);

    /**
     * Kiểm tra slug có tồn tại không
     * @param slug slug cần kiểm tra
     * @param excludeId ID cần loại trừ (khi update)
     * @return true nếu tồn tại
     */
    boolean isSlugExists(String slug, Integer excludeId);

    /**
     * Kiểm tra tên có tồn tại không
     * @param name tên cần kiểm tra
     * @param excludeId ID cần loại trừ (khi update)
     * @return true nếu tồn tại
     */
    boolean isNameExists(String name, Integer excludeId);

    /**
     * Đếm số sản phẩm trong danh mục
     * @param categoryId ID danh mục
     * @return số lượng sản phẩm
     */
    long countProducts(Integer categoryId);

    /**
     * Lấy danh mục con của một danh mục
     * @param parentId ID danh mục cha
     * @return List danh sách danh mục con
     */
    List<CategoryDTO> getSubcategories(Integer parentId);
}
