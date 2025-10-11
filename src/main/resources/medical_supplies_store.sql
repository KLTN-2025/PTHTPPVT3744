-- =============================================
--  DATABASE: medical_supplies_store (ENHANCED VERSION)
--  Created: 2025-10-10
--  Description: Complete database for medical supplies e-commerce
--  Enhanced: Separate Customer and Employee tables
-- =============================================

CREATE DATABASE IF NOT EXISTS medical_supplies_store;
USE medical_supplies_store;



-- ===============================
-- CREATE TABLES (ORDERED)
-- ===============================

CREATE TABLE role (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE employee (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(50) UNIQUE NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255),
    avatar_url VARCHAR(255),
    role_id INT NOT NULL,
    date_of_birth DATE,
    gender ENUM('Male','Female','Other'),
    citizen_id VARCHAR(20) UNIQUE COMMENT 'CMND/CCCD',
    position VARCHAR(100) COMMENT 'Chức vụ cụ thể',
    department VARCHAR(100) COMMENT 'Phòng ban',
    hire_date DATE NOT NULL,
    salary DECIMAL(15,2) COMMENT 'Lương cơ bản',
    status ENUM('Active','On Leave','Resigned','Terminated') DEFAULT 'Active',
    last_login DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by INT COMMENT 'ID nhân viên tạo',
    FOREIGN KEY (role_id) REFERENCES role(role_id),
    FOREIGN KEY (created_by) REFERENCES employee(employee_id),
    INDEX idx_employee_code (employee_code),
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(50) UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(255),
    avatar_url VARCHAR(255),
    date_of_birth DATE,
    gender ENUM('Male','Female','Other'),
    customer_tier ENUM('Bronze','Silver','Gold','Platinum') DEFAULT 'Bronze',
    loyalty_points INT DEFAULT 0,
    total_spent DECIMAL(15,2) DEFAULT 0,
    total_orders INT DEFAULT 0,
    status ENUM('Active','Inactive','Blocked') DEFAULT 'Active',
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    last_login DATETIME,
    last_order_date DATETIME,
    referral_code VARCHAR(20) UNIQUE COMMENT 'Mã giới thiệu của khách hàng',
    referred_by INT COMMENT 'ID khách hàng giới thiệu',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (referred_by) REFERENCES customer(customer_id),
    INDEX idx_customer_code (customer_code),
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_status (status),
    INDEX idx_tier (customer_tier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE loyalty_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    points INT NOT NULL COMMENT 'Số điểm (dương: cộng, âm: trừ)',
    type ENUM('Earned','Redeemed','Expired','Bonus','Refund') DEFAULT 'Earned',
    reference_id INT COMMENT 'order_id hoặc promotion_id',
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    INDEX idx_customer (customer_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id INT NULL,
    slug VARCHAR(100) UNIQUE,
    description TEXT,
    image_url VARCHAR(255),
    meta_title VARCHAR(255),
    meta_description TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES category(category_id) ON DELETE SET NULL,
    INDEX idx_slug (slug),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE brand (
    brand_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE,
    country VARCHAR(100),
    logo_url VARCHAR(255),
    description TEXT,
    website VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE supplier (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    address VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(100),
    tax_code VARCHAR(50),
    bank_account VARCHAR(100),
    bank_name VARCHAR(100),
    description TEXT,
    status ENUM('Active','Inactive') DEFAULT 'Active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE medical_device (
    device_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    sku VARCHAR(100) UNIQUE,
    category_id INT,
    brand_id INT,
    supplier_id INT,
    description TEXT,
    specification TEXT,
    usage_instruction TEXT,
    price DECIMAL(15,2) NOT NULL,
    original_price DECIMAL(15,2),
    discount_percent INT DEFAULT 0,
    stock_quantity INT DEFAULT 0,
    min_stock_level INT DEFAULT 10,
    unit VARCHAR(50) DEFAULT 'Cái',
    weight DECIMAL(10,2) COMMENT 'Khối lượng (kg)',
    dimensions VARCHAR(100) COMMENT 'Kích thước (cm)',
    warranty_period INT COMMENT 'Thời gian bảo hành (tháng)',
    status ENUM('Còn hàng','Hết hàng','Ngừng bán') DEFAULT 'Còn hàng',
    is_featured BOOLEAN DEFAULT FALSE,
    is_new BOOLEAN DEFAULT FALSE,
    view_count INT DEFAULT 0,
    sold_count INT DEFAULT 0,
    image_url VARCHAR(255),
    gallery_urls TEXT,
    meta_keywords TEXT,
    meta_description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE SET NULL,
    FOREIGN KEY (brand_id) REFERENCES brand(brand_id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id) ON DELETE SET NULL,
    INDEX idx_name (name),
    INDEX idx_slug (slug),
    INDEX idx_sku (sku),
    INDEX idx_category (category_id),
    INDEX idx_brand (brand_id),
    INDEX idx_status (status),
    INDEX idx_price (price),
    INDEX idx_featured (is_featured),
    FULLTEXT idx_search (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stock_import (
    import_id INT AUTO_INCREMENT PRIMARY KEY,
    import_code VARCHAR(50) UNIQUE,
    supplier_id INT,
    import_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(15,2) NOT NULL,
    note TEXT,
    status ENUM('Pending','Completed','Cancelled') DEFAULT 'Pending',
    created_by INT COMMENT 'employee_id',
    approved_by INT COMMENT 'employee_id',
    approved_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id),
    FOREIGN KEY (created_by) REFERENCES employee(employee_id),
    FOREIGN KEY (approved_by) REFERENCES employee(employee_id),
    INDEX idx_import_code (import_code),
    INDEX idx_import_date (import_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stock_import_detail (
    import_detail_id INT AUTO_INCREMENT PRIMARY KEY,
    import_id INT,
    device_id VARCHAR(50),
    quantity INT NOT NULL,
    import_price DECIMAL(15,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    expiry_date DATE,
    batch_number VARCHAR(100),
    FOREIGN KEY (import_id) REFERENCES stock_import(import_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES medical_device(device_id),
    INDEX idx_import (import_id),
    INDEX idx_device (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE promotion (
    promotion_id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255),
    description TEXT,
    discount_type ENUM('Percent','Fixed','FreeShip') DEFAULT 'Percent',
    discount_value DECIMAL(15,2) NOT NULL,
    min_order_amount DECIMAL(15,2) DEFAULT 0,
    max_discount_amount DECIMAL(15,2),
    usage_limit INT COMMENT 'Giới hạn số lần sử dụng tổng',
    used_count INT DEFAULT 0,
    usage_per_customer INT DEFAULT 1 COMMENT 'Số lần mỗi khách hàng được dùng',
    customer_tier ENUM('All','Bronze','Silver','Gold','Platinum') DEFAULT 'All',
    start_date DATETIME,
    end_date DATETIME,
    applicable_to ENUM('All','Category','Product') DEFAULT 'All',
    is_active BOOLEAN DEFAULT TRUE,
    created_by INT COMMENT 'employee_id',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES employee(employee_id),
    INDEX idx_code (code),
    INDEX idx_dates (start_date, end_date),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE promotion_category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    promotion_id INT,
    category_id INT,
    FOREIGN KEY (promotion_id) REFERENCES promotion(promotion_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE,
    UNIQUE KEY unique_promo_cat (promotion_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE promotion_product (
    id INT AUTO_INCREMENT PRIMARY KEY,
    promotion_id INT,
    device_id VARCHAR(50),
    FOREIGN KEY (promotion_id) REFERENCES promotion(promotion_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES medical_device(device_id) ON DELETE CASCADE,
    UNIQUE KEY unique_promo_prod (promotion_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE customer_address (
    address_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    receiver_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    ward VARCHAR(100),
    district VARCHAR(100),
    province VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    INDEX idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `order` (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(50) UNIQUE NOT NULL,
    customer_id INT,
    address_id INT COMMENT 'Địa chỉ giao hàng',
    receiver_name VARCHAR(255) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(500) NOT NULL,
    subtotal DECIMAL(15,2) NOT NULL COMMENT 'Tổng tiền hàng',
    shipping_fee DECIMAL(10,2) DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    loyalty_points_used INT DEFAULT 0,
    loyalty_discount DECIMAL(10,2) DEFAULT 0,
    total_price DECIMAL(15,2) NOT NULL COMMENT 'Tổng thanh toán',
    promotion_id INT,
    payment_method ENUM('COD','VNPay','Momo','Bank Transfer','Wallet') DEFAULT 'COD',
    payment_status ENUM('Chưa thanh toán','Đã thanh toán','Hoàn tiền') DEFAULT 'Chưa thanh toán',
    transaction_id VARCHAR(100),
    status ENUM('Chờ xác nhận','Đã xác nhận','Đang chuẩn bị','Đang giao','Hoàn thành','Đã hủy','Trả hàng') DEFAULT 'Chờ xác nhận',
    note TEXT COMMENT 'Ghi chú từ khách hàng',
    internal_note TEXT COMMENT 'Ghi chú nội bộ',
    cancel_reason TEXT,
    assigned_to INT COMMENT 'employee_id xử lý đơn',
    confirmed_by INT COMMENT 'employee_id xác nhận',
    confirmed_at DATETIME,
    prepared_at DATETIME COMMENT 'Thời gian chuẩn bị xong',
    shipped_at DATETIME,
    completed_at DATETIME,
    cancelled_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (promotion_id) REFERENCES promotion(promotion_id),
    FOREIGN KEY (assigned_to) REFERENCES employee(employee_id),
    FOREIGN KEY (confirmed_by) REFERENCES employee(employee_id),
    INDEX idx_order_code (order_code),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_payment_status (payment_status),
    INDEX idx_created (created_at),
    INDEX idx_assigned (assigned_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_detail (
    order_detail_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    device_id VARCHAR(50),
    device_name VARCHAR(255) NOT NULL,
    device_image VARCHAR(255),
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES medical_device(device_id),
    INDEX idx_order (order_id),
    INDEX idx_device (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE promotion_usage (
    usage_id INT AUTO_INCREMENT PRIMARY KEY,
    promotion_id INT,
    customer_id INT,
    order_id INT,
    discount_amount DECIMAL(10,2),
    used_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (promotion_id) REFERENCES promotion(promotion_id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES `order`(order_id),
    INDEX idx_promotion (promotion_id),
    INDEX idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_status_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    note TEXT,
    changed_by INT COMMENT 'employee_id',
    changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES employee(employee_id),
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cart (
    cart_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    device_id VARCHAR(50),
    quantity INT DEFAULT 1,
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES medical_device(device_id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_item (customer_id, device_id),
    INDEX idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wishlist (
    wishlist_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    device_id VARCHAR(50),
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES medical_device(device_id) ON DELETE CASCADE,
    UNIQUE KEY unique_wishlist (customer_id, device_id),
    INDEX idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE review (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    device_id VARCHAR(50),
    order_id INT COMMENT 'Đánh giá từ đơn hàng nào',
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    images TEXT COMMENT 'JSON array of image URLs',
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    admin_reply TEXT,
    replied_by INT COMMENT 'employee_id',
    replied_at DATETIME,
    status ENUM('Pending','Approved','Rejected') DEFAULT 'Pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES medical_device(device_id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES `order`(order_id),
    FOREIGN KEY (replied_by) REFERENCES employee(employee_id),
    INDEX idx_device (device_id),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notification (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    target_type ENUM('Customer','Employee') NOT NULL,
    customer_id INT,
    employee_id INT,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    type ENUM('Order','Promotion','System','Review','Stock','Task') DEFAULT 'System',
    reference_id INT COMMENT 'ID liên quan (order_id, promotion_id...)',
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id) ON DELETE CASCADE,
    INDEX idx_customer (customer_id),
    INDEX idx_employee (employee_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE banner (
    banner_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    image_url VARCHAR(255) NOT NULL,
    link_url VARCHAR(255),
    position ENUM('Home Slider','Sidebar','Top','Bottom','Category') DEFAULT 'Home Slider',
    display_order INT DEFAULT 0,
    start_date DATETIME,
    end_date DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    click_count INT DEFAULT 0,
    created_by INT COMMENT 'employee_id',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES employee(employee_id),
    INDEX idx_position (position),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_post (
    post_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    content TEXT,
    excerpt TEXT,
    featured_image VARCHAR(255),
    author_id INT COMMENT 'employee_id',
    category_id INT,
    view_count INT DEFAULT 0,
    status ENUM('Draft','Published','Archived') DEFAULT 'Draft',
    published_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES employee(employee_id),
    INDEX idx_slug (slug),
    INDEX idx_status (status),
    INDEX idx_published (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE faq (
    faq_id INT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(500) NOT NULL,
    answer TEXT NOT NULL,
    category VARCHAR(100),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_by INT COMMENT 'employee_id',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES employee(employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE contact_message (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT COMMENT 'Nếu là khách hàng đã đăng ký',
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    subject VARCHAR(255),
    message TEXT NOT NULL,
    status ENUM('New','Processing','Resolved','Closed') DEFAULT 'New',
    assigned_to INT COMMENT 'employee_id',
    replied_by INT COMMENT 'employee_id',
    reply_content TEXT,
    replied_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (assigned_to) REFERENCES employee(employee_id),
    FOREIGN KEY (replied_by) REFERENCES employee(employee_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE system_config (
    config_id INT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    description VARCHAR(255),
    updated_by INT COMMENT 'employee_id',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES employee(employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE employee_schedule (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    work_date DATE NOT NULL,
    shift ENUM('Morning','Afternoon','Evening','Full Day') NOT NULL,
    start_time TIME,
    end_time TIME,
    status ENUM('Scheduled','Completed','Absent','Late') DEFAULT 'Scheduled',
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id) ON DELETE CASCADE,
    INDEX idx_employee (employee_id),
    INDEX idx_work_date (work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    check_in DATETIME,
    check_out DATETIME,
    work_hours DECIMAL(4,2) COMMENT 'Số giờ làm việc',
    overtime_hours DECIMAL(4,2) DEFAULT 0,
    status ENUM('On Time','Late','Early Leave','Absent') DEFAULT 'On Time',
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id) ON DELETE CASCADE,
    INDEX idx_employee (employee_id),
    INDEX idx_date (check_in)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===============================
-- BẢNG QUẢN LÝ VAI TRÒ
-- ===============================


INSERT INTO role (role_name, description) VALUES
('Admin', 'Toàn quyền quản lý hệ thống'),
('Manager', 'Quản lý cửa hàng, kho hàng'),
('Staff', 'Nhân viên bán hàng, xử lý đơn hàng'),
('Warehouse', 'Nhân viên kho'),
('Customer', 'Khách hàng mua hàng');

-- ===============================
-- BẢNG NHÂN VIÊN (EMPLOYEE)
-- ===============================


-- ===============================
-- BẢNG KHÁCH HÀNG (CUSTOMER)
-- ===============================


-- ===============================
-- BẢNG LỊCH SỬ ĐIỂM THƯỞNG
-- ===============================


-- ===============================
-- BẢNG DANH MỤC SẢN PHẨM
-- ===============================


-- ===============================
-- BẢNG THƯƠNG HIỆU
-- ===============================


-- ===============================
-- BẢNG NHÀ CUNG CẤP
-- ===============================


-- ===============================
-- BẢNG SẢN PHẨM / THIẾT BỊ Y TẾ
-- ===============================


-- ===============================
-- BẢNG NHẬP KHO
-- ===============================




-- Trigger tính tổng giá nhập kho
DELIMITER $$
CREATE TRIGGER trg_stock_import_detail_total
BEFORE INSERT ON stock_import_detail
FOR EACH ROW
BEGIN
    SET NEW.total_price = NEW.quantity * NEW.import_price;
END$$
DELIMITER ;

-- ===============================
-- BẢNG KHUYẾN MÃI / MÃ GIẢM GIÁ
-- ===============================


-- Bảng lịch sử sử dụng mã khuyến mãi


-- Bảng áp dụng khuyến mãi cho danh mục


-- Bảng áp dụng khuyến mãi cho sản phẩm


-- ===============================
-- BẢNG ĐỊA CHỈ GIAO HÀNG
-- ===============================


-- ===============================
-- BẢNG ĐƠN HÀNG
-- ===============================


-- ===============================
-- BẢNG CHI TIẾT ĐƠN HÀNG
-- ===============================


-- Trigger tự động tính total_price cho order_detail
DELIMITER $$
CREATE TRIGGER trg_order_detail_before_insert
BEFORE INSERT ON order_detail
FOR EACH ROW
BEGIN
    SET NEW.total_price = NEW.quantity * NEW.unit_price;
END$$

CREATE TRIGGER trg_order_detail_before_update
BEFORE UPDATE ON order_detail
FOR EACH ROW
BEGIN
    SET NEW.total_price = NEW.quantity * NEW.unit_price;
END$$
DELIMITER ;

-- Trigger cập nhật thông tin khách hàng khi hoàn thành đơn hàng
DELIMITER $$
CREATE TRIGGER trg_order_completed
AFTER UPDATE ON `order`
FOR EACH ROW
BEGIN
    IF NEW.status = 'Hoàn thành' AND OLD.status != 'Hoàn thành' THEN
        -- Cập nhật tổng chi tiêu và số đơn hàng
        UPDATE customer 
        SET total_spent = total_spent + NEW.total_price,
            total_orders = total_orders + 1,
            last_order_date = NEW.completed_at
        WHERE customer_id = NEW.customer_id;
        
        -- Thêm điểm thưởng (1 điểm cho mỗi 10,000 VNĐ)
        INSERT INTO loyalty_history (customer_id, points, type, reference_id, description)
        VALUES (NEW.customer_id, FLOOR(NEW.total_price / 10000), 'Earned', NEW.order_id, 
                CONCAT('Tích điểm từ đơn hàng #', NEW.order_code));
        
        -- Cập nhật điểm thưởng
        UPDATE customer 
        SET loyalty_points = loyalty_points + FLOOR(NEW.total_price / 10000)
        WHERE customer_id = NEW.customer_id;
    END IF;
END$$
DELIMITER ;

-- ===============================
-- BẢNG LỊCH SỬ TRẠNG THÁI ĐơN HÀNG
-- ===============================


-- ===============================
-- BẢNG GIỎ HÀNG
-- ===============================


-- ===============================
-- BẢNG YÊU THÍCH / WISHLIST
-- ===============================


-- ===============================
-- BẢNG ĐÁNH GIÁ SẢN PHẨM
-- ===============================


-- ===============================
-- BẢNG THÔNG BÁO
-- ===============================


-- ===============================
-- BẢNG BANNER QUẢNG CÁO
-- ===============================


-- ===============================
-- BẢNG TIN TỨC / BLOG
-- ===============================


-- ===============================
-- BẢNG CÂU HỎI THƯỜNG GẶP (FAQ)
-- ===============================


-- ===============================
-- BẢNG LIÊN HỆ
-- ===============================


-- ===============================
-- BẢNG CẤU HÌNH HỆ THỐNG
-- ===============================


INSERT INTO system_config (config_key, config_value, description) VALUES
('site_name', 'Vật Tư Y Tế ABC', 'Tên website'),
('site_email', 'info@vattuyteabc.com', 'Email liên hệ'),
('site_phone', '1900-xxxx', 'Số điện thoại'),
('free_shipping_amount', '500000', 'Đơn hàng miễn phí ship (VNĐ)'),
('default_shipping_fee', '30000', 'Phí ship mặc định (VNĐ)'),
('min_order_amount', '50000', 'Giá trị đơn hàng tối thiểu (VNĐ)'),
('loyalty_points_per_vnd', '10000', 'Số tiền để được 1 điểm (VNĐ)'),
('points_to_vnd_ratio', '1000', '1 điểm = bao nhiêu VNĐ'),
('currency', 'VNĐ', 'Đơn vị tiền tệ'),
('bronze_min_spent', '0', 'Chi tiêu tối thiểu hạng Đồng'),
('silver_min_spent', '5000000', 'Chi tiêu tối thiểu hạng Bạc'),
('gold_min_spent', '15000000', 'Chi tiêu tối thiểu hạng Vàng'),
('platinum_min_spent', '50000000', 'Chi tiêu tối thiểu hạng Bạch Kim');

-- ===============================
-- BẢNG LỊCH LÀM VIỆC NHÂN VIÊN
-- ===============================


-- ===============================
-- BẢNG CHẤM CÔNG
-- ===============================


-- ===============================
-- VIEWS - Các view hữu ích
-- ===============================

-- View tổng quan sản phẩm
CREATE VIEW vw_product_overview AS
SELECT 
    md.device_id,
    md.name,
    md.slug,
    md.sku,
    md.price,
    md.discount_percent,
    md.stock_quantity,
    md.status,
    md.view_count,
    md.sold_count,
    c.name AS category_name,
    b.name AS brand_name,
    s.name AS supplier_name,
    COALESCE(AVG(r.rating), 0) AS avg_rating,
    COUNT(DISTINCT r.review_id) AS review_count
FROM medical_device md
LEFT JOIN category c ON md.category_id = c.category_id
LEFT JOIN brand b ON md.brand_id = b.brand_id
LEFT JOIN supplier s ON md.supplier_id = s.supplier_id
LEFT JOIN review r ON md.device_id = r.device_id AND r.status = 'Approved'
GROUP BY md.device_id;

-- View thống kê đơn hàng
CREATE VIEW vw_order_statistics AS
SELECT 
    o.order_id,
    o.order_code,
    o.customer_id,
    c.full_name AS customer_name,
    c.email AS customer_email,
    c.phone AS customer_phone,
    c.customer_tier,
    o.total_price,
    o.status,
    o.payment_status,
    o.payment_method,
    o.created_at,
    e.full_name AS assigned_employee,
    COUNT(od.order_detail_id) AS total_items,
    SUM(od.quantity) AS total_quantity
FROM `order` o
LEFT JOIN customer c ON o.customer_id = c.customer_id
LEFT JOIN employee e ON o.assigned_to = e.employee_id
LEFT JOIN order_detail od ON o.order_id = od.order_id
GROUP BY o.order_id;

-- View thống kê khách hàng
CREATE VIEW vw_customer_summary AS
SELECT 
    c.customer_id,
    c.customer_code,
    c.full_name,
    c.email,
    c.phone,
    c.customer_tier,
    c.loyalty_points,
    c.total_spent,
    c.total_orders,
    c.last_order_date,
    c.status,
    COUNT(DISTINCT o.order_id) AS completed_orders,
    COUNT(DISTINCT r.review_id) AS review_count,
    AVG(r.rating) AS avg_rating_given
FROM customer c
LEFT JOIN `order` o ON c.customer_id = o.customer_id AND o.status = 'Hoàn thành'
LEFT JOIN review r ON c.customer_id = r.customer_id
GROUP BY c.customer_id;

-- View nhân viên và hiệu suất
CREATE VIEW vw_employee_performance AS
SELECT 
    e.employee_id,
    e.employee_code,
    e.full_name,
    e.position,
    e.department,
    r.role_name,
    e.status,
    COUNT(DISTINCT o.order_id) AS orders_handled,
    SUM(CASE WHEN o.status = 'Hoàn thành' THEN o.total_price ELSE 0 END) AS total_sales,
    COUNT(DISTINCT cm.message_id) AS messages_replied
FROM employee e
LEFT JOIN role r ON e.role_id = r.role_id
LEFT JOIN `order` o ON e.employee_id = o.assigned_to
LEFT JOIN contact_message cm ON e.employee_id = cm.replied_by
GROUP BY e.employee_id;

-- View sản phẩm cần nhập thêm
CREATE VIEW vw_low_stock_products AS
SELECT 
    md.device_id,
    md.name,
    md.sku,
    md.stock_quantity,
    md.min_stock_level,
    (md.min_stock_level - md.stock_quantity) AS need_to_order,
    c.name AS category_name,
    s.name AS supplier_name,
    s.phone AS supplier_phone,
    s.email AS supplier_email
FROM medical_device md
LEFT JOIN category c ON md.category_id = c.category_id
LEFT JOIN supplier s ON md.supplier_id = s.supplier_id
WHERE md.stock_quantity <= md.min_stock_level
    AND md.status != 'Ngừng bán'
ORDER BY (md.min_stock_level - md.stock_quantity) DESC;

-- ===============================
-- STORED PROCEDURES
-- ===============================

-- Procedure tính tổng doanh thu theo khoảng thời gian
DELIMITER $
CREATE PROCEDURE sp_revenue_report(
    IN start_date DATE,
    IN end_date DATE
)
BEGIN
    SELECT 
        DATE(created_at) AS order_date,
        COUNT(order_id) AS total_orders,
        SUM(total_price) AS total_revenue,
        AVG(total_price) AS avg_order_value,
        SUM(CASE WHEN payment_status = 'Đã thanh toán' THEN total_price ELSE 0 END) AS paid_amount,
        SUM(CASE WHEN payment_status = 'Chưa thanh toán' THEN total_price ELSE 0 END) AS unpaid_amount
    FROM `order`
    WHERE status = 'Hoàn thành'
        AND DATE(created_at) BETWEEN start_date AND end_date
    GROUP BY DATE(created_at)
    ORDER BY order_date DESC;
END$
DELIMITER ;

-- Procedure cập nhật số lượng tồn kho
DELIMITER $
CREATE PROCEDURE sp_update_stock(
    IN p_device_id VARCHAR(50),
    IN p_quantity INT,
    IN p_operation VARCHAR(10) -- 'ADD' hoặc 'SUBTRACT'
)
BEGIN
    IF p_operation = 'ADD' THEN
        UPDATE medical_device 
        SET stock_quantity = stock_quantity + p_quantity
        WHERE device_id = p_device_id;
    ELSEIF p_operation = 'SUBTRACT' THEN
        UPDATE medical_device 
        SET stock_quantity = stock_quantity - p_quantity
        WHERE device_id = p_device_id AND stock_quantity >= p_quantity;
    END IF;
    
    -- Cập nhật trạng thái hết hàng
    UPDATE medical_device 
    SET status = CASE 
        WHEN stock_quantity <= 0 THEN 'Hết hàng'
        ELSE 'Còn hàng'
    END
    WHERE device_id = p_device_id;
END$
DELIMITER ;

-- Procedure cập nhật hạng khách hàng
DELIMITER $
CREATE PROCEDURE sp_update_customer_tier(
    IN p_customer_id INT
)
BEGIN
    DECLARE v_total_spent DECIMAL(15,2);
    
    SELECT total_spent INTO v_total_spent
    FROM customer
    WHERE customer_id = p_customer_id;
    
    UPDATE customer
    SET customer_tier = CASE
        WHEN v_total_spent >= 50000000 THEN 'Platinum'
        WHEN v_total_spent >= 15000000 THEN 'Gold'
        WHEN v_total_spent >= 5000000 THEN 'Silver'
        ELSE 'Bronze'
    END
    WHERE customer_id = p_customer_id;
END$
DELIMITER ;

-- Procedure kiểm tra và áp dụng mã khuyến mãi
DELIMITER $
CREATE PROCEDURE sp_apply_promotion(
    IN p_promotion_code VARCHAR(50),
    IN p_customer_id INT,
    IN p_order_amount DECIMAL(15,2),
    OUT p_discount_amount DECIMAL(15,2),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_promotion_id INT;
    DECLARE v_discount_type VARCHAR(20);
    DECLARE v_discount_value DECIMAL(15,2);
    DECLARE v_min_order_amount DECIMAL(15,2);
    DECLARE v_max_discount DECIMAL(15,2);
    DECLARE v_usage_limit INT;
    DECLARE v_used_count INT;
    DECLARE v_usage_per_customer INT;
    DECLARE v_customer_usage INT;
    DECLARE v_customer_tier VARCHAR(20);
    DECLARE v_required_tier VARCHAR(20);
    
    SET p_discount_amount = 0;
    SET p_message = 'Mã không hợp lệ';
    
    -- Lấy thông tin khách hàng
    SELECT customer_tier INTO v_customer_tier
    FROM customer WHERE customer_id = p_customer_id;
    
    -- Lấy thông tin khuyến mãi
    SELECT promotion_id, discount_type, discount_value, min_order_amount, 
           max_discount_amount, usage_limit, used_count, usage_per_customer, customer_tier
    INTO v_promotion_id, v_discount_type, v_discount_value, v_min_order_amount,
         v_max_discount, v_usage_limit, v_used_count, v_usage_per_customer, v_required_tier
    FROM promotion
    WHERE code = p_promotion_code
        AND is_active = TRUE
        AND (start_date IS NULL OR start_date <= NOW())
        AND (end_date IS NULL OR end_date >= NOW());
    
    IF v_promotion_id IS NULL THEN
        SET p_message = 'Mã khuyến mãi không tồn tại hoặc đã hết hạn';
    ELSE
        -- Kiểm tra hạng khách hàng
        IF v_required_tier != 'All' AND v_customer_tier != v_required_tier THEN
            SET p_message = CONCAT('Mã này chỉ dành cho khách hàng hạng ', v_required_tier);
        -- Kiểm tra giá trị đơn hàng tối thiểu
        ELSEIF p_order_amount < v_min_order_amount THEN
            SET p_message = CONCAT('Đơn hàng tối thiểu ', v_min_order_amount, ' VNĐ');
        -- Kiểm tra giới hạn sử dụng tổng
        ELSEIF v_usage_limit IS NOT NULL AND v_used_count >= v_usage_limit THEN
            SET p_message = 'Mã khuyến mãi đã hết lượt sử dụng';
        ELSE
            -- Kiểm tra số lần khách hàng đã dùng
            SELECT COUNT(*) INTO v_customer_usage
            FROM promotion_usage
            WHERE promotion_id = v_promotion_id AND customer_id = p_customer_id;
            
            IF v_customer_usage >= v_usage_per_customer THEN
                SET p_message = 'Bạn đã sử dụng hết số lần cho mã này';
            ELSE
                -- Tính giảm giá
                IF v_discount_type = 'Percent' THEN
                    SET p_discount_amount = p_order_amount * v_discount_value / 100;
                    IF v_max_discount IS NOT NULL AND p_discount_amount > v_max_discount THEN
                        SET p_discount_amount = v_max_discount;
                    END IF;
                ELSEIF v_discount_type = 'Fixed' THEN
                    SET p_discount_amount = v_discount_value;
                END IF;
                
                SET p_message = 'Áp dụng thành công';
            END IF;
        END IF;
    END IF;
END$
DELIMITER ;

-- Procedure tạo mã đơn hàng tự động
DELIMITER $
CREATE PROCEDURE sp_generate_order_code(
    OUT p_order_code VARCHAR(50)
)
BEGIN
    DECLARE v_date VARCHAR(8);
    DECLARE v_sequence INT;
    
    SET v_date = DATE_FORMAT(NOW(), '%Y%m%d');
    
    SELECT COALESCE(MAX(CAST(SUBSTRING(order_code, 10) AS UNSIGNED)), 0) + 1
    INTO v_sequence
    FROM `order`
    WHERE order_code LIKE CONCAT('ORD', v_date, '%');
    
    SET p_order_code = CONCAT('ORD', v_date, LPAD(v_sequence, 4, '0'));
END$
DELIMITER ;

-- ===============================
-- DỮ LIỆU MẪU (SAMPLE DATA)
-- ===============================

-- Thêm dữ liệu nhân viên mẫu
INSERT INTO employee (employee_code, username, password_hash, full_name, email, phone, role_id, hire_date, position, department) VALUES
('EMP001', 'admin', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Nguyễn Văn Admin', 'admin@vattuyteabc.com', '0901234567', 1, '2024-01-01', 'Giám đốc', 'Điều hành'),
('EMP002', 'manager01', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Trần Thị Quản Lý', 'manager@vattuyteabc.com', '0901234568', 2, '2024-01-15', 'Quản lý', 'Kinh doanh'),
('EMP003', 'staff01', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Lê Văn Nhân Viên', 'staff01@vattuyteabc.com', '0901234569', 3, '2024-02-01', 'Nhân viên bán hàng', 'Kinh doanh'),
('EMP004', 'warehouse01', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Phạm Văn Kho', 'warehouse@vattuyteabc.com', '0901234570', 4, '2024-02-01', 'Thủ kho', 'Kho hàng');

-- Thêm khách hàng mẫu
INSERT INTO customer (customer_code, username, password_hash, full_name, email, phone, customer_tier, loyalty_points) VALUES
('CUS001', 'customer01', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Nguyễn Thị Khách Hàng', 'customer01@gmail.com', '0987654321', 'Bronze', 100),
('CUS002', 'customer02', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Trần Văn Mua Hàng', 'customer02@gmail.com', '0987654322', 'Silver', 500),
('CUS003', 'vipCustomer', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Lê Thị VIP', 'vip@gmail.com', '0987654323', 'Gold', 2000);

-- Thêm danh mục mẫu
INSERT INTO category (name, slug, description, display_order) VALUES
('Thiết bị đo lường', 'thiet-bi-do-luong', 'Nhiệt kế, huyết áp, đường huyết', 1),
('Vật tư y tế', 'vat-tu-y-te', 'Băng gạc, bông, băng dính', 2),
('Khẩu trang', 'khau-trang', 'Khẩu trang y tế các loại', 3),
('Chăm sóc cá nhân', 'cham-soc-ca-nhan', 'Sản phẩm chăm sóc sức khỏe', 4),
('Dụng cụ y tế', 'dung-cu-y-te', 'Kéo, nhíp, dụng cụ phẫu thuật', 5);

-- Thêm thương hiệu mẫu
INSERT INTO brand (name, slug, country, description) VALUES
('Omron', 'omron', 'Nhật Bản', 'Thương hiệu thiết bị y tế hàng đầu'),
('Microlife', 'microlife', 'Thụy Sĩ', 'Chuyên về thiết bị đo lường y tế'),
('3M', '3m', 'Mỹ', 'Sản xuất vật tư y tế chất lượng cao'),
('Medipure', 'medipure', 'Việt Nam', 'Thương hiệu khẩu trang y tế');

-- Thêm nhà cung cấp mẫu
INSERT INTO supplier (name, phone, email, address, contact_person) VALUES
('Công ty TNHH Thiết Bị Y Tế ABC', '0901234567', 'abc@medical.com', 'Số 1, Đường ABC, Hà Nội', 'Nguyễn Văn A'),
('Công ty CP Vật Tư Y Tế XYZ', '0912345678', 'xyz@medical.com', 'Số 2, Đường XYZ, TP.HCM', 'Trần Thị B'),
('Công ty TNHH Medisupply', '0923456789', 'medisupply@gmail.com', 'Số 3, Đường 123, Đà Nẵng', 'Lê Văn C');

SELECT * FROM employee WHERE username='admin';
SELECT LENGTH(password_hash), password_hash FROM customer WHERE username='customer01';

-- =============================================
-- KẾT THÚC SCRIPT
-- =============================================