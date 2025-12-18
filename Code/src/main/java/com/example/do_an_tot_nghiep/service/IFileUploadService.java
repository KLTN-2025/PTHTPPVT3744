package com.example.do_an_tot_nghiep.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interface định nghĩa các phương thức service cho File Upload
 */
public interface IFileUploadService {

    /**
     * Upload file và trả về URL
     * @param file file cần upload
     * @param folder thư mục đích (vd: "categories", "products")
     * @return URL của file đã upload
     * @throws IOException nếu có lỗi upload
     */
    String uploadFile(MultipartFile file, String folder) throws IOException;

    /**
     * Xóa file theo URL
     * @param fileUrl URL của file cần xóa
     * @throws IOException nếu có lỗi xóa file
     */
    void deleteFile(String fileUrl) throws IOException;

    /**
     * Kiểm tra file có tồn tại không
     * @param fileUrl URL của file
     * @return true nếu file tồn tại
     */
    boolean fileExists(String fileUrl);

    /**
     * Lấy kích thước file
     * @param fileUrl URL của file
     * @return kích thước file (bytes)
     * @throws IOException nếu có lỗi đọc file
     */
    long getFileSize(String fileUrl) throws IOException;

    /**
     * Validate file upload
     * @param file file cần validate
     * @param maxSize kích thước tối đa (bytes)
     * @param allowedTypes các loại file cho phép
     * @throws IOException nếu file không hợp lệ
     */
    void validateFile(MultipartFile file, long maxSize, String... allowedTypes) throws IOException;
}