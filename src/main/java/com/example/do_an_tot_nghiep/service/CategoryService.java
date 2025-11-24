package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.CategoryDTO;
import com.example.do_an_tot_nghiep.model.Category;
import com.example.do_an_tot_nghiep.repository.ICategoryRepository;
import com.example.do_an_tot_nghiep.service.ICategoryService;
import com.example.do_an_tot_nghiep.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService implements ICategoryService {

    private final ICategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;

    @Override
    public List<CategoryDTO> getAllCategories() {
        log.info("Getting all categories");
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoryDTO> getAllCategoriesPage(Pageable pageable) {
        log.info("Getting categories with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return categoryRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public List<CategoryDTO> getAllParentCategories() {
        log.info("Getting all parent categories");
        // SỬA: Thay findByParentIdIsNull() thành findByParentIsNull()
        return categoryRepository.findByParentIsNull().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoryDTO> searchCategories(String keyword, Pageable pageable) {
        log.info("Searching categories with keyword: {}", keyword);
        return categoryRepository.findByNameContainingIgnoreCase(keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<CategoryDTO> getCategoriesByStatus(Boolean isActive, Pageable pageable) {
        log.info("Getting categories by status: {}", isActive);
        return categoryRepository.findByIsActive(isActive, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public CategoryDTO getCategoryById(Integer id) {
        log.info("Getting category by id: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        return convertToDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto, MultipartFile imageFile) {
        log.info("Creating new category: {}", dto.getName());

        // Kiểm tra tên danh mục đã tồn tại
        if (isNameExists(dto.getName(), null)) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }

        Category category = new Category();
        category.setName(dto.getName());

        // Tự động tạo slug nếu không có
        String slug = (dto.getSlug() == null || dto.getSlug().isEmpty())
                ? generateSlug(dto.getName())
                : dto.getSlug();
        category.setSlug(slug);

        // Kiểm tra slug đã tồn tại
        if (isSlugExists(slug, null)) {
            throw new RuntimeException("Slug đã tồn tại");
        }

        category.setDescription(dto.getDescription());
        category.setMetaTitle(dto.getMetaTitle());
        category.setMetaDescription(dto.getMetaDescription());
        category.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        category.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        // Set parent category nếu có
        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha"));
            category.setParent(parent);
        }

        // Upload image nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = fileUploadService.uploadFile(imageFile, "categories");
                category.setImageUrl(imageUrl);
                log.info("Uploaded category image: {}", imageUrl);
            } catch (Exception e) {
                log.error("Error uploading image", e);
                throw new RuntimeException("Lỗi khi upload hình ảnh: " + e.getMessage());
            }
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Created category successfully with id: {}", savedCategory.getCategoryId());
        return convertToDTO(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Integer id, CategoryDTO dto, MultipartFile imageFile) {
        log.info("Updating category id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        // Kiểm tra tên danh mục đã tồn tại (trừ chính nó)
        if (isNameExists(dto.getName(), id)) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }

        category.setName(dto.getName());

        // Cập nhật slug
        String slug = (dto.getSlug() == null || dto.getSlug().isEmpty())
                ? generateSlug(dto.getName())
                : dto.getSlug();

        if (isSlugExists(slug, id)) {
            throw new RuntimeException("Slug đã tồn tại");
        }
        category.setSlug(slug);

        category.setDescription(dto.getDescription());
        category.setMetaTitle(dto.getMetaTitle());
        category.setMetaDescription(dto.getMetaDescription());
        category.setDisplayOrder(dto.getDisplayOrder());
        category.setIsActive(dto.getIsActive());

        // Cập nhật parent category
        if (dto.getParentId() != null) {
            // Không cho phép set parent là chính nó
            if (dto.getParentId().equals(id)) {
                throw new RuntimeException("Không thể chọn danh mục cha là chính nó");
            }
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        // Upload image mới nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Xóa ảnh cũ nếu có
                if (category.getImageUrl() != null) {
                    fileUploadService.deleteFile(category.getImageUrl());
                }
                String imageUrl = fileUploadService.uploadFile(imageFile, "categories");
                category.setImageUrl(imageUrl);
                log.info("Updated category image: {}", imageUrl);
            } catch (Exception e) {
                log.error("Error uploading image", e);
                throw new RuntimeException("Lỗi khi upload hình ảnh: " + e.getMessage());
            }
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Updated category successfully with id: {}", id);
        return convertToDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        log.info("Deleting category id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        // Kiểm tra có danh mục con không
        if (categoryRepository.existsByParentId(id)) {
            throw new RuntimeException("Không thể xóa danh mục có danh mục con. Vui lòng xóa danh mục con trước.");
        }

        // Kiểm tra có sản phẩm nào đang sử dụng không
        if (categoryRepository.hasProducts(id)) {
            throw new RuntimeException("Không thể xóa danh mục đang có sản phẩm. Vui lòng xóa hoặc chuyển sản phẩm sang danh mục khác.");
        }

        // Xóa ảnh nếu có
        if (category.getImageUrl() != null) {
            try {
                fileUploadService.deleteFile(category.getImageUrl());
                log.info("Deleted category image: {}", category.getImageUrl());
            } catch (Exception e) {
                log.error("Error deleting image", e);
            }
        }

        categoryRepository.delete(category);
        log.info("Deleted category successfully with id: {}", id);
    }

    @Override
    @Transactional
    public int deleteCategories(List<Integer> ids) {
        log.info("Deleting multiple categories: {}", ids);
        int deletedCount = 0;
        StringBuilder errors = new StringBuilder();

        for (Integer id : ids) {
            try {
                deleteCategory(id);
                deletedCount++;
            } catch (Exception e) {
                errors.append("ID ").append(id).append(": ").append(e.getMessage()).append("\n");
                log.error("Failed to delete category id: {}", id, e);
            }
        }

        if (errors.length() > 0 && deletedCount == 0) {
            throw new RuntimeException("Không thể xóa danh mục:\n" + errors.toString());
        }

        log.info("Deleted {} categories successfully", deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional
    public void toggleStatus(Integer id) {
        log.info("Toggling status for category id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        category.setIsActive(!category.getIsActive());
        categoryRepository.save(category);

        log.info("Toggled category status to: {}", category.getIsActive());
    }

    @Override
    public boolean isSlugExists(String slug, Integer excludeId) {
        Category existing = categoryRepository.findBySlug(slug);
        if (existing == null) {
            return false;
        }
        return excludeId == null || !existing.getCategoryId().equals(excludeId);
    }

    @Override
    public boolean isNameExists(String name, Integer excludeId) {
        boolean exists = categoryRepository.existsByName(name);
        if (!exists) {
            return false;
        }

        // Nếu có excludeId, kiểm tra xem có phải chính nó không
        if (excludeId != null) {
            Category existing = categoryRepository.findById(excludeId).orElse(null);
            return existing == null || !existing.getName().equals(name);
        }

        return true;
    }

    @Override
    public long countProducts(Integer categoryId) {
        return categoryRepository.countProductsByCategory(categoryId);
    }

    @Override
    public List<CategoryDTO> getSubcategories(Integer parentId) {
        log.info("Getting subcategories for parent id: {}", parentId);
        return categoryRepository.findByParentIdOrderByDisplayOrder(parentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tạo slug từ tên
     */
    private String generateSlug(String name) {
        String slug = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("đ", "d")
                .replaceAll("Đ", "d")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        // Thêm số nếu slug đã tồn tại
        String originalSlug = slug;
        int counter = 1;
        while (categoryRepository.existsBySlug(slug)) {
            slug = originalSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    /**
     * Convert Entity sang DTO
     */
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setMetaTitle(category.getMetaTitle());
        dto.setMetaDescription(category.getMetaDescription());
        dto.setDisplayOrder(category.getDisplayOrder());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt());

        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getCategoryId());
            dto.setParentName(category.getParent().getName());
        }

        return dto;
    }
}