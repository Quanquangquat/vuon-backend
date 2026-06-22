-- =====================================================================
-- VƯƠN – Migration cho bản nâng cấp Admin (sửa SP có ảnh, blog, banner)
-- Chạy 1 lần trong Supabase → SQL Editor. An toàn nếu chạy lại nhiều lần.
-- =====================================================================

-- 1) Mở rộng cột ảnh sang TEXT để chứa ảnh upload (base64 data URL).
--    Bắt buộc: Hibernate ddl-auto=update KHÔNG tự nới varchar(255) -> text.
ALTER TABLE products   ALTER COLUMN image TYPE TEXT;
ALTER TABLE blog_posts ALTER COLUMN image TYPE TEXT;

-- 2) Bảng banners (Hibernate sẽ tự tạo khi backend khởi động lại,
--    nhưng tạo sẵn ở đây cũng không sao nhờ IF NOT EXISTS).
CREATE TABLE IF NOT EXISTS banners (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  title       varchar(200),
  subtitle    text,
  image       text,
  link        varchar(300),
  sort_order  integer  NOT NULL DEFAULT 0,
  is_active   boolean  NOT NULL DEFAULT true,
  created_at  timestamp DEFAULT now(),
  updated_at  timestamp DEFAULT now()
);
