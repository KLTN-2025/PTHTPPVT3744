package com.example.do_an_tot_nghiep.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.do_an_tot_nghiep.service.IFileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService implements IFileUploadService {

    private final Cloudinary cloudinary;

    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    private static final long MAX_FILE_SIZE = 5242880; // 5MB

    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        log.info("Uploading file to Cloudinary: {} in folder: {}", file.getOriginalFilename(), folder);

        if (file.isEmpty()) {
            throw new IOException("File rỗng");
        }

        // Validate file
        validateFile(file, MAX_FILE_SIZE, ALLOWED_IMAGE_TYPES);

        try {
            // Tạo tên file unique
            String publicId = folder + "/" + UUID.randomUUID().toString();

            // Upload lên Cloudinary với optimization
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", folder,
                            "resource_type", "auto",
                            "overwrite", true,
                            // Tối ưu hóa ảnh
                            "quality", "auto:good",           // Tự động nén chất lượng
                            "fetch_format", "auto",           // Tự động chọn format tốt nhất
                            "width", 1200,                    // Giới hạn chiều rộng tối đa
                            "height", 1200,                   // Giới hạn chiều cao tối đa
                            "crop", "limit",                  // Không crop, chỉ resize nếu quá lớn
                            "flags", "progressive"            // Progressive loading
                    ));

            // Lấy URL của ảnh đã upload
            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("File uploaded successfully to Cloudinary: {}", imageUrl);

            return imageUrl;

        } catch (Exception e) {
            log.error("Error uploading file to Cloudinary", e);
            throw new IOException("Lỗi khi upload file lên Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("File URL is null or empty, skipping delete");
            return;
        }

        try {
            // Extract public_id from Cloudinary URL
            String publicId = extractPublicIdFromUrl(fileUrl);

            if (publicId != null && !publicId.isEmpty()) {
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("File deleted from Cloudinary: {} - Result: {}", publicId, result.get("result"));
            } else {
                log.warn("Could not extract public_id from URL: {}", fileUrl);
            }

        } catch (Exception e) {
            log.error("Error deleting file from Cloudinary", e);
            throw new IOException("Lỗi khi xóa file trên Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        try {
            String publicId = extractPublicIdFromUrl(fileUrl);
            if (publicId != null) {
                Map result = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
                return result != null && result.containsKey("public_id");
            }
        } catch (Exception e) {
            log.debug("File does not exist on Cloudinary: {}", fileUrl);
        }

        return false;
    }

    @Override
    public long getFileSize(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return 0;
        }

        try {
            String publicId = extractPublicIdFromUrl(fileUrl);
            if (publicId != null) {
                Map result = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
                Object bytes = result.get("bytes");
                return bytes != null ? Long.parseLong(bytes.toString()) : 0;
            }
        } catch (Exception e) {
            log.error("Error getting file size from Cloudinary", e);
            throw new IOException("Lỗi khi lấy kích thước file: " + e.getMessage());
        }

        return 0;
    }

    @Override
    public void validateFile(MultipartFile file, long maxSize, String... allowedTypes) throws IOException {
        // Kiểm tra file rỗng
        if (file.isEmpty()) {
            throw new IOException("File rỗng");
        }

        // Kiểm tra kích thước
        if (file.getSize() > maxSize) {
            throw new IOException(String.format("Kích thước file vượt quá giới hạn cho phép (%d MB)",
                    maxSize / (1024 * 1024)));
        }

        // Kiểm tra định dạng
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IOException("Không xác định được loại file");
        }

        boolean isAllowed = Arrays.asList(allowedTypes).contains(contentType);
        if (!isAllowed) {
            throw new IOException("Định dạng file không được hỗ trợ. Chỉ chấp nhận: " +
                    String.join(", ", allowedTypes));
        }

        // Kiểm tra extension
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new IOException("Tên file không hợp lệ");
        }

        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        boolean hasValidExtension = Arrays.asList(allowedExtensions).contains(extension);

        if (!hasValidExtension) {
            throw new IOException("Phần mở rộng file không hợp lệ. Chỉ chấp nhận: " +
                    String.join(", ", allowedExtensions));
        }

        log.debug("File validation passed for: {}", filename);
    }

    /**
     * Extract public_id from Cloudinary URL
     * URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{folder}/{public_id}.{format}
     */
    private String extractPublicIdFromUrl(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.contains("cloudinary.com")) {
                return null;
            }

            // Extract the part after "/upload/"
            String[] parts = fileUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            // Get everything after version number
            String afterUpload = parts[1];
            String[] segments = afterUpload.split("/");

            // Skip version (v1234567890)
            int startIndex = segments[0].startsWith("v") ? 1 : 0;

            // Reconstruct public_id (folder/filename without extension)
            StringBuilder publicId = new StringBuilder();
            for (int i = startIndex; i < segments.length; i++) {
                if (i > startIndex) {
                    publicId.append("/");
                }
                publicId.append(segments[i]);
            }

            // Remove file extension
            String result = publicId.toString();
            int lastDot = result.lastIndexOf(".");
            if (lastDot > 0) {
                result = result.substring(0, lastDot);
            }

            log.debug("Extracted public_id: {} from URL: {}", result, fileUrl);
            return result;

        } catch (Exception e) {
            log.error("Error extracting public_id from URL: {}", fileUrl, e);
            return null;
        }
    }
}