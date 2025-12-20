-- Database schema for Medical Supplies Store
CREATE DATABASE IF NOT EXISTS medical_supplies_store;
USE medical_supplies_store;



-- ===============================
-- CREATE TABLES (ORDERED)
-- ===============================

CREATE TABLE role
(
    role_id     INT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE employee
(
    employee_id   INT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(50) UNIQUE  NOT NULL,
    username      VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    full_name     VARCHAR(255)        NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    phone         VARCHAR(20)         NOT NULL,
    address       VARCHAR(255),
    role_id       INT                 NOT NULL,
    date_of_birth DATE,
    gender        ENUM ('MALE','FEMALE','OTHER'),
    citizen_id    VARCHAR(20) UNIQUE,
    position      VARCHAR(100),
    department    VARCHAR(100),
    hire_date     DATE                NOT NULL,
    salary        DECIMAL(15, 2),
    status        ENUM ('ACTIVE','ON_LEAVE','RESIGNED','TERMINATED') DEFAULT 'ACTIVE',
    last_login    DATETIME,
    created_at    DATETIME                                           DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME                                           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by    INT,
    FOREIGN KEY (role_id) REFERENCES role (role_id),
    FOREIGN KEY (created_by) REFERENCES employee (employee_id)
) ENGINE = InnoDB,
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE customer
(
    customer_id         INT AUTO_INCREMENT PRIMARY KEY,
    customer_code       VARCHAR(50) UNIQUE,
    username            VARCHAR(100) UNIQUE,
    password_hash       VARCHAR(255),
    provider            VARCHAR(20) COMMENT 'LOCAL, GOOGLE, FACEBOOK',
    provider_id         VARCHAR(255),
    has_custom_password BOOLEAN                                    DEFAULT FALSE,

    full_name           VARCHAR(255),
    email               VARCHAR(255),
    phone               VARCHAR(20),
    address             VARCHAR(255),
    avatar_url          VARCHAR(255),
    date_of_birth       DATE,
    gender              ENUM ('MALE','FEMALE','OTHER'),

    customer_tier       ENUM ('BRONZE','SILVER','GOLD','PLATINUM') DEFAULT 'BRONZE',
    loyalty_points      INT                                        DEFAULT 0,
    total_spent         DECIMAL(15, 2)                             DEFAULT 0,
    total_orders        INT                                        DEFAULT 0,

    status              ENUM ('ACTIVE','INACTIVE','BLOCKED')       DEFAULT 'ACTIVE',
    email_verified      BOOLEAN                                    DEFAULT FALSE,
    phone_verified      BOOLEAN                                    DEFAULT FALSE,

    last_login          DATETIME,
    last_order_date     DATETIME,

    referral_code       VARCHAR(20) UNIQUE,
    referred_by         INT,

    created_at          DATETIME                                   DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME                                   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (referred_by) REFERENCES customer (customer_id),
    INDEX idx_tier (customer_tier),
    INDEX idx_provider (provider),
    INDEX idx_provider_id (provider_id)
) ENGINE = InnoDB,
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE loyalty_history
(
    history_id   INT AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT NOT NULL,
    points       INT NOT NULL,
    type         ENUM ('EARNED','REDEEMED','EXPIRED','BONUS','REFUND') DEFAULT 'EARNED',
    reference_id INT,
    description  VARCHAR(255),
    created_at   DATETIME                                              DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE CASCADE
) ENGINE = InnoDB,
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE category
(
    category_id      INT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    parent_id        INT          NULL,
    slug             VARCHAR(100) UNIQUE,
    description      TEXT,
    image_url        VARCHAR(255),
    meta_title       VARCHAR(255),
    meta_description TEXT,
    display_order    INT      DEFAULT 0,
    is_active        BOOLEAN  DEFAULT TRUE,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES category (category_id) ON DELETE SET NULL,
    INDEX idx_slug (slug),
    INDEX idx_parent (parent_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE brand
(
    brand_id    INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(100) UNIQUE,
    country     VARCHAR(100),
    logo_url    VARCHAR(255),
    description TEXT,
    website     VARCHAR(255),
    is_active   BOOLEAN  DEFAULT TRUE,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_slug (slug)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE supplier
(
    supplier_id    INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    address        VARCHAR(255),
    phone          VARCHAR(20),
    email          VARCHAR(100),
    tax_code       VARCHAR(50),
    bank_account   VARCHAR(100),
    bank_name      VARCHAR(100),
    description    TEXT,
    status         ENUM ('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
    created_at     DATETIME                   DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME                   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE medical_device
(
    device_id         VARCHAR(50) PRIMARY KEY,
    name              VARCHAR(255)   NOT NULL,
    slug              VARCHAR(255) UNIQUE,
    sku               VARCHAR(100) UNIQUE,
    category_id       INT,
    brand_id          INT,
    supplier_id       INT,
    description       TEXT,
    specification     TEXT,
    usage_instruction TEXT,
    price             DECIMAL(15, 2) NOT NULL,
    original_price    DECIMAL(15, 2),
    discount_percent  INT                                      DEFAULT 0,
    stock_quantity    INT                                      DEFAULT 0,
    min_stock_level   INT                                      DEFAULT 10,
    unit              VARCHAR(50)                              DEFAULT 'Cái',
    weight            DECIMAL(10, 2) COMMENT 'Khối lượng (kg)',
    dimensions        VARCHAR(100) COMMENT 'Kích thước (cm)',
    warranty_period   INT COMMENT 'Thời gian bảo hành (tháng)',
    status            ENUM ('Còn_hàng','Hết_hàng','Ngừng_bán') DEFAULT 'Còn_hàng',
    is_featured       BOOLEAN                                  DEFAULT FALSE,
    is_new            BOOLEAN                                  DEFAULT FALSE,
    view_count        INT                                      DEFAULT 0,
    sold_count        INT                                      DEFAULT 0,
    image_url         VARCHAR(255),
    gallery_urls      TEXT,
    meta_keywords     TEXT,
    meta_description  TEXT,
    created_at        DATETIME                                 DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME                                 DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    image_public_id VARCHAR(255),
    FOREIGN KEY (category_id) REFERENCES category (category_id) ON DELETE SET NULL,
    FOREIGN KEY (brand_id) REFERENCES brand (brand_id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES supplier (supplier_id) ON DELETE SET NULL,
    INDEX idx_name (name),
    INDEX idx_slug (slug),
    INDEX idx_sku (sku),
    INDEX idx_category (category_id),
    INDEX idx_brand (brand_id),
    INDEX idx_status (status),
    INDEX idx_price (price),
    INDEX idx_featured (is_featured),
    FULLTEXT idx_search (name, description)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE stock_import
(
    import_id    INT AUTO_INCREMENT PRIMARY KEY,
    import_code  VARCHAR(50) UNIQUE,
    supplier_id  INT,
    import_date  DATETIME                                 DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(15, 2) NOT NULL,
    note         TEXT,
    status       ENUM ('PENDING','COMPLETED','CANCEllED') DEFAULT 'PENDING',
    created_by   INT COMMENT 'employee_id',
    approved_by  INT COMMENT 'employee_id',
    approved_at  DATETIME,
    created_at   DATETIME                                 DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME                                 DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES supplier (supplier_id),
    FOREIGN KEY (created_by) REFERENCES employee (employee_id),
    FOREIGN KEY (approved_by) REFERENCES employee (employee_id),
    INDEX idx_import_code (import_code),
    INDEX idx_import_date (import_date),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE stock_import_detail
(
    import_detail_id INT AUTO_INCREMENT PRIMARY KEY,
    import_id        INT,
    device_id        VARCHAR(50),
    quantity         INT            NOT NULL,
    import_price     DECIMAL(15, 2) NOT NULL,
    total_price      DECIMAL(15, 2) NOT NULL,
    expiry_date      DATE,
    batch_number     VARCHAR(100),
    FOREIGN KEY (import_id) REFERENCES stock_import (import_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES medical_device (device_id),
    INDEX idx_import (import_id),
    INDEX idx_device (device_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE promotion
(
    promotion_id        INT AUTO_INCREMENT PRIMARY KEY,
    code                VARCHAR(50) UNIQUE NOT NULL,
    name                VARCHAR(255),
    description         TEXT,
    discount_type       ENUM ('PERCENT','FIXED','FREESHIP')              DEFAULT 'PERCENT',
    discount_value      DECIMAL(15, 2)     NOT NULL,
    min_order_amount    DECIMAL(15, 2)                                   DEFAULT 0,
    max_discount_amount DECIMAL(15, 2),
    usage_limit         INT,
    used_count          INT                                              DEFAULT 0,
    usage_per_customer  INT                                              DEFAULT 1,
    customer_tier       ENUM ('ALL','BRONZE','SILVER','GOLD','PLATINUM') DEFAULT 'ALL',
    start_date          DATETIME,
    end_date            DATETIME,
    is_active           BOOLEAN                                          DEFAULT TRUE,
    created_by          INT,
    created_at          DATETIME                                         DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME                                         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES employee (employee_id)
) ENGINE = InnoDB,
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE promotion_category
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    promotion_id INT,
    category_id  INT,
    FOREIGN KEY (promotion_id) REFERENCES promotion (promotion_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category (category_id) ON DELETE CASCADE,
    UNIQUE KEY unique_promo_cat (promotion_id, category_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE promotion_product
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    promotion_id INT,
    device_id    VARCHAR(50),
    FOREIGN KEY (promotion_id) REFERENCES promotion (promotion_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES medical_device (device_id) ON DELETE CASCADE,
    UNIQUE KEY unique_promo_prod (promotion_id, device_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE customer_address
(
    address_id    INT AUTO_INCREMENT PRIMARY KEY,
    customer_id   INT,
    receiver_name VARCHAR(255) NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    address       VARCHAR(255) NOT NULL,
    ward          VARCHAR(100),
    district      VARCHAR(100),
    province      VARCHAR(100),
    is_default    BOOLEAN  DEFAULT FALSE,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE CASCADE,
    INDEX idx_customer (customer_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `order` (
                         order_id            INT AUTO_INCREMENT PRIMARY KEY,
                         order_code          VARCHAR(50) NOT NULL UNIQUE,

                         customer_id         INT,
                         address_id          INT,
                         promotion_id        INT,

                         receiver_name       VARCHAR(255) NOT NULL,
                         receiver_phone      VARCHAR(20)  NOT NULL,
                         receiver_address    VARCHAR(500) NOT NULL,

                         subtotal            DECIMAL(15,2) NOT NULL,
                         shipping_fee        DECIMAL(10,2) DEFAULT 0,
                         discount_amount     DECIMAL(10,2) DEFAULT 0,
                         loyalty_points_used INT          DEFAULT 0,
                         loyalty_discount    DECIMAL(10,2) DEFAULT 0,
                         total_price         DECIMAL(15,2) NOT NULL,

                         payment_method      ENUM('COD','VNPAY') DEFAULT 'COD',
                         payment_status      ENUM('UNPAID','PAID','REFUNDED') DEFAULT 'UNPAID',
                         transaction_id      VARCHAR(100),

                         status              ENUM(
                             'PENDING',
                             'CONFIRMED',
                             'PREPARING',
                             'SHIPPING',
                             'COMPLETED',
                             'CANCELLED',
                             'RETURNED'
                             ) DEFAULT 'PENDING',

                         note                TEXT,
                         internal_note       TEXT,
                         cancel_reason       TEXT,

                         assigned_to         INT,
                         confirmed_by        INT,

                         confirmed_at        DATETIME,
                         prepared_at         DATETIME,
                         shipped_at          DATETIME,
                         completed_at        DATETIME,
                         cancelled_at        DATETIME,

                         created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- ================= FOREIGN KEYS =================
                         CONSTRAINT fk_order_customer
                             FOREIGN KEY (customer_id) REFERENCES customer(customer_id),

                         CONSTRAINT fk_order_address
                             FOREIGN KEY (address_id) REFERENCES customer_address(address_id),

                         CONSTRAINT fk_order_promotion
                             FOREIGN KEY (promotion_id) REFERENCES promotion(promotion_id),

                         CONSTRAINT fk_order_assigned
                             FOREIGN KEY (assigned_to) REFERENCES employee(employee_id),

                         CONSTRAINT fk_order_confirmed
                             FOREIGN KEY (confirmed_by) REFERENCES employee(employee_id),

    -- ================= INDEXES =================
                         INDEX idx_order_code (order_code),
                         INDEX idx_order_customer (customer_id),
                         INDEX idx_order_status (status),
                         INDEX idx_payment_status (payment_status),
                         INDEX idx_created_at (created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_detail
(
    order_detail_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id        INT,
    device_id       VARCHAR(50),
    device_name     VARCHAR(255)   NOT NULL,
    device_image    VARCHAR(255),
    quantity        INT            NOT NULL,
    unit_price      DECIMAL(15, 2) NOT NULL,
    total_price     DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order` (order_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES medical_device (device_id),
    INDEX idx_order (order_id),
    INDEX idx_device (device_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE promotion_usage
(
    usage_id        INT AUTO_INCREMENT PRIMARY KEY,
    promotion_id    INT,
    customer_id     INT,
    order_id        INT,
    discount_amount DECIMAL(10, 2),
    used_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (promotion_id) REFERENCES promotion (promotion_id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES `order` (order_id),
    INDEX idx_promotion (promotion_id),
    INDEX idx_customer (customer_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE order_status_history
(
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id   INT,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    note       TEXT,
    changed_by INT COMMENT 'employee_id',
    changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES `order` (order_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES employee (employee_id),
    INDEX idx_order (order_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE cart
(
    cart_id     INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    device_id   VARCHAR(50),
    quantity    INT      DEFAULT 1,
    added_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES medical_device (device_id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_item (customer_id, device_id),
    INDEX idx_customer (customer_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE wishlist
(
    wishlist_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    device_id   VARCHAR(50),
    added_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES medical_device (device_id) ON DELETE CASCADE,
    UNIQUE KEY unique_wishlist (customer_id, device_id),
    INDEX idx_customer (customer_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE review
(
    review_id            INT AUTO_INCREMENT PRIMARY KEY,
    customer_id          INT,
    device_id            VARCHAR(50),
    order_id             INT COMMENT 'Đánh giá từ đơn hàng nào',
    rating               INT CHECK (rating BETWEEN 1 AND 5),
    comment              TEXT,
    images               TEXT COMMENT 'JSON array of image URLs',
    is_verified_purchase BOOLEAN                                DEFAULT FALSE,
    admin_reply          TEXT,
    replied_by           INT COMMENT 'employee_id',
    replied_at           DATETIME,
    status               ENUM ('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    created_at           DATETIME                               DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME                               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES medical_device (device_id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES `order` (order_id),
    FOREIGN KEY (replied_by) REFERENCES employee (employee_id),
    INDEX idx_device (device_id),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_rating (rating)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE notification
(
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    target_type     ENUM ('Customer','Employee') NOT NULL,
    customer_id     INT,
    employee_id     INT,
    title           VARCHAR(255)                 NOT NULL,
    content         TEXT,
    type            ENUM ('ORDER','PROMOTION','SYSTEM','REVIEW','STOCK','TASK') DEFAULT 'SYSTEM',
    reference_id    INT COMMENT 'ID liên quan (order_id, promotion_id...)',
    is_read         BOOLEAN                                                     DEFAULT FALSE,
    created_at      DATETIME                                                    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employee (employee_id) ON DELETE CASCADE,
    INDEX idx_customer (customer_id),
    INDEX idx_employee (employee_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE banner
(
    banner_id     INT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(255),
    image_url     VARCHAR(255) NOT NULL,
    link_url      VARCHAR(255),
    position      ENUM ('Home_Slider','Sidebar','Top','Bottom','Category') DEFAULT 'Home_Slider',
    display_order INT                                                      DEFAULT 0,
    start_date    DATETIME,
    end_date      DATETIME,
    is_active     BOOLEAN                                                  DEFAULT TRUE,
    click_count   INT                                                      DEFAULT 0,
    created_by    INT COMMENT 'employee_id',
    created_at    DATETIME                                                 DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES employee (employee_id),
    INDEX idx_position (position),
    INDEX idx_active (is_active)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE blog_post
(
    post_id        INT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    slug           VARCHAR(255) UNIQUE,
    content        TEXT,
    excerpt        TEXT,
    featured_image VARCHAR(255),
    author_id      INT COMMENT 'employee_id',
    category_id    INT,
    view_count     INT                                   DEFAULT 0,
    status         ENUM ('Draft','Published','Archived') DEFAULT 'Draft',
    published_at   DATETIME,
    created_at     DATETIME                              DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME                              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES employee (employee_id),
    INDEX idx_slug (slug),
    INDEX idx_status (status),
    INDEX idx_published (published_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE faq
(
    faq_id        INT AUTO_INCREMENT PRIMARY KEY,
    question      VARCHAR(500) NOT NULL,
    answer        TEXT         NOT NULL,
    category      VARCHAR(100),
    display_order INT      DEFAULT 0,
    is_active     BOOLEAN  DEFAULT TRUE,
    created_by    INT COMMENT 'employee_id',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES employee (employee_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE contact_message
(
    message_id    INT AUTO_INCREMENT PRIMARY KEY,
    customer_id   INT COMMENT 'Nếu là khách hàng đã đăng ký',
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    phone         VARCHAR(20),
    subject       VARCHAR(255),
    message       TEXT         NOT NULL,
    status        ENUM ('New','Processing','Resolved','Closed') DEFAULT 'New',
    assigned_to   INT COMMENT 'employee_id',
    replied_by    INT COMMENT 'employee_id',
    reply_content TEXT,
    replied_at    DATETIME,
    created_at    DATETIME                                      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    FOREIGN KEY (assigned_to) REFERENCES employee (employee_id),
    FOREIGN KEY (replied_by) REFERENCES employee (employee_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE system_config
(
    config_id    INT AUTO_INCREMENT PRIMARY KEY,
    config_key   VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    description  VARCHAR(255),
    updated_by   INT COMMENT 'employee_id',
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES employee (employee_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE employee_schedule
(
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    work_date   DATE                                              NOT NULL,
    shift       ENUM ('MORNING','AFTERNOON','EVENING','FULL_DAY') NOT NULL,
    start_time  TIME,
    end_time    TIME,
    status      ENUM ('SCHEDULED','COMPLETED','ABSENT','LATE') DEFAULT 'SCHEDULED',
    note        TEXT,
    created_at  DATETIME                                       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee (employee_id) ON DELETE CASCADE,
    INDEX idx_employee (employee_id),
    INDEX idx_work_date (work_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE attendance
(
    attendance_id  INT AUTO_INCREMENT PRIMARY KEY,
    employee_id    INT,
    check_in       DATETIME,
    check_out      DATETIME,
    work_hours     DECIMAL(4, 2) COMMENT 'Số giờ làm việc',
    overtime_hours DECIMAL(4, 2)                                  DEFAULT 0,
    status         ENUM ('ON_TIME','LATE','EARLY_LEAVE','ABSENT') DEFAULT 'ON_TIME',
    note           TEXT,
    created_at     DATETIME                                       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee (employee_id) ON DELETE CASCADE,
    INDEX idx_employee (employee_id),
    INDEX idx_date (check_in)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- ===============================
-- BẢNG QUẢN LÝ VAI TRÒ
-- ===============================


INSERT INTO role (role_name, description)
VALUES ('ADMIN', 'Toàn quyền hệ thống'),
       ('MANAGER', 'Quản lý'),
       ('STAFF', 'Nhân viên'),
       ('WAREHOUSE', 'Kho'),
       ('CUSTOMER', 'Khách hàng');

-- Trigger tính tổng giá nhập kho
DELIMITER $$
CREATE TRIGGER trg_stock_import_detail_total
    BEFORE INSERT
    ON stock_import_detail
    FOR EACH ROW
BEGIN
    SET NEW.total_price = NEW.quantity * NEW.import_price;
END$$
DELIMITER ;

-- Trigger tự động tính total_price cho order_detail
DELIMITER $$
CREATE TRIGGER trg_order_detail_before_insert
    BEFORE INSERT
    ON order_detail
    FOR EACH ROW
BEGIN
    SET NEW.total_price = NEW.quantity * NEW.unit_price;
END$$

CREATE TRIGGER trg_order_detail_before_update
    BEFORE UPDATE
    ON order_detail
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
    IF NEW.status = 'COMPLETED' AND OLD.status <> 'COMPLETED' THEN

        UPDATE customer
        SET total_spent = total_spent + NEW.total_price,
            total_orders = total_orders + 1,
            last_order_date = NEW.completed_at
        WHERE customer_id = NEW.customer_id;

        INSERT INTO loyalty_history (customer_id, points, type, reference_id, description)
        VALUES (
                   NEW.customer_id,
                   FLOOR(NEW.total_price / 10000),
                   'EARNED',
                   NEW.order_id,
                   CONCAT('Order ', NEW.order_code)
               );

        UPDATE customer
        SET loyalty_points = loyalty_points + FLOOR(NEW.total_price / 10000)
        WHERE customer_id = NEW.customer_id;

        CALL sp_update_customer_tier(NEW.customer_id);
    END IF;
END$$
DELIMITER ;
INSERT INTO system_config (config_key, config_value, description)
VALUES ('site_name', 'Vật Tư Y Tế ABC', 'Tên website'),
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

-- View tổng quan sản phẩm
CREATE VIEW vw_product_overview AS
SELECT md.device_id,
       md.name,
       md.slug,
       md.sku,
       md.price,
       md.discount_percent,
       md.stock_quantity,
       md.status,
       md.view_count,
       md.sold_count,
       c.name                      AS category_name,
       b.name                      AS brand_name,
       s.name                      AS supplier_name,
       COALESCE(AVG(r.rating), 0)  AS avg_rating,
       COUNT(DISTINCT r.review_id) AS review_count
FROM medical_device md
         LEFT JOIN category c ON md.category_id = c.category_id
         LEFT JOIN brand b ON md.brand_id = b.brand_id
         LEFT JOIN supplier s ON md.supplier_id = s.supplier_id
         LEFT JOIN review r ON md.device_id = r.device_id AND r.status = 'Approved'
GROUP BY md.device_id;

-- View thống kê đơn hàng
CREATE VIEW vw_order_statistics AS
SELECT o.order_id,
       o.order_code,
       o.customer_id,
       c.full_name               AS customer_name,
       c.email                   AS customer_email,
       c.phone                   AS customer_phone,
       c.customer_tier,
       o.total_price,
       o.status,
       o.payment_status,
       o.payment_method,
       o.created_at,
       e.full_name               AS assigned_employee,
       COUNT(od.order_detail_id) AS total_items,
       SUM(od.quantity)          AS total_quantity
FROM `order` o
         LEFT JOIN customer c ON o.customer_id = c.customer_id
         LEFT JOIN employee e ON o.assigned_to = e.employee_id
         LEFT JOIN order_detail od ON o.order_id = od.order_id
GROUP BY o.order_id;

-- View thống kê khách hàng
CREATE VIEW vw_customer_summary AS
SELECT c.customer_id,
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
       COUNT(DISTINCT o.order_id)  AS COMPLETED_orders,
       COUNT(DISTINCT r.review_id) AS review_count,
       AVG(r.rating)               AS avg_rating_given
FROM customer c
         LEFT JOIN `order` o ON c.customer_id = o.customer_id AND o.status = 'Hoàn thành'
         LEFT JOIN review r ON c.customer_id = r.customer_id
GROUP BY c.customer_id;

-- View nhân viên và hiệu suất
CREATE VIEW vw_employee_performance AS
SELECT e.employee_id,
       e.employee_code,
       e.full_name,
       e.position,
       e.department,
       r.role_name,
       e.status,
       COUNT(DISTINCT o.order_id)                                           AS orders_handled,
       SUM(CASE WHEN o.status = 'Hoàn thành' THEN o.total_price ELSE 0 END) AS total_sales,
       COUNT(DISTINCT cm.message_id)                                        AS messages_replied
FROM employee e
         LEFT JOIN role r ON e.role_id = r.role_id
         LEFT JOIN `order` o ON e.employee_id = o.assigned_to
         LEFT JOIN contact_message cm ON e.employee_id = cm.replied_by
GROUP BY e.employee_id;

-- View sản phẩm cần nhập thêm
CREATE VIEW vw_low_stock_products AS
SELECT md.device_id,
       md.name,
       md.sku,
       md.stock_quantity,
       md.min_stock_level,
       (md.min_stock_level - md.stock_quantity) AS need_to_order,
       c.name                                   AS category_name,
       s.name                                   AS supplier_name,
       s.phone                                  AS supplier_phone,
       s.email                                  AS supplier_email
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
    SELECT DATE(created_at)                                                     AS order_date,
           COUNT(order_id)                                                      AS total_orders,
           SUM(total_price)                                                     AS total_revenue,
           AVG(total_price)                                                     AS avg_order_value,
           SUM(CASE WHEN payment_status = 'PAID' THEN total_price ELSE 0 END)   AS paid_amount,
           SUM(CASE WHEN payment_status = 'UNPAID' THEN total_price ELSE 0 END) AS unpaid_amount
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
        WHERE device_id = p_device_id
          AND stock_quantity >= p_quantity;
    END IF;

    -- Cập nhật trạng thái hết hàng
    UPDATE medical_device
    SET status = CASE
                     WHEN stock_quantity <= 0 THEN 'Hết hàng'
                     ELSE 'Còn_hàng'
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
    DECLARE v_total_spent DECIMAL(15, 2);

    SELECT total_spent
    INTO v_total_spent
    FROM customer
    WHERE customer_id = p_customer_id;

    UPDATE customer
    SET customer_tier = CASE
                            WHEN v_total_spent >= 50000000 THEN 'PLATINUM'
                            WHEN v_total_spent >= 15000000 THEN 'GOLD'
                            WHEN v_total_spent >= 5000000 THEN 'SILVER'
                            ELSE 'BRONZE'
        END
    WHERE customer_id = p_customer_id;
END$
DELIMITER ;

-- Procedure kiểm tra và áp dụng mã khuyến mãi
DELIMITER $
CREATE PROCEDURE sp_apply_promotion(
    IN p_promotion_code VARCHAR(50),
    IN p_customer_id INT,
    IN p_order_amount DECIMAL(15, 2),
    OUT p_discount_amount DECIMAL(15, 2),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_promotion_id INT;
    DECLARE v_discount_type VARCHAR(20);
    DECLARE v_discount_value DECIMAL(15, 2);
    DECLARE v_min_order_amount DECIMAL(15, 2);
    DECLARE v_max_discount DECIMAL(15, 2);
    DECLARE v_usage_limit INT;
    DECLARE v_used_count INT;
    DECLARE v_usage_per_customer INT;
    DECLARE v_customer_usage INT;
    DECLARE v_customer_tier VARCHAR(20);
    DECLARE v_required_tier VARCHAR(20);

    SET p_discount_amount = 0;
    SET p_message = 'Mã không hợp lệ';

    -- Lấy thông tin khách hàng
    SELECT customer_tier
    INTO v_customer_tier
    FROM customer
    WHERE customer_id = p_customer_id;

    -- Lấy thông tin khuyến mãi
    SELECT promotion_id,
           discount_type,
           discount_value,
           min_order_amount,
           max_discount_amount,
           usage_limit,
           used_count,
           usage_per_customer,
           customer_tier
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
            SELECT COUNT(*)
            INTO v_customer_usage
            FROM promotion_usage
            WHERE promotion_id = v_promotion_id
              AND customer_id = p_customer_id;

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
-- ===============================
-- EMPLOYEES (Nhân viên)
-- ===============================
INSERT INTO employee (employee_code,
                      username,
                      password_hash,
                      full_name,
                      email,
                      phone,
                      address,
                      role_id,
                      date_of_birth,
                      gender,
                      citizen_id,
                      position,
                      department,
                      hire_date,
                      salary,
                      status)
VALUES
-- 1. ADMIN
('EMP001', 'admin',
 '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO',
 'Bùi Trung Nguyên',
 'admin@vattuyteabc.com',
 '0964772715',
 '123 Lê Lợi, Q.1, TP.HCM',
 1,
 '2003-12-05',
 'MALE',
 '079085001234',
 'Giám đốc',
 'Ban Giám đốc',
 '2020-01-01',
 25000000,
 'ACTIVE'),

-- 2. MANAGER
('EMP002', 'huynhnhan',
 '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO',
 'Huỳnh Nhân',
 'huynhnhan@vattuyteabc.com',
 '0901234568',
 '456 Trần Hưng Đạo, Q.5, TP.HCM',
 2,
 '2003-04-16',
 'MALE',
 '079088002345',
 'Quản lý cửa hàng',
 'Kinh doanh',
 '2020-03-15',
 18000000,
 'ACTIVE'),

-- 3. STAFF 01
('EMP003', 'hoangthai',
 '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO',
 'Đặng Ngọc Hoàng Thái',
 'hoangthai@vattuyteabc.com',
 '0901234569',
 '789 Nguyễn Trãi, Q.5, TP.HCM',
 3,
 '2003-12-16',
 'MALE',
 '079092003456',
 'Nhân viên bán hàng',
 'Kinh doanh',
 '2021-06-01',
 12000000,
 'ACTIVE'),
-- 4. STAFF 02
('EMP004', 'tuankiet',
 '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO',
 'Ngô Tuấn Kiệt',
 'tuankiet@vattuyteabc.com',
 '0901234570',
 '321 Võ Văn Tần, Q.3, TP.HCM',
 3,
 '1995-11-25',
 'MALE',
 '079095004567',
 'Nhân viên bán hàng',
 'Kinh doanh',
 '2022-01-15',
 11000000,
 'ACTIVE');
-- ===============================
-- CUSTOMERS (Khách hàng)
-- ===============================
INSERT INTO customer (customer_code, username, password_hash, full_name, email, phone, address,
                      date_of_birth, gender, customer_tier, loyalty_points, total_spent, total_orders, status)
VALUES ('CUS001', 'customer01', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Nguyễn Thị Mai',
        'mai.nguyen@gmail.com', '0912345678', '123 Lê Văn Việt, Q.9, TP.HCM', '1985-03-15', 'Female', 'Gold', 2500,
        18500000, 45, 'ACTIVE'),
       ('CUS002', 'customer02', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Trần Văn Nam',
        'nam.tran@gmail.com', '0912345679', '456 Võ Văn Ngân, Thủ Đức, TP.HCM', '1990-07-22', 'Male', 'Silver', 850,
        7200000, 18, 'ACTIVE'),
       ('CUS003', 'customer03', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Phạm Thị Oanh',
        'oanh.pham@gmail.com', '0912345680', '789 Nguyễn Duy Trinh, Q.2, TP.HCM', '1988-11-10', 'Female', 'Platinum',
        5200, 55000000, 120, 'ACTIVE'),
       ('CUS004', 'customer04', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Lê Văn Phát',
        'phat.le@gmail.com', '0912345681', '321 Điện Biên Phủ, Q.3, TP.HCM', '1992-05-18', 'Male', 'Bronze', 320,
        2800000, 8, 'ACTIVE'),
       ('CUS005', 'customer05', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Hoàng Thị Quỳnh',
        'quynh.hoang@gmail.com', '0912345682', '654 Xa lộ Hà Nội, Q.9, TP.HCM', '1995-09-25', 'Female', 'Silver', 1100,
        9500000, 25, 'ACTIVE'),
       ('CUS006', 'customer06', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Võ Văn Sang',
        'sang.vo@gmail.com', '0912345683', '987 Kha Vạn Cân, Thủ Đức, TP.HCM', '1987-12-05', 'Male', 'Gold', 1850,
        16000000, 35, 'ACTIVE'),
       ('CUS007', 'customer07', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Đặng Thị Tâm',
        'tam.dang@gmail.com', '0912345684', '147 Quang Trung, Gò Vấp, TP.HCM', '1991-04-30', 'Female', 'Bronze', 180,
        1200000, 4, 'ACTIVE'),
       ('CUS008', 'customer08', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Bùi Văn Út',
        'ut.bui@gmail.com', '0912345685', '258 Phan Văn Trị, Gò Vấp, TP.HCM', '1993-08-14', 'Male', 'Silver', 680,
        5800000, 15, 'ACTIVE'),
       ('CUS009', 'customer09', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Cao Thị Vân',
        'van.cao@gmail.com', '0912345686', '369 Hoàng Văn Thụ, Tân Bình, TP.HCM', '1989-06-20', 'Female', 'Gold', 2100,
        19000000, 42, 'ACTIVE'),
       ('CUS010', 'customer10', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Phan Văn Xuân',
        'xuan.phan@gmail.com', '0912345687', '741 Lũy Bán Bích, Tân Phú, TP.HCM', '1994-02-28', 'Male', 'Bronze', 150,
        950000, 3, 'ACTIVE');

-- ===============================
-- CATEGORIES (Danh mục)
-- ===============================
INSERT INTO category (name, slug, description, display_order, is_active)
VALUES ('Thiết bị y tế', 'thiet-bi-y-te', 'Các loại thiết bị y tế chuyên dụng', 1, TRUE),
       ('Dụng cụ chăm sóc', 'dung-cu-cham-soc', 'Dụng cụ chăm sóc sức khỏe hàng ngày', 2, TRUE),
       ('Máy đo sức khỏe', 'may-do-suc-khoe', 'Máy đo huyết áp, đường huyết, nhiệt độ...', 3, TRUE),
       ('Băng y tế', 'bang-y-te', 'Băng gạc, băng dính các loại', 4, TRUE),
       ('Khẩu trang', 'khau-trang', 'Khẩu trang y tế, N95, vải...', 5, TRUE),
       ('Dụng cụ phẫu thuật', 'dung-cu-phau-thuat', 'Dụng cụ phẫu thuật chuyên nghiệp', 6, TRUE),
       ('Thiết bị phục hồi chức năng', 'thiet-bi-phuc-hoi-chuc-nang', 'Thiết bị hỗ trợ phục hồi chức năng', 7, TRUE),
       ('Vật tư tiêu hao', 'vat-tu-tieu-hao', 'Vật tư y tế sử dụng một lần', 8, TRUE),
       ('Máy xông khí dung', 'may-xong-khi-dung', 'Máy xông mũi họng các loại', 9, TRUE),
       ('Thiết bị massage', 'thiet-bi-massage', 'Máy massage trị liệu', 10, TRUE);

-- ===============================
-- BRANDS (Thương hiệu)
-- ===============================
INSERT INTO brand (name, slug, country, description, is_active)
VALUES ('Omron', 'omron', 'Nhật Bản', 'Thương hiệu thiết bị y tế hàng đầu Nhật Bản', TRUE),
       ('Microlife', 'microlife', 'Thụy Sĩ', 'Thiết bị y tế chuyên nghiệp Thụy Sĩ', TRUE),
       ('Beurer', 'beurer', 'Đức', 'Thương hiệu thiết bị chăm sóc sức khỏe Đức', TRUE),
       ('Rossmax', 'rossmax', 'Thụy Sĩ', 'Máy đo huyết áp chất lượng cao', TRUE),
       ('3M', '3m', 'Mỹ', 'Vật tư y tế và khẩu trang chuyên nghiệp', TRUE),
       ('Medline', 'medline', 'Mỹ', 'Vật tư y tế chuyên dụng', TRUE),
       ('B.Braun', 'b-braun', 'Đức', 'Thiết bị và vật tư y tế Đức', TRUE),
       ('Terumo', 'terumo', 'Nhật Bản', 'Dụng cụ y tế Nhật Bản', TRUE),
       ('Abbott', 'abbott', 'Mỹ', 'Thiết bị xét nghiệm và chẩn đoán', TRUE),
       ('Philips', 'philips', 'Hà Lan', 'Thiết bị y tế và chăm sóc sức khỏe', TRUE);

-- ===============================
-- SUPPLIERS (Nhà cung cấp)
-- ===============================
INSERT INTO supplier (name, contact_person, address, phone, email, tax_code, status)
VALUES ('Công ty TNHH Thiết bị Y tế Việt Nam', 'Nguyễn Văn A', '123 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', '0283456789',
        'info@medviet.com', '0123456789', 'ACTIVE'),
       ('Công ty CP Dược phẩm Trung ương 1', 'Trần Thị B', '456 Nguyễn Thị Minh Khai, Q.1, TP.HCM', '0287654321',
        'contact@pharbaco.com', '0234567890', 'ACTIVE'),
       ('Công ty TNHH Medico Việt', 'Phạm Văn C', '789 Võ Văn Kiệt, Q.5, TP.HCM', '0289876543', 'sales@medico.vn',
        '0345678901', 'ACTIVE'),
       ('Công ty CP Thiết bị Y khoa Hải Phòng', 'Lê Thị D', '321 Lạch Tray, Hải Phòng', '0225123456',
        'info@haiphongmedical.vn', '0456789012', 'ACTIVE'),
       ('Công ty TNHH Import Y tế Nam Sài Gòn', 'Hoàng Văn E', '654 Trường Chinh, Q.Tân Bình, TP.HCM', '0281234567',
        'import@namsaigon.com', '0567890123', 'ACTIVE');

-- ===============================
-- MEDICAL DEVICES (Sản phẩm)
-- ===============================
INSERT INTO medical_device (
    device_id, name, slug, sku,
    category_id, brand_id, supplier_id,
    description, specification, usage_instruction,
    price, original_price, discount_percent,
    stock_quantity, min_stock_level,
    unit, weight, dimensions, warranty_period,
    status, is_featured, is_new,
    view_count, sold_count,
    image_url, gallery_urls,
    meta_keywords, meta_description,
    created_at, updated_at,
    image_public_id
) VALUES
-- MD001
('MD001','Máy đo huyết áp Omron HEM-7120','may-do-huyet-ap-omron-hem-7120','OMRON-7120',
 3,1,1,'Máy đo huyết áp bắp tay chính xác cao',NULL,NULL,
 850000,850000,0,50,10,'Cái',NULL,NULL,NULL,
 'Còn_hàng',1,0,0,0,
 'https://res.cloudinary.com/dkzoqr001/image/upload/v1766189295/medical_devices/MD001/main_adbc99f9-8890-4438-af49-38349fd06fd6.jpg','',
 NULL,NULL,'2025-12-20 05:09:46','2025-12-20 06:31:58','medical_devices/MD001/main'),

-- MD002
('MD002','Nhiệt kế điện tử Microlife MT-1622','nhiet-ke-dien-tu-microlife-mt-1622','MICROLIFE-MT1622',
 3,2,1,'Nhiệt kế điện tử đo nhanh',NULL,NULL,
 120000,120000,0,100,20,'Cái',NULL,NULL,NULL,
 'Còn_hàng',0,0,0,0,
 'https://res.cloudinary.com/dkzoqr001/image/upload/medical_devices/MD002/main_437ac7ac-a172-4de6-83f4-540b1137d1d6.jpg','',
 NULL,NULL,'2025-12-20 05:09:46','2025-12-20 06:31:58','medical_devices/MD002/main'),

-- MD003
('MD003','Máy đo đường huyết Beurer GL44','may-do-duong-huyet-beurer-gl44','BEURER-GL44',
 3,3,2,'Máy đo đường huyết nhỏ gọn',NULL,NULL,
 650000,650000,0,40,10,'Cái',NULL,NULL,NULL,
 'Còn_hàng',0,0,0,0,
 'https://res.cloudinary.com/dkzoqr001/image/upload/medical_devices/MD003/main_e02737cc-d497-4f55-a6ab-4eeac94d0bfe.jpg','',
 NULL,NULL,'2025-12-20 05:09:46','2025-12-20 06:31:58','medical_devices/MD003/main'),

-- MD004
('MD004','Khẩu trang y tế 3M 4 lớp (Hộp 50 cái)','khau-trang-y-te-3m-4-lop','3M-MASK-50',
 5,5,3,'Khẩu trang y tế kháng khuẩn',NULL,NULL,
 85000,85000,0,300,50,'Hộp',NULL,NULL,NULL,
 'Còn_hàng',0,0,0,0,
 'https://res.cloudinary.com/dkzoqr001/image/upload/medical_devices/MD004/main_32a46968-5356-45a7-a264-6453ec113202.jpg','',
 NULL,NULL,'2025-12-20 05:09:46','2025-12-20 06:31:58','medical_devices/MD004/main'),

-- MD005
('MD005','Băng gạc y tế Medline 10x10cm','bang-gac-y-te-medline-10x10','MEDLINE-1010',
 4,6,3,'Băng gạc vô trùng',NULL,NULL,
 45000,45000,0,200,40,'Gói',NULL,NULL,NULL,
 'Còn_hàng',0,0,0,0,
 'https://res.cloudinary.com/dkzoqr001/image/upload/medical_devices/MD005/main_b0af1a82-57bd-446a-98de-b46509afbb55.jpg','',
 NULL,NULL,'2025-12-20 05:09:46','2025-12-20 06:31:58','medical_devices/MD005/main'),

-- MD006
('MD006','Máy xông khí dung Omron NE-C803','may-xong-khi-dung-omron-ne-c803','OMRON-C803',
 3,1,1,'Máy xông mũi họng gia đình',NULL,NULL,
 1250000,1250000,0,20,5,'Cái',NULL,NULL,NULL,
 'Còn_hàng',1,0,0,0,
 'https://res.cloudinary.com/dkzoqr001/image/upload/medical_devices/MD006/main_be1a2b3b-baf6-401b-a0b4-e1a97c636c6c.jpg','',
 NULL,NULL,'2025-12-20 05:09:46','2025-12-20 06:31:58','medical_devices/MD006/main'),

-- MD007
('MD007','Máy đo huyết áp cổ tay Beurer BC32','may-do-huyet-ap-co-tay-beurer-bc32','BEURER-BC32',
 3,3,2,'Máy đo huyết áp cổ tay',NULL,NULL,
 550000,550000,0,30,8,'Cái',NULL,NULL,NULL,
 'Còn_hàng',0,0,0,0,
 'https://res.cloudinary.com/dkzoqr001/image/upload/medical_devices/MD007/main_970f6dc9-2bef-4677-820a-7baed24ca68e.jpg','',
 NULL,NULL,'2025-12-20 05:09:46','2025-12-20 06:31:58','medical_devices/MD007/main'),

-- MD008
('MD008','Que thử đường huyết Abbott (Hộp 50 que)','que-thu-duong-huyet-abbott-hop-50-que','ABBOTT-STRIP50',
 3,9,2,'Que thử đường huyết chính xác cao cho máy FreeStyle',NULL,NULL,
 400000,400000,0,80,20,'Cái',NULL,NULL,NULL,
 'Còn_hàng',0,0,0,0,
 'https://res.cloudinary.com/dkzoqr001/image/upload/v1766185802/medical_devices/MD008/main_24aed49e-182a-4210-963f-b2edbe1a809c.jpg','',
 NULL,NULL,'2025-12-20 05:09:46','2025-12-20 06:31:58','medical_devices/MD008/main'),

-- MD009
('MD009','Gối massage hồng ngoại Beurer MG145','goi-massage-hong-ngoai-beurer-mg145','BEURER-MG145',
 10,3,2,'Gối massage cổ vai gáy với đèn hồng ngoại',NULL,NULL,
 1200000,1200000,0,15,5,'Cái',NULL,NULL,NULL,
 'Còn_hàng',1,0,110,15,
 'https://res.cloudinary.com/dkzoqr001/image/upload/v1766185868/medical_devices/MD009/main_0202d0b2-aa6c-4b15-b46b-5e80dba95943.png','',
 NULL,NULL,'2025-12-20 05:09:46','2025-12-20 06:31:58','medical_devices/MD009/main'),

-- MD010
('MD010','Ống nghe y tế Littmann Classic II','ong-nghe-y-te-littmann-classic-ii','LITTMANN-C2',
 1,5,4,'Ống nghe y tế chuyên nghiệp, âm thanh rõ nét',NULL,NULL,
 2000000,2000000,0,20,5,'Cái',NULL,NULL,NULL,
 'Còn_hàng',0,0,0,0,
 'https://res.cloudinary.com/dkzoqr001/image/upload/v1766185755/medical_devices/MD010/main_14188358-be8a-4fa2-8cc1-122932e40f3b.jpg','',
 NULL,NULL,'2025-12-20 05:09:46','2025-12-20 06:31:58','medical_devices/MD010/main'),
-- MD011
    (
    'MD011',
    'Kim tiêm insulin B.Braun 0.3ml',
    'kim-tiem-insulin-bbraun-03ml',
    'BRAUN-INS03',
    8, 7, 3,
    'Kim tiêm insulin siêu mỏng, ít đau',
    NULL, NULL,
    200000.00, 200000.00, 0,
    150, 30,
    'Cái', NULL, NULL, NULL,
    'Còn_hàng', 0, 0,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766185901/medical_devices/MD011/main_434945b5-60e7-4ccd-8c67-d9f7612c0418.jpg',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD011/main_434945b5-60e7-4ccd-8c67-d9f7612c0418'
    ),
-- MD012
(
    'MD012',
    'Máy đo SpO2 kẹp ngón tay Beurer PO30',
    'may-do-spo2-kep-ngon-tay-beurer-po30',
    'BEURER-PO30',
    3, 3, 2,
    'Máy đo nồng độ oxy trong máu và nhịp tim',
    NULL, NULL,
    600000.00, 600000.00, 0,
    35, 10,
    'Cái', NULL, NULL, NULL,
    'Còn_hàng', 1, 0,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766185977/medical_devices/MD012/main_f421842f-88ce-4fde-8f22-22eba42ce554.png',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD012/main_f421842f-88ce-4fde-8f22-22eba42ce554'
),

-- MD013
(
    'MD013',
    'Cân sức khỏe điện tử Omron HN-289',
    'can-suc-khoe-dien-tu-omron-hn-289',
    'OMRON-HN289',
    2, 1, 1,
    'Cân điện tử đo chính xác, màn hình LCD lớn',
    NULL, NULL,
    500000.00, 500000.00, 0,
    45, 10,
    'Cái', NULL, NULL, NULL,
    'Còn_hàng', 0, 0,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186049/medical_devices/MD013/main_31fe7d4d-182e-4c58-a224-ce702d3fb4ad.jpg',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD013/main_31fe7d4d-182e-4c58-a224-ce702d3fb4ad'
),

-- MD014
(
    'MD014',
    'Túi chườm đa năng Medione',
    'tui-chuom-da-nang-medione',
    'MEDIONE-COMP',
    2, 6, 3,
    'Túi chườm nóng/lạnh, giữ ấm 4–6 tiếng',
    NULL, NULL,
    130000.00, 130000.00, 0,
    120, 30,
    'Cái', NULL, NULL, NULL,
    'Còn_hàng', 0, 0,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186208/medical_devices/MD014/main_2d220113-bd49-441c-b37b-89c1aa6ac39c.jpg',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD014/main_2d220113-bd49-441c-b37b-89c1aa6ac39c'
),

-- MD015
(
    'MD015',
    'Máy massage chân Beurer FM60',
    'may-massage-chan-beurer-fm60',
    'BEURER-FM60',
    10, 3, 2,
    'Máy massage chân với chức năng nhiệt',
    NULL, NULL,
    2200000.00, 2200000.00, 0,
    10, 3,
    'Cái', NULL, NULL, NULL,
    'Còn_hàng', 1, 0,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186233/medical_devices/MD015/main_8b4a6e9c-4865-48f1-902d-8d87f5892185.jpg',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD015/main_8b4a6e9c-4865-48f1-902d-8d87f5892185'
),

-- MD016
(
    'MD016',
    'Băng dính y tế 3M Micropore 2.5cm',
    'bang-dinh-y-te-3m-micropore-25cm',
    '3M-MICRO25',
    4, 5, 3,
    'Băng dính y tế không gây dị ứng',
    NULL, NULL,
    40000.00, 40000.00, 0,
    250, 50,
    'Cuộn', NULL, NULL, NULL,
    'Còn_hàng', 0, 0,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186353/medical_devices/MD016/main_ec173c15-d8ed-444b-a3da-5ee9aa511840.jpg',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD016/main_ec173c15-d8ed-444b-a3da-5ee9aa511840'
),

-- MD017
(
    'MD017',
    'Máy đo huyết áp Rossmax MW701f',
    'may-do-huyet-ap-rossmax-mw701f',
    'ROSSMAX-MW701F',
    3, 4, 1,
    'Máy đo huyết áp tự động',
    NULL, NULL,
    950000.00, 950000.00, 0,
    30, 8,
    'Cái', NULL, NULL, NULL,
    'Còn_hàng', 0, 0,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186429/medical_devices/MD017/main_00b36415-4f51-43b3-a0d2-785ff789ce54.jpg',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD017/main_00b36415-4f51-43b3-a0d2-785ff789ce54'
),

-- MD018
(
    'MD018',
    'Khẩu trang N95 3M 9502+ (Hộp 20 cái)',
    'khau-trang-n95-3m-9502-hop-20-cai',
    '3M-N95-20',
    5, 5, 3,
    'Khẩu trang lọc bụi mịn',
    NULL, NULL,
    500000.00, 50000.00, 0,
    200, 40,
    'Hộp', NULL, NULL, NULL,
    'Còn_hàng', 1, 0,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186105/medical_devices/MD018/main_02a8fcf0-3e70-4743-9a45-e6676d3f5e98.jpg',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD018/main_02a8fcf0-3e70-4743-9a45-e6676d3f5e98'
),

-- MD019
(
    'MD019',
    'Bộ test COVID-19 Abbott Panbio',
    'bo-test-covid-19-abbott-panbio',
    'ABBOTT-COVID',
    8, 9, 2,
    'Bộ test nhanh COVID-19 tại nhà',
    NULL, NULL,
    120000.00, 120000.00, 0,
    300, 50,
    'Hộp', NULL, NULL, NULL,
    'Còn_hàng', 0, 0,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186471/medical_devices/MD019/main_8e95615a-1791-446d-8f52-390025ff8ff0.jpg',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD019/main_8e95615a-1791-446d-8f52-390025ff8ff0'
),

-- MD020
(
    'MD020',
    'Máy massage cầm tay Beurer MG70',
    'may-massage-cam-tay-beurer-mg70',
    'BEURER-MG70',
    10, 3, 2,
    'Máy massage cầm tay đa năng',
    NULL, NULL,
    800000.00, 800000.00, 0,
    25, 8,
    'Cái', NULL, NULL, NULL,
    'Còn_hàng', 0, 0,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186527/medical_devices/MD020/main_ce0a3e24-eb78-4bc8-a6a1-65bcea1ea1dd.png',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD020/main_ce0a3e24-eb78-4bc8-a6a1-65bcea1ea1dd'
),

-- MD021
(
    'MD021',
    'Đai Massage Bụng – Lưng Beurer EM39',
    'dai-massage-bung-lung-beurer-em39',
    'BEURER-EM39',
    10, 3, 2,
    'Đai massage xung điện 4 cực',
    NULL, NULL,
    1705000.00, 1705000.00, 0,
    12, 3,
    'Cái', NULL, NULL, NULL,
    'Còn_hàng', 1, 1,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186622/medical_devices/MD021/main_16722b3d-2f7e-4347-afa8-e582d946bb52.jpg',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD021/main_16722b3d-2f7e-4347-afa8-e582d946bb52'
),

-- MD022
(
    'MD022',
    'Máy tạo ẩm y tế Philips HU4803',
    'may-tao-am-y-te-philips-hu4803',
    'PHILIPS-HU4803',
    2, 10, 1,
    'Máy tạo ẩm không khí phòng bệnh',
    NULL, NULL,
    2190000.00, 2190000.00, 0,
    8, 2,
    'Cái', NULL, NULL, NULL,
    'Còn_hàng', 1, 1,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186695/medical_devices/MD022/main_f415f872-4c17-4c0f-b593-1b32a7531884.png',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD022/main_f415f872-4c17-4c0f-b593-1b32a7531884'
),

-- MD023
(
    'MD023',
    'Bộ dụng cụ sơ cứu gia đình',
    'bo-dung-cu-so-cuu-gia-dinh',
    'FIRSTAID-HOME',
    8, 6, 3,
    'Bộ sơ cứu đầy đủ cho gia đình',
    NULL, NULL,
    550000.00, 550000.00, 0,
    50, 15,
    'Bộ', NULL, NULL, NULL,
    'Còn_hàng', 0, 1,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186755/medical_devices/MD023/main_5ee01f6f-41bd-4b09-acc6-b566098309a3.jpg',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD023/main_5ee01f6f-41bd-4b09-acc6-b566098309a3'
),

-- MD024
(
    'MD024',
    'Máy đo nhịp tim Polar H10',
    'may-do-nhip-tim-polar-h10',
    'POLAR-H10',
    3, 10, 4,
    'Dây đo nhịp tim thể thao chính xác',
    NULL, NULL,
    2500000.00, 2500000.00, 0,
    18, 5,
    'Cái', NULL, NULL, NULL,
    'Còn_hàng', 0, 1,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186830/medical_devices/MD024/main_0c190ff9-da1e-4cf1-9972-70ea06c72ea9.png',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD024/main_0c190ff9-da1e-4cf1-9972-70ea06c72ea9'
),

-- MD025
(
    'MD025',
    'Đệm massage toàn thân Beurer MG295',
    'dem-massage-toan-than-beurer-mg295',
    'BEURER-MG295',
    10, 3, 2,
    'Đệm massage toàn thân đa chức năng',
    NULL, NULL,
    3800000.00, 3800000.00, 0,
    6, 2,
    'Cái', NULL, NULL, NULL,
    'Còn_hàng', 1, 1,
    0, 0,
    'https://res.cloudinary.com/dkzoqr001/image/upload/v1766186873/medical_devices/MD025/main_c49c0ea7-367b-4ca3-83b0-5d3dd0b26d1c.jpg',
    '',
    NULL, NULL,
    '2025-12-20 05:09:46',
    '2025-12-20 06:31:58',
    'medical_devices/MD025/main_c49c0ea7-367b-4ca3-83b0-5d3dd0b26d1c'
);

;


-- ===============================
-- PROMOTIONS (Khuyến mãi)
-- ===============================
INSERT INTO promotion (code, name, description, discount_type, discount_value, min_order_amount, max_discount_amount,
                       usage_limit, usage_per_customer, customer_tier, start_date, end_date, is_active, created_by)
VALUES ('WELCOME10', 'Giảm 10% cho khách hàng mới', 'Mã giảm 10% cho đơn hàng đầu tiên', 'Percent', 10, 200000, 50000,
        100, 1, 'All', '2024-01-01', '2025-12-31', TRUE, 1),
       ('FREESHIP', 'Miễn phí vận chuyển', 'Miễn phí ship cho đơn từ 500k', 'FreeShip', 30000, 500000, 30000, NULL, 999,
        'All', '2024-01-01', '2025-12-31', TRUE, 1),
       ('GOLD50', 'Giảm 50k cho hạng Gold', 'Ưu đãi đặc biệt khách hàng Gold', 'Fixed', 50000, 300000, 50000, NULL, 5,
        'Gold', '2024-01-01', '2025-12-31', TRUE, 1),
       ('PLATINUM100', 'Giảm 100k cho hạng Platinum', 'Ưu đãi VIP cho khách hàng Platinum', 'Fixed', 100000, 500000,
        100000, NULL, 10, 'Platinum', '2024-01-01', '2025-12-31', TRUE, 1),
       ('SALE20', 'Giảm 20% tất cả sản phẩm', 'Khuyến mãi lớn giảm 20%', 'Percent', 20, 300000, 200000, 500, 3, 'All',
        '2024-10-01', '2024-12-31', TRUE, 2);

-- ===============================
-- CUSTOMER ADDRESSES (Địa chỉ giao hàng)
-- ===============================
INSERT INTO customer_address (customer_id, receiver_name, phone, address, ward, district, province, is_default)
VALUES (1, 'Nguyễn Thị Mai', '0912345678', '123 Lê Văn Việt', 'Phường Tăng Nhơn Phú A', 'Quận 9', 'TP. Hồ Chí Minh',
        TRUE),
       (2, 'Trần Văn Nam', '0912345679', '456 Võ Văn Ngân', 'Phường Linh Chiểu', 'Thủ Đức', 'TP. Hồ Chí Minh', TRUE),
       (3, 'Phạm Thị Oanh', '0912345680', '789 Nguyễn Duy Trinh', 'Phường Bình Trưng Đông', 'Quận 2', 'TP. Hồ Chí Minh',
        TRUE),
       (4, 'Lê Văn Phát', '0912345681', '321 Điện Biên Phủ', 'Phường 1', 'Quận 3', 'TP. Hồ Chí Minh', TRUE),
       (5, 'Hoàng Thị Quỳnh', '0912345682', '654 Xa lộ Hà Nội', 'Phường Hiệp Phú', 'Quận 9', 'TP. Hồ Chí Minh', TRUE),
       (1, 'Nguyễn Văn Long', '0987654321', '999 Nguyễn Văn Linh', 'Phường Tân Phú', 'Quận 7', 'TP. Hồ Chí Minh',
        FALSE);

-- ===============================
-- ORDERS (Đơn hàng)
-- ===============================
INSERT INTO `order` (
    order_code, customer_id, receiver_name, receiver_phone, receiver_address,
    subtotal, shipping_fee, discount_amount, total_price,
    payment_method, payment_status, status,
    assigned_to, confirmed_by, created_at
)
VALUES
    ('ORD202512010001', 1, 'Nguyễn Thị Mai', '0912345678',
     '123 Lê Văn Việt, P.Tăng Nhơn Phú A, Q.9, TP.HCM',
     970000, 30000, 0, 1000000, 'VNPAY', 'PAID', 'COMPLETED',
     3, 2, '2025-12-01 08:30:00'),

    ('ORD202512030002', 2, 'Trần Văn Nam', '0912345679',
     '456 Võ Văn Ngân, P.Linh Chiểu, Thủ Đức, TP.HCM',
     1220000, 30000, 0, 1250000, 'COD', 'UNPAID', 'SHIPPING',
     3, 2, '2025-12-03 09:15:00'),

    ('ORD202512050003', 3, 'Phạm Thị Oanh', '0912345680',
     '789 Nguyễn Duy Trinh, P.Bình Trưng Đông, Q.2, TP.HCM',
     2450000, 0, 100000, 2350000, 'VNPAY', 'PAID', 'COMPLETED',
     4, 2, '2025-12-05 10:45:00'),

    ('ORD202512070004', 4, 'Lê Văn Phát', '0912345681',
     '321 Điện Biên Phủ, P.1, Q.3, TP.HCM',
     320000, 30000, 35000, 315000, 'VNPAY', 'PAID', 'COMPLETED',
     3, 2, '2025-12-07 14:20:00'),

    ('ORD202512090005', 5, 'Hoàng Thị Quỳnh', '0912345682',
     '654 Xa lộ Hà Nội, P.Hiệp Phú, Q.9, TP.HCM',
     1700000, 30000, 0, 1730000, 'COD', 'UNPAID', 'CONFIRMED',
     4, 2, '2025-12-09 11:30:00'),

    ('ORD202512110006', 1, 'Nguyễn Thị Mai', '0912345678',
     '123 Lê Văn Việt, P.Tăng Nhơn Phú A, Q.9, TP.HCM',
     850000, 30000, 0, 880000, 'COD', 'PAID', 'COMPLETED',
     3, 2, '2025-12-11 15:00:00'),

    ('ORD202512130007', 6, 'Võ Văn Sang', '0912345683',
     '987 Kha Vạn Cân, P.Linh Chiểu, Thủ Đức, TP.HCM',
     550000, 30000, 0, 580000, 'VNPAY', 'PAID', 'COMPLETED',
     4, 2, '2025-12-13 09:00:00'),

    ('ORD202512150008', 7, 'Đặng Thị Tâm', '0912345684',
     '147 Quang Trung, P.10, Gò Vấp, TP.HCM',
     240000, 30000, 0, 270000, 'COD', 'UNPAID', 'PENDING',
     3, NULL, '2025-12-15 16:45:00'),

    ('ORD202512180009', 8, 'Bùi Văn Út', '0912345685',
     '258 Phan Văn Trị, P.11, Gò Vấp, TP.HCM',
     1380000, 30000, 0, 1410000, 'VNPAY', 'PAID', 'PREPARING',
     4, 2, '2025-12-18 13:15:00'),

    ('ORD202512200010', 9, 'Cao Thị Vân', '0912345686',
     '369 Hoàng Văn Thụ, P.4, Tân Bình, TP.HCM',
     950000, 30000, 0, 980000, 'VNPAY', 'PAID', 'COMPLETED',
     3, 2, '2025-12-20 10:30:00');

-- ORDER DETAILS (Chi tiết đơn hàng)
-- ===============================
INSERT INTO order_detail (order_id, device_id, device_name, quantity, unit_price)
VALUES (1, 'MD001', 'Máy đo huyết áp Omron HEM-7120', 1, 850000),
       (1, 'MD002', 'Nhiệt kế điện tử Microlife MT-1622', 1, 120000),
       (2, 'MD006', 'Máy xông khí dung Omron NE-C803', 1, 1250000),
       (3, 'MD009', 'Gối massage hồng ngoại Beurer MG145', 1, 950000),
       (3, 'MD015', 'Máy massage chân Beurer FM60', 1, 1850000),
       (4, 'MD004', 'Khẩu trang y tế 3M 4 lớp (Hộp 50 cái)', 2, 85000),
       (4, 'MD005', 'Băng gạc y tế Medline 10x10cm', 3, 45000),
       (5, 'MD010', 'Ống nghe y tế Littmann Classic II', 1, 1800000),
       (6, 'MD001', 'Máy đo huyết áp Omron HEM-7120', 1, 850000),
       (7, 'MD007', 'Máy đo huyết áp cổ tay Beurer BC32', 1, 550000),
       (8, 'MD002', 'Nhiệt kế điện tử Microlife MT-1622', 2, 120000),
       (9, 'MD003', 'Máy đo đường huyết Beurer GL44', 1, 650000),
       (9, 'MD012', 'Máy đo SpO2 kẹp ngón tay Beurer PO30', 1, 480000),
       (9, 'MD013', 'Cân sức khỏe điện tử Omron HN-289', 1, 420000),
       (10, 'MD009', 'Gối massage hồng ngoại Beurer MG145', 1, 950000);

-- ===============================
-- CART (Giỏ hàng)
-- ===============================
INSERT INTO cart (customer_id, device_id, quantity)
VALUES (1, 'MD012', 1),
       (1, 'MD018', 2),
       (2, 'MD003', 1),
       (4, 'MD006', 1),
       (5, 'MD009', 1),
       (5, 'MD020', 1);

-- ===============================
-- WISHLIST (Danh sách yêu thích)
-- ===============================
INSERT INTO wishlist (customer_id, device_id)
VALUES (1, 'MD015'),
       (1, 'MD010'),
       (2, 'MD009'),
       (3, 'MD006'),
       (4, 'MD012'),
       (5, 'MD015');

-- ===============================
-- REVIEWS (Đánh giá sản phẩm)
-- ===============================
INSERT INTO review (customer_id, device_id, order_id, rating, comment, is_verified_purchase, status, created_at)
VALUES (1, 'MD001', 1, 5, 'Máy đo rất chính xác, dễ sử dụng. Giao hàng nhanh!', TRUE, 'Approved',
        '2024-10-05 10:00:00'),
       (1, 'MD002', 1, 5, 'Nhiệt kế đo nhanh, chính xác. Rất hài lòng!', TRUE, 'Approved', '2024-10-05 10:15:00'),
       (3, 'MD009', 3, 4, 'Gối massage tốt, nhưng hơi ồn một chút.', TRUE, 'Approved', '2024-10-08 14:30:00'),
       (3, 'MD015', 3, 5, 'Máy massage chân rất tuyệt, đáng đồng tiền!', TRUE, 'Approved', '2024-10-08 14:45:00'),
       (4, 'MD004', 4, 5, 'Khẩu trang chất lượng tốt, đóng gói cẩn thận.', TRUE, 'Approved', '2024-10-09 09:00:00'),
       (1, 'MD001', 6, 5, 'Mua lần 2 rồi, sản phẩm rất tốt!', TRUE, 'Approved', '2024-10-11 11:00:00'),
       (6, 'MD007', 7, 4, 'Máy đo cổ tay tiện lợi, nhỏ gọn. Giá hợp lý.', TRUE, 'Approved', '2024-10-12 13:00:00'),
       (9, 'MD009', 10, 5, 'Massage rất sướng, giảm đau vai gáy hiệu quả!', TRUE, 'Approved', '2024-10-15 16:00:00');

-- ===============================
-- STOCK IMPORT (Nhập kho)
-- ===============================
INSERT INTO stock_import (import_code,
                          supplier_id,
                          import_date,
                          total_amount,
                          status,
                          created_by,
                          approved_by,
                          approved_at)
VALUES ('IMP202409010001', 1, '2024-09-01 08:00:00', 45000000, 'COMPLETED', 1, 2, '2024-09-01 09:00:00'),
       ('IMP202409150002', 2, '2024-09-15 09:30:00', 28000000, 'COMPLETED', 1, 2, '2024-09-15 10:30:00'),
       ('IMP202410010003', 3, '2024-10-01 10:00:00', 35000000, 'COMPLETED', 1, 2, '2024-10-01 11:00:00'),
       ('IMP202410080004', 1, '2024-10-08 14:00:00', 52000000, 'PENDING', 1, NULL, NULL);
-- ===============================
-- STOCK IMPORT DETAILS (Chi tiết nhập kho)
-- ===============================
INSERT INTO stock_import_detail (import_id, device_id, quantity, import_price, expiry_date, batch_number)
VALUES (1, 'MD001', 50, 700000, '2027-09-01', 'BATCH001'),
       (1, 'MD002', 100, 90000, '2028-09-01', 'BATCH002'),
       (1, 'MD003', 30, 550000, '2027-09-01', 'BATCH003'),
       (2, 'MD004', 200, 70000, '2026-09-15', 'BATCH004'),
       (2, 'MD005', 150, 35000, '2027-09-15', 'BATCH005'),
       (2, 'MD006', 25, 1050000, '2029-09-15', 'BATCH006'),
       (3, 'MD007', 40, 480000, '2028-10-01', 'BATCH007'),
       (3, 'MD009', 15, 800000, '2029-10-01', 'BATCH008'),
       (3, 'MD010', 20, 1600000, '2030-10-01', 'BATCH009'),
       (4, 'MD012', 35, 400000, '2028-10-08', 'BATCH010'),
       (4, 'MD015', 10, 1600000, '2029-10-08', 'BATCH011');

-- ===============================
-- LOYALTY HISTORY (Lịch sử điểm thưởng)
-- ===============================
INSERT INTO loyalty_history (customer_id, points, type, reference_id, description, created_at)
VALUES (1, 100, 'Earned', 1, 'Tích điểm từ đơn hàng #ORD202410010001', '2024-10-01 20:00:00'),
       (2, 125, 'Earned', 2, 'Tích điểm từ đơn hàng #ORD202410020002', '2024-10-02 20:00:00'),
       (3, 235, 'Earned', 3, 'Tích điểm từ đơn hàng #ORD202410030003', '2024-10-03 20:00:00'),
       (4, 32, 'Earned', 4, 'Tích điểm từ đơn hàng #ORD202410040004', '2024-10-04 20:00:00'),
       (1, 88, 'Earned', 6, 'Tích điểm từ đơn hàng #ORD202410060006', '2024-10-06 20:00:00'),
       (6, 58, 'Earned', 7, 'Tích điểm từ đơn hàng #ORD202410070007', '2024-10-07 20:00:00'),
       (9, 98, 'Earned', 10, 'Tích điểm từ đơn hàng #ORD202410100010', '2024-10-10 20:00:00'),
       (1, 500, 'Bonus', NULL, 'Thưởng sinh nhật khách hàng', '2024-03-15 00:00:00'),
       (3, -1000, 'Redeemed', 3, 'Sử dụng điểm giảm giá đơn hàng', '2024-10-03 10:45:00');

-- ===============================
-- PROMOTION USAGE (Sử dụng khuyến mãi)
-- ===============================
INSERT INTO promotion_usage (promotion_id, customer_id, order_id, discount_amount, used_at)
VALUES (1, 4, 4, 35000, '2024-10-04 14:20:00'),
       (3, 3, 3, 100000, '2024-10-03 10:45:00');

-- ===============================
-- ORDER STATUS HISTORY (Lịch sử trạng thái đơn hàng)
-- ===============================
INSERT INTO order_status_history (order_id, old_status, new_status, note, changed_by, changed_at)
VALUES (1, 'Chờ xác nhận', 'CONFIRMED', 'Đơn hàng đã được xác nhận', 2, '2024-10-01 09:00:00'),
       (1, 'CONFIRMED', 'PREPARING', 'PREPARING hàng', 4, '2024-10-01 10:00:00'),
       (1, 'PREPARING', 'Đang giao', 'Đơn hàng đã xuất kho', 3, '2024-10-01 14:00:00'),
       (1, 'Đang giao', 'Hoàn thành', 'Giao hàng thành công', 3, '2024-10-02 10:00:00'),
       (2, 'Chờ xác nhận', 'CONFIRMED', 'Đơn hàng đã được xác nhận', 2, '2024-10-02 10:00:00'),
       (2, 'CONFIRMED', 'PREPARING', 'PREPARING hàng', 4, '2024-10-02 11:00:00'),
       (2, 'PREPARING', 'Đang giao', 'Đơn hàng đã xuất kho', 3, '2024-10-02 15:00:00'),
       (3, 'Chờ xác nhận', 'CONFIRMED', 'Đơn hàng đã được xác nhận', 2, '2024-10-03 11:00:00'),
       (3, 'CONFIRMED', 'PREPARING', 'PREPARING hàng', 4, '2024-10-03 13:00:00'),
       (3, 'PREPARING', 'Đang giao', 'Đơn hàng đã xuất kho', 4, '2024-10-03 16:00:00'),
       (3, 'Đang giao', 'Hoàn thành', 'Giao hàng thành công', 4, '2024-10-04 11:00:00');

-- ===============================
-- NOTIFICATIONS (Thông báo)
-- ===============================
INSERT INTO notification (target_type, customer_id, employee_id, title, content, type, reference_id, is_read,
                          created_at)
VALUES ('Customer', 1, NULL, 'Đơn hàng đã được xác nhận', 'Đơn hàng #ORD202410010001 của bạn đã được xác nhận', 'Order',
        1, TRUE, '2024-10-01 09:00:00'),
       ('Customer', 1, NULL, 'Đơn hàng đang được giao', 'Đơn hàng #ORD202410010001 đang trên đường giao đến bạn',
        'Order', 1, TRUE, '2024-10-01 14:00:00'),
       ('Customer', 1, NULL, 'Đơn hàng đã hoàn thành', 'Cảm ơn bạn đã mua hàng! Đơn #ORD202410010001 đã hoàn thành',
        'Order', 1, FALSE, '2024-10-02 10:00:00'),
       ('Customer', 2, NULL, 'Đơn hàng đang được giao', 'Đơn hàng #ORD202410020002 đang trên đường đến bạn', 'Order', 2,
        FALSE, '2024-10-02 15:00:00'),
       ('Employee', NULL, 3, 'Đơn hàng mới cần xử lý', 'Có đơn hàng mới #ORD202410080008 cần xác nhận', 'Order', 8,
        FALSE, '2024-10-08 16:45:00'),
       ('Employee', NULL, 3, 'Sản phẩm sắp hết hàng', 'Sản phẩm MD015 còn 10 cái, cần nhập thêm', 'Stock', NULL, TRUE,
        '2024-10-10 08:00:00'),
       ('Customer', 3, NULL, 'Khuyến mãi đặc biệt', 'Giảm 20% tất cả sản phẩm - Áp dụng đến 31/12', 'Promotion', 5,
        FALSE, '2024-10-01 00:00:00');

-- ===============================
-- BANNERS (Banner quảng cáo)
-- ===============================
INSERT INTO banner (title, image_url, link_url, position, display_order, start_date, end_date, is_active, created_by)
VALUES ('Sale 20% Tất Cả Sản Phẩm', '/images/banners/sale-20.jpg', '/promotion/sale-20', 'Home_Slider', 1, '2024-10-01',
        '2024-12-31', TRUE, 1),
       ('Máy Đo Huyết Áp Omron', '/images/banners/omron.jpg', '/products/category/may-do-suc-khoe', 'Home_Slider', 2,
        '2024-01-01', '2025-12-31', TRUE, 1),
       ('Miễn Phí Vận Chuyển', '/images/banners/freeship.jpg', '/promotion/freeship', 'Top', 1, '2024-01-01',
        '2025-12-31', TRUE, 1),
       ('Khẩu Trang Y Tế Chất Lượng', '/images/banners/mask.jpg', '/products/category/khau-trang', 'Sidebar', 1,
        '2024-01-01', '2025-12-31', TRUE, 2);

-- ===============================
-- BLOG POSTS (Bài viết)
-- ===============================
INSERT INTO blog_post (title, slug, content, excerpt, author_id, status, published_at)
VALUES ('Hướng dẫn sử dụng máy đo huyết áp tại nhà', 'huong-dan-su-dung-may-do-huyet-ap',
        '<p>Máy đo huyết áp là thiết bị y tế quan trọng giúp theo dõi sức khỏe tim mạch...</p>',
        'Cách sử dụng máy đo huyết áp chính xác tại nhà', 1, 'Published', '2024-09-01 10:00:00'),

       ('Top 5 thiết bị y tế cần thiết trong gia đình', 'top-5-thiet-bi-y-te-can-thiet',
        '<p>Mỗi gia đình nên trang bị những thiết bị y tế cơ bản để chăm sóc sức khỏe...</p>',
        'Những thiết bị y tế thiết yếu mọi nhà nên có', 2, 'Published', '2024-09-15 14:00:00'),

       ('Cách chọn khẩu trang y tế phù hợp', 'cach-chon-khau-trang-y-te-phu-hop',
        '<p>Khẩu trang y tế có nhiều loại khác nhau, tùy vào mục đích sử dụng...</p>',
        'Hướng dẫn chọn khẩu trang y tế đúng chuẩn', 1, 'Published', '2024-10-01 09:00:00');

-- ===============================
-- FAQ (Câu hỏi thường gặp)
-- ===============================
INSERT INTO faq (question, answer, category, display_order, is_active, created_by)
VALUES ('Làm thế nào để đặt hàng?',
        'Bạn có thể đặt hàng trực tuyến trên website hoặc gọi hotline 1900-xxxx để được hỗ trợ.', 'Đặt hàng', 1, TRUE,
        1),
       ('Thời gian giao hàng là bao lâu?', 'Thời gian giao hàng từ 2-5 ngày tùy khu vực. Nội thành TP.HCM: 1-2 ngày.',
        'Vận chuyển', 2, TRUE, 1),
       ('Tôi có thể đổi trả hàng không?',
        'Bạn có thể đổi trả trong vòng 7 ngày nếu sản phẩm còn nguyên tem, chưa qua sử dụng.', 'Đổi trả', 3, TRUE, 1),
       ('Các hình thức thanh toán nào được chấp nhận?', 'Chúng tôi chấp nhận COD, VNPAY, chuyển khoản ngân hàng.',
        'Thanh toán', 4, TRUE, 1),
       ('Làm thế nào để kiểm tra bảo hành?',
        'Vui lòng liên hệ hotline và cung cấp mã sản phẩm để được kiểm tra thông tin bảo hành.', 'Bảo hành', 5, TRUE,
        1);

-- ===============================
-- CONTACT MESSAGES (Tin nhắn liên hệ)
-- ===============================
INSERT INTO contact_message (customer_id, name, email, phone, subject, message, status, assigned_to, created_at)
VALUES (1, 'Nguyễn Thị Mai', 'mai.nguyen@gmail.com', '0912345678',
        'Hỏi về sản phẩm',
        'Cho tôi hỏi máy đo huyết áp Omron có bảo hành bao lâu?',
        'Resolved', 3, '2024-10-01 15:00:00'),
       (NULL, 'Trần Văn Bình', 'binh.tran@gmail.com', '0987654321',
        'Khiếu nại giao hàng',
        'Đơn hàng của tôi giao chậm 3 ngày so với dự kiến',
        'Processing', 3, '2024-10-05 10:00:00'),
       (4, 'Lê Văn Phát', 'phat.le@gmail.com', '0912345681',
        'Hỏi về khuyến mãi',
        'Mã giảm giá WELCOME10 có áp dụng cho tất cả sản phẩm không?',
        'New', NULL, '2024-10-08 14:00:00');
-- ===============================
-- EMPLOYEE SCHEDULE (Lịch làm việc)
-- ===============================
INSERT INTO employee_schedule (employee_id, work_date, shift, start_time, end_time, status)
VALUES (3, '2024-10-01', 'MORNING', '08:00:00', '12:00:00', 'COMPLETED'),
       (3, '2024-10-01', 'AFTERNOON', '13:00:00', '17:00:00', 'COMPLETED'),
       (4, '2024-10-01', 'AFTERNOON', '13:00:00', '17:00:00', 'COMPLETED'),
       (4, '2024-10-01', 'EVENING', '17:00:00', '21:00:00', 'COMPLETED'),
       (2, '2024-10-01', 'FULL_DAY', '08:00:00', '17:00:00', 'COMPLETED'),
       (3, '2024-10-02', 'FULL_DAY', '08:00:00', '17:00:00', 'COMPLETED'),
       (4, '2024-10-02', 'FULL_DAY', '08:00:00', '17:00:00', 'COMPLETED'),
       (2, '2024-10-02', 'MORNING', '08:00:00', '12:00:00', 'COMPLETED'),
       (2, '2024-10-02', 'AFTERNOON', '13:00:00', '17:00:00', 'COMPLETED');


-- ===============================
-- ATTENDANCE (Chấm công)
-- ===============================
INSERT INTO attendance (employee_id, check_in, check_out, work_hours, overtime_hours, status)
VALUES (3, '2024-10-01 07:55:00', '2024-10-01 17:10:00', 8.25, 0.25, 'ON_TIME'),
       (4, '2024-10-01 13:05:00', '2024-10-01 21:00:00', 7.92, 0, 'LATE'),
       (2, '2024-10-01 08:00:00', '2024-10-01 17:00:00', 8.0, 0, 'ON_TIME'),
       (3, '2024-10-02 08:10:00', '2024-10-02 17:05:00', 7.92, 0, 'LATE'),
       (4, '2024-10-02 07:50:00', '2024-10-02 17:30:00', 8.67, 0.67, 'ON_TIME'),
       (2, '2024-10-02 08:00:00', '2024-10-02 12:00:00', 4.0, 0, 'ON_TIME'),
       (2, '2024-10-02 13:00:00', '2024-10-02 17:00:00', 4.0, 0, 'ON_TIME'),
       (3, '2024-10-03 08:00:00', '2024-10-03 17:00:00', 8.0, 0, 'ON_TIME');


-- ===============================
-- PROMOTION CATEGORY (Áp dụng KM cho danh mục)
-- ===============================
INSERT INTO promotion_category (promotion_id, category_id)
VALUES (5, 1),
       (5, 2),
       (5, 3);

-- ===============================
-- PROMOTION PRODUCT (Áp dụng KM cho sản phẩm)
-- ===============================
INSERT INTO promotion_product (promotion_id, device_id)
VALUES (1, 'MD001'),
       (1, 'MD002'),
       (1, 'MD003'),
       (5, 'MD001'),
       (5, 'MD002'),
       (5, 'MD003'),
       (5, 'MD004'),
       (5, 'MD005'),
       (5, 'MD006'),
       (5, 'MD007'),
       (5, 'MD008'),
       (5, 'MD009'),
       (5, 'MD010'),
       (5, 'MD011'),
       (5, 'MD012'),
       (5, 'MD013'),
       (5, 'MD014'),
       (5, 'MD015'),
       (5, 'MD016'),
       (5, 'MD017'),
       (5, 'MD018'),
       (5, 'MD019'),
       (5, 'MD020');

-- ===============================
-- UPDATE STATISTICS (Cập nhật thống kê)
-- ===============================

-- Cập nhật số lần xem sản phẩm
UPDATE medical_device
SET view_count = 150
WHERE device_id = 'MD001';
UPDATE medical_device
SET view_count = 89
WHERE device_id = 'MD002';
UPDATE medical_device
SET view_count = 120
WHERE device_id = 'MD003';
UPDATE medical_device
SET view_count = 200
WHERE device_id = 'MD004';
UPDATE medical_device
SET view_count = 75
WHERE device_id = 'MD006';
UPDATE medical_device
SET view_count = 110
WHERE device_id = 'MD009';

-- Cập nhật số lượng đã bán
UPDATE medical_device
SET sold_count = 25
WHERE device_id = 'MD001';
UPDATE medical_device
SET sold_count = 18
WHERE device_id = 'MD002';
UPDATE medical_device
SET sold_count = 12
WHERE device_id = 'MD003';
UPDATE medical_device
SET sold_count = 45
WHERE device_id = 'MD004';
UPDATE medical_device
SET sold_count = 8
WHERE device_id = 'MD006';
UPDATE medical_device
SET sold_count = 15
WHERE device_id = 'MD009';

-- Cập nhật số lần sử dụng mã khuyến mãi
UPDATE promotion
SET used_count = 1
WHERE promotion_id = 1;
UPDATE promotion
SET used_count = 1
WHERE promotion_id = 3;

-- ===============================
-- ADDITIONAL SAMPLE DATA
-- ===============================

-- Thêm đánh giá cho nhiều sản phẩm hơn
INSERT INTO review (customer_id, device_id, order_id, rating, comment, is_verified_purchase, status, created_at)
VALUES (2, 'MD006', 2, 5, 'Máy xông rất tốt, con nhỏ dùng rất hiệu quả', TRUE, 'Approved', '2024-10-08 15:00:00'),
       (5, 'MD010', 5, 5, 'Ống nghe chất lượng tuyệt vời, âm thanh rõ ràng', TRUE, 'Approved', '2024-10-10 16:30:00'),
       (6, 'MD007', 7, 4, 'Máy đo cổ tay tiện lợi khi đi công tác', TRUE, 'Approved', '2024-10-13 10:00:00'),
       (8, 'MD003', 9, 5, 'Máy đo đường huyết chính xác, đo nhanh', TRUE, 'PENDING', '2024-10-14 14:00:00'),
       (8, 'MD012', 9, 4, 'Máy đo SpO2 nhỏ gọn, dễ mang theo', TRUE, 'Approved', '2024-10-14 14:15:00');

-- Thêm khách hàng mới (chưa mua hàng)
INSERT INTO customer (customer_code, username, password_hash, full_name, email, phone, date_of_birth, gender, status)
VALUES ('CUS011', 'customer11', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Dương Văn Yên',
        'yen.duong@gmail.com', '0912345688', '1996-07-15', 'Male', 'ACTIVE'),
       ('CUS012', 'customer12', '$2a$12$VAdnZPxy4cqUSSydZcAZTO4RRywHC2uBpNF9smx1hMsGBtOsI0PfO', 'Lý Thị Zin',
        'zin.ly@gmail.com', '0912345689', '1998-12-30', 'Female', 'ACTIVE');

-- Thêm đơn hàng gần đây
INSERT INTO `order` (order_code, customer_id, receiver_name, receiver_phone, receiver_address,
                     subtotal, shipping_fee, discount_amount, total_price, payment_method, payment_status,
                     status, assigned_to, confirmed_by, created_at)
VALUES ('ORD202410110011', 1, 'Nguyễn Thị Mai', '0912345678', '123 Lê Văn Việt, P.Tăng Nhơn Phú A, Q.9, TP.HCM',
        1150000, 30000, 0, 1180000, 'VNPAY', 'PAID', 'PREPARING', 3, 2, '2024-10-11 09:30:00'),
       ('ORD202410110012', 5, 'Hoàng Thị Quỳnh', '0912345682', '654 Xa lộ Hà Nội, P.Hiệp Phú, Q.9, TP.HCM', 2850000, 0,
        0, 2850000, 'COD', 'UNPAID', 'CONFIRMED', 4, 2, '2024-10-11 14:00:00'),
       ('ORD202410120013', 6, 'Võ Văn Sang', '0912345683', '987 Kha Vạn Cân, P.Linh Chiểu, Thủ Đức, TP.HCM', 450000,
        30000, 0, 480000, 'VNPAY', 'PAID', 'PENDING', NULL, NULL, '2024-10-12 10:15:00');

-- Chi tiết đơn hàng mới
INSERT INTO order_detail (order_id, device_id, device_name, quantity, unit_price)
VALUES (11, 'MD021', 'Máy massage mắt Beurer EM58', 1, 1150000),
       (12, 'MD022', 'Máy tạo ẩm y tế Philips HU4803', 1, 2850000),
       (13, 'MD023', 'Bộ dụng cụ sơ cứu gia đình', 1, 450000);

-- Thêm thông báo mới
INSERT INTO notification (target_type, customer_id, employee_id, title, content, type, reference_id, is_read,
                          created_at)
VALUES ('Customer', 5, NULL, 'Có sản phẩm mới bạn quan tâm', 'Máy massage mắt Beurer EM58 vừa về hàng!', 'System', NULL,
        FALSE, '2024-10-11 08:00:00'),
       ('Employee', NULL, 3, 'Đơn hàng mới cần xử lý', 'Có đơn hàng mới #ORD202410120013 cần xác nhận', 'Order', 13,
        FALSE, '2024-10-12 10:15:00'),
       ('Customer', 1, NULL, 'Đơn hàng đang được chuẩn bị', 'Đơn hàng #ORD202410110011 đang được chuẩn bị', 'Order', 11,
        FALSE, '2024-10-11 10:00:00');

-- Thêm bài viết blog mới
INSERT INTO blog_post (title, slug, content, excerpt, author_id, status, published_at)
VALUES ('Cách phòng tránh bệnh tim mạch hiệu quả', 'cach-phong-tranh-benh-tim-mach',
        '<p>Bệnh tim mạch là một trong những nguyên nhân gây tử vong hàng đầu...</p>',
        'Những điều cần biết để phòng tránh bệnh tim mạch', 2, 'Published', '2024-10-05 10:00:00'),

       ('Lợi ích của việc theo dõi sức khỏe hàng ngày', 'loi-ich-theo-doi-suc-khoe-hang-ngay',
        '<p>Việc theo dõi các chỉ số sức khỏe hàng ngày giúp phát hiện sớm bệnh tật...</p>',
        'Tại sao nên theo dõi sức khỏe thường xuyên', 1, 'Draft', NULL);

-- Thêm câu hỏi thường gặp
INSERT INTO faq (question, answer, category, display_order, is_active, created_by)
VALUES ('Sản phẩm có được bảo hành không?',
        'Tất cả sản phẩm đều được bảo hành theo chính sách của nhà sản xuất, từ 12-24 tháng tùy loại.', 'Bảo hành', 6,
        TRUE, 1),
       ('Tôi có thể mua trả góp không?',
        'Hiện tại chúng tôi chưa hỗ trợ trả góp. Bạn có thể thanh toán qua thẻ tín dụng.', 'Thanh toán', 7, TRUE, 1),
       ('Làm sao để tích điểm thưởng?',
        'Mỗi đơn hàng hoàn thành, bạn nhận 1 điểm cho mỗi 10,000 VNĐ. 1000 điểm = 1,000 VNĐ giảm giá.', 'Tích điểm', 8,
        TRUE, 1);

-- Cập nhật last_login cho nhân viên
UPDATE employee
SET last_login = '2024-10-12 08:00:00'
WHERE employee_id = 1;
UPDATE employee
SET last_login = '2024-10-12 08:15:00'
WHERE employee_id = 2;
UPDATE employee
SET last_login = '2024-10-12 08:30:00'
WHERE employee_id = 3;
UPDATE employee
SET last_login = '2024-10-12 09:00:00'
WHERE employee_id = 4;
UPDATE employee
SET last_login = '2024-10-12 08:45:00'
WHERE employee_id = 5;

-- Cập nhật last_login cho khách hàng
UPDATE customer
SET last_login = '2024-10-11 20:00:00'
WHERE customer_id = 1;
UPDATE customer
SET last_login = '2024-10-10 19:30:00'
WHERE customer_id = 2;
UPDATE customer
SET last_login = '2024-10-09 21:00:00'
WHERE customer_id = 3;
UPDATE customer
SET last_login = '2024-10-12 10:00:00'
WHERE customer_id = 6;

-- ===============================
-- VERIFY DATA INTEGRITY
-- ===============================

-- Kiểm tra tổng số bản ghi
SELECT 'Employees' as TableName, COUNT(*) as RecordCount
FROM employee
UNION ALL
SELECT 'Customers', COUNT(*)
FROM customer
UNION ALL
SELECT 'Categories', COUNT(*)
FROM category
UNION ALL
SELECT 'Brands', COUNT(*)
FROM brand
UNION ALL
SELECT 'Suppliers', COUNT(*)
FROM supplier
UNION ALL
SELECT 'Medical Devices', COUNT(*)
FROM medical_device
UNION ALL
SELECT 'Orders', COUNT(*)
FROM `order`
UNION ALL
SELECT 'Order Details', COUNT(*)
FROM order_detail
UNION ALL
SELECT 'Reviews', COUNT(*)
FROM review
UNION ALL
SELECT 'Promotions', COUNT(*)
FROM promotion
UNION ALL
SELECT 'Stock Imports', COUNT(*)
FROM stock_import
UNION ALL
SELECT 'Notifications', COUNT(*)
FROM notification
UNION ALL
SELECT 'Cart Items', COUNT(*)
FROM cart
UNION ALL
SELECT 'Wishlist Items', COUNT(*)
FROM wishlist;

-- ===============================
-- SAMPLE QUERIES FOR TESTING
-- ===============================

-- Kiểm tra top sản phẩm bán chạy
-- SELECT name, sold_count, view_count, price FROM medical_device ORDER BY sold_count DESC LIMIT 10;

-- Kiểm tra đơn hàng theo trạng thái
-- SELECT status, COUNT(*) as total_orders, SUM(total_price) as total_revenue FROM `order` GROUP BY status;

-- Kiểm tra khách hàng theo hạng
-- SELECT customer_tier, COUNT(*) as total_customers, AVG(total_spent) as avg_spent FROM customer GROUP BY customer_tier;

-- Kiểm tra nhân viên theo phòng ban
-- SELECT department, COUNT(*) as total_employees, AVG(salary) as avg_salary FROM employee WHERE status = 'Active' GROUP BY department;

-- Kiểm tra sản phẩm sắp hết hàng
-- SELECT device_id, name, stock_quantity, min_stock_level FROM medical_device WHERE stock_quantity <= min_stock_level;

-- =============================================
-- END OF SAMPLE DATA INSERTION
-- =============================================

-- Thông báo hoàn thành
SELECT CONCAT('Total Employees: ', COUNT(*)) as Summary
FROM employee
UNION ALL
SELECT CONCAT('Total Customers: ', COUNT(*))
FROM customer
UNION ALL
SELECT CONCAT('Total Products: ', COUNT(*))
FROM medical_device
UNION ALL
SELECT CONCAT('Total Orders: ', COUNT(*))
FROM `order`
UNION ALL
SELECT CONCAT('Total Revenue: ', FORMAT(SUM(total_price), 0), ' VNĐ')
FROM `order`
WHERE status = 'Hoàn thành';
-- =============================================
-- 1. Sửa columns cho phép NULL
ALTER TABLE customer
    MODIFY COLUMN password_hash VARCHAR(255) NULL,
    MODIFY COLUMN username VARCHAR(100) NULL;

-- 4. Update existing data
UPDATE customer
SET provider            = 'LOCAL',
    has_custom_password = TRUE
WHERE provider IS NULL
  AND password_hash IS NOT NULL;
-- =============================================
-- =============================================
-- KẾT THÚC SCRIPT
-- =============================================