-- ============================================================
-- VƯƠN APP - MySQL Database Schema
-- Chạy trong MySQL Workbench: File > Open SQL Script > chọn file này > Execute (⚡)
-- Hoặc terminal: mysql -u root -p < database.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS vuon_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE vuon_db;

-- ============================================================
-- BẢNG 1: USERS - Người dùng
-- ============================================================
CREATE TABLE users (
  id            CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
  name          VARCHAR(100) NOT NULL,
  email         VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  phone         VARCHAR(20),
  avatar        TEXT,
  role          VARCHAR(10)  NOT NULL DEFAULT 'user' CHECK (role IN ('user', 'admin')),
  is_verified   BOOLEAN      NOT NULL DEFAULT FALSE,
  total_orders  INT          NOT NULL DEFAULT 0,
  total_spent   BIGINT       NOT NULL DEFAULT 0,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role  ON users(role);

-- ============================================================
-- BẢNG 2: OTP_CODES - Mã xác thực OTP
-- ============================================================
CREATE TABLE otp_codes (
  id         CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
  email      VARCHAR(150) NOT NULL,
  code       VARCHAR(6)   NOT NULL,
  type       VARCHAR(20)  NOT NULL DEFAULT 'verify' CHECK (type IN ('verify', 'reset_password')),
  expires_at DATETIME     NOT NULL,
  used       BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_otp_email ON otp_codes(email);

-- ============================================================
-- BẢNG 3: PRODUCTS - Sản phẩm
-- ============================================================
CREATE TABLE products (
  id            CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
  name          VARCHAR(200) NOT NULL,
  category      VARCHAR(20)  NOT NULL CHECK (category IN ('vegetable', 'flower', 'combo', 'accessory')),
  price         INT          NOT NULL CHECK (price >= 0),
  image         TEXT,
  description   TEXT,
  difficulty    VARCHAR(20)  CHECK (difficulty IN ('Dễ', 'Trung bình', 'Khó')),
  light         VARCHAR(50)  CHECK (light IN ('Ít ánh sáng', 'Ánh sáng trung bình', 'Nhiều ánh sáng')),
  care_level    VARCHAR(200),
  rating        DECIMAL(2,1) NOT NULL DEFAULT 0 CHECK (rating BETWEEN 0 AND 5),
  reviews_count INT          NOT NULL DEFAULT 0,
  in_stock      BOOLEAN      NOT NULL DEFAULT TRUE,
  stock         INT          NOT NULL DEFAULT 0,
  is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_category  ON products(category);
CREATE INDEX idx_products_in_stock  ON products(in_stock);
CREATE INDEX idx_products_is_active ON products(is_active);

-- ============================================================
-- BẢNG 4: REVIEWS - Đánh giá sản phẩm
-- ============================================================
CREATE TABLE reviews (
  id         CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  product_id CHAR(36) NOT NULL,
  user_id    CHAR(36) NOT NULL,
  rating     INT      NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment    TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_review_product_user (product_id, user_id),
  CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  CONSTRAINT fk_reviews_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE
);

CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_user    ON reviews(user_id);

-- ============================================================
-- BẢNG 5: CART_ITEMS - Giỏ hàng
-- ============================================================
CREATE TABLE cart_items (
  id         CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  user_id    CHAR(36) NOT NULL,
  product_id CHAR(36) NOT NULL,
  quantity   INT      NOT NULL DEFAULT 1 CHECK (quantity > 0),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_cart_user_product (user_id, product_id),
  CONSTRAINT fk_cart_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
  CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_cart_user ON cart_items(user_id);

-- ============================================================
-- BẢNG 6: FAVORITES - Yêu thích
-- ============================================================
CREATE TABLE favorites (
  id         CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  user_id    CHAR(36) NOT NULL,
  product_id CHAR(36) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_fav_user_product (user_id, product_id),
  CONSTRAINT fk_fav_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
  CONSTRAINT fk_fav_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_favorites_user ON favorites(user_id);

-- ============================================================
-- BẢNG 7: PROMOTIONS - Mã khuyến mãi
-- ============================================================
CREATE TABLE promotions (
  id          CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
  title       VARCHAR(200) NOT NULL,
  code        VARCHAR(50)  NOT NULL UNIQUE,
  discount    INT          NOT NULL DEFAULT 0 CHECK (discount BETWEEN 0 AND 100),
  type        VARCHAR(10)  NOT NULL DEFAULT 'percent' CHECK (type IN ('percent', 'fixed', 'freeship')),
  description TEXT,
  min_order   INT          NOT NULL DEFAULT 0,
  max_uses    INT,
  used_count  INT          NOT NULL DEFAULT 0,
  valid_from  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  valid_until DATETIME     NOT NULL,
  is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_promotions_code      ON promotions(code);
CREATE INDEX idx_promotions_is_active ON promotions(is_active);

-- ============================================================
-- BẢNG 8: ORDERS - Đơn hàng
-- ============================================================
CREATE TABLE orders (
  id               CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
  user_id          CHAR(36)     NOT NULL,
  subtotal         INT          NOT NULL DEFAULT 0,
  discount_amount  INT          NOT NULL DEFAULT 0,
  shipping_fee     INT          NOT NULL DEFAULT 0,
  total            INT          NOT NULL DEFAULT 0,
  status           VARCHAR(20)  NOT NULL DEFAULT 'processing'
                   CHECK (status IN ('processing', 'shipping', 'completed', 'cancelled')),
  payment_method   VARCHAR(30)  NOT NULL DEFAULT 'COD'
                   CHECK (payment_method IN ('COD', 'bank_transfer', 'e_wallet', 'momo', 'zalopay')),
  payment_status   VARCHAR(20)  NOT NULL DEFAULT 'pending'
                   CHECK (payment_status IN ('pending', 'paid', 'failed', 'refunded')),
  promotion_id     CHAR(36),
  shipping_name    VARCHAR(100) NOT NULL,
  shipping_phone   VARCHAR(20)  NOT NULL,
  shipping_address TEXT         NOT NULL,
  shipping_note    TEXT,
  created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_orders_user      FOREIGN KEY (user_id)      REFERENCES users(id)      ON DELETE SET NULL,
  CONSTRAINT fk_orders_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id)
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status  ON orders(status);
CREATE INDEX idx_orders_created ON orders(created_at DESC);

-- ============================================================
-- BẢNG 9: ORDER_ITEMS - Chi tiết đơn hàng
-- ============================================================
CREATE TABLE order_items (
  id            CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
  order_id      CHAR(36)     NOT NULL,
  product_id    CHAR(36),
  product_name  VARCHAR(200) NOT NULL,
  product_image TEXT,
  price         INT          NOT NULL,
  quantity      INT          NOT NULL CHECK (quantity > 0),
  subtotal      INT          NOT NULL,
  CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
  CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

CREATE INDEX idx_order_items_order   ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- ============================================================
-- BẢNG 10: BLOG_POSTS - Bài viết blog
-- ============================================================
CREATE TABLE blog_posts (
  id           CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
  title        VARCHAR(300) NOT NULL,
  slug         VARCHAR(350) NOT NULL UNIQUE,
  excerpt      TEXT,
  content      TEXT,
  image        TEXT,
  author       VARCHAR(100) NOT NULL DEFAULT 'Admin VƯƠN',
  category     VARCHAR(50),
  tags         JSON,
  views        INT          NOT NULL DEFAULT 0,
  is_published BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_blog_slug      ON blog_posts(slug);
CREATE INDEX idx_blog_category  ON blog_posts(category);
CREATE INDEX idx_blog_published ON blog_posts(is_published);

-- ============================================================
-- BẢNG 11: COMMUNITY_POSTS - Bài viết cộng đồng
-- ============================================================
CREATE TABLE community_posts (
  id         CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  user_id    CHAR(36) NOT NULL,
  content    TEXT     NOT NULL,
  image      TEXT,
  likes      INT      NOT NULL DEFAULT 0,
  is_visible BOOLEAN  NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_community_posts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_community_user    ON community_posts(user_id);
CREATE INDEX idx_community_created ON community_posts(created_at DESC);

-- ============================================================
-- BẢNG 12: COMMUNITY_LIKES - Lượt thích bài cộng đồng
-- ============================================================
CREATE TABLE community_likes (
  id      CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  post_id CHAR(36) NOT NULL,
  user_id CHAR(36) NOT NULL,
  UNIQUE KEY uq_like_post_user (post_id, user_id),
  CONSTRAINT fk_likes_post FOREIGN KEY (post_id) REFERENCES community_posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id)           ON DELETE CASCADE
);

-- ============================================================
-- BẢNG 13: COMMUNITY_COMMENTS - Bình luận bài cộng đồng
-- ============================================================
CREATE TABLE community_comments (
  id         CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  post_id    CHAR(36) NOT NULL,
  user_id    CHAR(36) NOT NULL,
  content    TEXT     NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES community_posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id)           ON DELETE CASCADE
);

CREATE INDEX idx_comments_post ON community_comments(post_id);

-- ============================================================
-- BẢNG 14: PLANT_JOURNAL - Nhật ký cây trồng
-- ============================================================
CREATE TABLE plant_journal (
  id            CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
  user_id       CHAR(36)     NOT NULL,
  plant_name    VARCHAR(100) NOT NULL,
  notes         TEXT,
  image         TEXT,
  health_status VARCHAR(20)  DEFAULT 'good' CHECK (health_status IN ('good', 'warning', 'danger')),
  planted_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_journal_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_journal_user ON plant_journal(user_id);

-- ============================================================
-- BẢNG 15: WATERING_REMINDERS - Nhắc nhở tưới cây
-- ============================================================
CREATE TABLE watering_reminders (
  id             CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
  user_id        CHAR(36)     NOT NULL,
  plant_name     VARCHAR(100) NOT NULL,
  frequency      INT          NOT NULL DEFAULT 1 CHECK (frequency > 0),
  frequency_unit VARCHAR(10)  NOT NULL DEFAULT 'day' CHECK (frequency_unit IN ('day', 'week')),
  last_watered   DATETIME,
  next_water     DATETIME,
  is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_reminders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_reminders_user ON watering_reminders(user_id);

-- ============================================================
-- BẢNG 16: FAQS - Câu hỏi thường gặp
-- ============================================================
CREATE TABLE faqs (
  id         CHAR(36)    PRIMARY KEY DEFAULT (UUID()),
  question   TEXT        NOT NULL,
  answer     TEXT        NOT NULL,
  category   VARCHAR(50) NOT NULL DEFAULT 'general',
  sort_order INT         NOT NULL DEFAULT 0,
  is_active  BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- BẢNG 17: AI_DIAGNOSES - Lịch sử chẩn đoán bệnh cây bằng AI
-- ============================================================
CREATE TABLE ai_diagnoses (
  id          CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
  user_id     CHAR(36),
  image_url   TEXT,
  disease     VARCHAR(200),
  confidence  INT,
  solution    TEXT,
  description TEXT,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_diagnoses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_diagnoses_user ON ai_diagnoses(user_id);

-- ============================================================
-- TRIGGER: Tự động cập nhật rating sản phẩm khi có review
-- (updated_at tự động cập nhật qua ON UPDATE CURRENT_TIMESTAMP)
-- ============================================================

DELIMITER $$

CREATE TRIGGER trg_update_rating_insert
  AFTER INSERT ON reviews
  FOR EACH ROW
BEGIN
  UPDATE products
  SET
    rating        = (SELECT ROUND(AVG(rating), 1) FROM reviews WHERE product_id = NEW.product_id),
    reviews_count = (SELECT COUNT(*)               FROM reviews WHERE product_id = NEW.product_id)
  WHERE id = NEW.product_id;
END$$

CREATE TRIGGER trg_update_rating_update
  AFTER UPDATE ON reviews
  FOR EACH ROW
BEGIN
  UPDATE products
  SET
    rating        = (SELECT ROUND(AVG(rating), 1) FROM reviews WHERE product_id = NEW.product_id),
    reviews_count = (SELECT COUNT(*)               FROM reviews WHERE product_id = NEW.product_id)
  WHERE id = NEW.product_id;
END$$

CREATE TRIGGER trg_update_rating_delete
  AFTER DELETE ON reviews
  FOR EACH ROW
BEGIN
  UPDATE products
  SET
    rating        = COALESCE((SELECT ROUND(AVG(rating), 1) FROM reviews WHERE product_id = OLD.product_id), 0),
    reviews_count = (SELECT COUNT(*) FROM reviews WHERE product_id = OLD.product_id)
  WHERE id = OLD.product_id;
END$$

DELIMITER ;

-- ============================================================
-- DỮ LIỆU MẪU (Seed Data)
-- ============================================================

-- Admin user (password: admin123)
INSERT INTO users (id, name, email, password_hash, phone, role, is_verified) VALUES
  (UUID(), 'Admin VƯƠN', 'admin@vuon.vn', '$2a$10$rBV2JDeWW3.vKmBCxNfWg.kgBBdHsNhUfhXiVFm/1VmhNeOPRWtaS', '0900000000', 'admin', TRUE);

-- Demo users (password: user123)
INSERT INTO users (id, name, email, password_hash, phone, role, is_verified, total_orders, total_spent) VALUES
  (UUID(), 'Nguyễn Văn A', 'nguyenvana@email.com', '$2a$10$gywGzovXeFR04Xl7IHiYbO9ZxWqP3pB5jj.sDzf1DXXuv1Zh0P7gi', '0901234567', 'user', TRUE, 12, 2500000),
  (UUID(), 'Trần Thị B',   'tranthib@email.com',   '$2a$10$gywGzovXeFR04Xl7IHiYbO9ZxWqP3pB5jj.sDzf1DXXuv1Zh0P7gi', '0912345678', 'user', TRUE, 5,  850000);

-- Sản phẩm mẫu
INSERT INTO products (id, name, category, price, image, description, difficulty, light, care_level, rating, reviews_count, in_stock, stock) VALUES
  (UUID(), 'Rau cải xanh',        'vegetable', 120000, 'https://tse1.mm.bing.net/th/id/OIP.YMyyO4B2E7JowCCN-rgPEwHaFj?rs=1&pid=ImgDetMain&o=7&rm=3', 'Rau cải xanh tươi, dễ trồng, phù hợp cho người mới bắt đầu', 'Dễ', 'Nhiều ánh sáng', 'Tưới nước 2 lần/ngày', 4.5, 128, TRUE, 50),
  (UUID(), 'Cà chua bi',           'vegetable', 120000, 'https://images.unsplash.com/photo-1592841200221-a6898f307baa?w=400', 'Cà chua bi ngọt, năng suất cao', 'Trung bình', 'Nhiều ánh sáng', 'Tưới nước 1 lần/ngày, bón phân định kỳ', 4.8, 95, TRUE, 30),
  (UUID(), 'Hoa hồng',             'flower',    120000, 'https://img.thuthuatphanmem.vn/uploads/2018/09/24/hinh-anh-hoa-hong-dep-nhat_053955504.jpg', 'Hoa hồng đỏ thắm, thơm ngát', 'Khó', 'Nhiều ánh sáng', 'Cần chăm sóc kỹ lưỡng', 4.9, 203, TRUE, 20),
  (UUID(), 'Hoa oải hương',        'flower',    120000, 'https://charsawfarms.com/cdn/shop/files/PurpleBouquetlavender2.jpg', 'Hoa oải hương tím, hương thơm dễ chịu', 'Dễ', 'Nhiều ánh sáng', 'Ít cần chăm sóc', 4.6, 87, TRUE, 45),
  (UUID(), 'Combo cây rau gia vị', 'combo',     120000, 'https://images.unsplash.com/photo-1466692476868-aef1dfb1e735?w=400', 'Bộ 5 loại rau gia vị: húng quế, rau mùi, ngò gai, rau húng, tía tô', 'Dễ', 'Ánh sáng trung bình', 'Tưới nước đều đặn', 4.7, 156, TRUE, 25),
  (UUID(), 'Phân bón hữu cơ',      'accessory', 10000,  'https://th.bing.com/th/id/R.6c286dba498a4c368b9da7b62e2e04a6?rik=HGOrgHSmda8Yqg&pid=ImgRaw&r=0', 'Phân bón hữu cơ cho cây trồng', 'Dễ', 'Ít ánh sáng', 'Không cần chăm sóc', 4.4, 67, TRUE, 100),
  (UUID(), 'Rau diếp xanh',        'vegetable', 120000, 'https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=400', 'Rau diếp tươi ngon, giàu vitamin', 'Dễ', 'Ánh sáng trung bình', 'Tưới nước 2 lần/ngày', 4.3, 45, TRUE, 60),
  (UUID(), 'Hoa cúc vàng',         'flower',    120000, 'https://media.chuabavang.com/files/tu_chinh/2021/12/28/hoa-cuc-vang-clb-cuc-vang-chua-ba-vang-0839.jpg', 'Hoa cúc vàng rực rỡ, dễ trồng', 'Dễ', 'Nhiều ánh sáng', 'Tưới nước vừa phải', 4.5, 92, TRUE, 35);

-- Khuyến mãi mẫu
INSERT INTO promotions (id, title, code, discount, type, description, min_order, valid_until) VALUES
  (UUID(), 'Giảm 20% cho đơn hàng đầu tiên', 'FIRSTORDER', 20,    'percent', 'Áp dụng cho khách hàng mới', 100000, DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (UUID(), 'Miễn phí vận chuyển',             'FREESHIP',    0,    'freeship','Cho đơn hàng từ 200k',        200000, DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (UUID(), 'Giảm 50k',                        'VUON50K',    50000, 'fixed',   'Giảm ngay 50.000đ',           150000, DATE_ADD(NOW(), INTERVAL 15 DAY));

-- Blog posts mẫu
INSERT INTO blog_posts (id, title, slug, excerpt, image, author, category) VALUES
  (UUID(), 'Cách trồng cà chua bi trên ban công',        'cach-trong-ca-chua-bi-tren-ban-cong', 'Hướng dẫn chi tiết cách trồng cà chua bi tại nhà cho người mới bắt đầu...', 'https://images.unsplash.com/photo-1464226184884-fa280b87c399?w=600', 'Chuyên gia Vườn', 'Hướng dẫn'),
  (UUID(), '10 loại cây dễ trồng nhất cho người bận rộn', '10-loai-cay-de-trong-nhat',           'Những loại cây không cần chăm sóc nhiều vẫn cho năng suất cao...',           'https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=600', 'Admin Vườn',      'Tips'),
  (UUID(), 'Chẩn đoán và xử lý bệnh vàng lá',           'chan-doan-xu-ly-benh-vang-la',         'Nguyên nhân và cách khắc phục tình trạng lá cây bị vàng...',                 'https://images.unsplash.com/photo-1558904541-efa843a96f01?w=600', 'Chuyên gia Vườn', 'Bệnh cây');

-- FAQs mẫu
INSERT INTO faqs (id, question, answer, category, sort_order) VALUES
  (UUID(), 'Bộ kit VƯƠN gồm những gì?',              'Bộ kit VƯƠN gồm: chậu cây nhỏ gọn, viên nén xơ dừa, hạt giống, hướng dẫn trồng và quyền truy cập ứng dụng AI.', 'product',  1),
  (UUID(), 'Tôi có cần kinh nghiệm trồng cây không?', 'Không cần! Bộ kit và AI tư vấn sẽ hướng dẫn bạn từng bước, phù hợp cho người mới bắt đầu.',                      'product',  2),
  (UUID(), 'Ứng dụng AI có miễn phí không?',          'Ứng dụng AI đi kèm miễn phí khi mua bộ kit. Bạn có thể chẩn đoán bệnh cây và nhận tư vấn chăm sóc không giới hạn.', 'ai',    3),
  (UUID(), 'Thời gian giao hàng mất bao lâu?',        'Thông thường 2-3 ngày làm việc. Chúng tôi hỗ trợ giao hàng toàn quốc.',                                            'shipping', 4),
  (UUID(), 'Tôi có thể đổi trả hàng không?',          'Bạn có thể đổi trả trong vòng 7 ngày nếu sản phẩm lỗi hoặc không đúng mô tả.',                                     'policy',   5);
