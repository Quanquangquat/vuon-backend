package com.vuon.config;

import com.vuon.model.Product;
import com.vuon.model.Promotion;
import com.vuon.model.User;
import com.vuon.repository.ProductRepository;
import com.vuon.repository.PromotionRepository;
import com.vuon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedProducts();
        seedPromotions();
    }

    private void seedAdmin() {
        if (userRepository.count() > 0) return;
        User admin = new User();
        admin.setName("Admin VƯƠN");
        admin.setEmail("admin@vuon.vn");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setPhone("0900000000");
        admin.setRole(User.Role.admin);
        admin.setVerified(true);
        userRepository.save(admin);
        log.info("[SEEDER] Admin user created: admin@vuon.vn / admin123");
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;
        Object[][] data = {
            // name, category, price, image, description, difficulty, light, careLevel, rating, reviews, stock
            {"Bộ Kit Rau Muống Thủy Canh", Product.Category.vegetable, 89000, "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400",
             "Bộ kit trồng rau muống thủy canh mini, không cần đất, chỉ cần nước và ánh sáng. Phù hợp ban công chung cư.",
             Product.Difficulty.Dễ, "Sáng vừa", "Tưới nước mỗi 2-3 ngày, bổ sung phân bón 1 lần/tuần", 4.8, 124, 50},
            {"Kit Rau Cải Xanh Organic", Product.Category.vegetable, 95000, "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400",
             "Bộ kit trồng cải xanh theo phương pháp hữu cơ. Đất trồng đã được xử lý và trộn sẵn dinh dưỡng.",
             Product.Difficulty.Dễ, "Nhiều nắng", "Tưới 1 lần/ngày, bổ sung phân hữu cơ mỗi 2 tuần", 4.7, 98, 45},
            {"Bộ Kit Húng Quế Thơm", Product.Category.vegetable, 79000, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400",
             "Trồng húng quế tươi ngay tại bếp. Hương thơm tự nhiên, dùng nấu ăn mỗi ngày.",
             Product.Difficulty.Dễ, "Sáng vừa", "Tưới đều đặn, tránh để đất quá khô", 4.9, 156, 60},
            {"Kit Cà Chua Cherry Mini", Product.Category.vegetable, 129000, "https://images.unsplash.com/photo-1592841200221-a6898f307baa?w=400",
             "Bộ kit trồng cà chua cherry cho ban công. Ra quả sau 60-70 ngày, quả ngọt và đẹp mắt.",
             Product.Difficulty.Trung_bình, "Nhiều nắng", "Tưới 1-2 lần/ngày, cắm cọc khi cây cao 20cm", 4.6, 87, 30},
            {"Bộ Kit Hoa Cúc Mini", Product.Category.flower, 99000, "https://images.unsplash.com/photo-1490750967868-88df5691cc0f?w=400",
             "Kit trồng hoa cúc vạn thọ miniature. Ra hoa sau 30-40 ngày, màu sắc rực rỡ.",
             Product.Difficulty.Dễ, "Nhiều nắng", "Tưới mỗi 2 ngày, bón phân kali khi chuẩn bị ra hoa", 4.5, 73, 40},
            {"Kit Hoa Lavender Tím", Product.Category.flower, 149000, "https://images.unsplash.com/photo-1499002238440-d264edd596ec?w=400",
             "Trồng hoa lavender thơm ngát ngay tại nhà. Có tác dụng thư giãn và làm thơm phòng tự nhiên.",
             Product.Difficulty.Trung_bình, "Nhiều nắng", "Tưới ít, thích hợp đất thoát nước tốt", 4.7, 112, 25},
            {"Bộ Kit Combo Rau Sạch 3-1", Product.Category.combo, 199000, "https://images.unsplash.com/photo-1466637574441-749b8f19452f?w=400",
             "Combo 3 loại rau: cải xanh, xà lách và húng quế. Đủ rau sạch dùng cho cả tuần.",
             Product.Difficulty.Dễ, "Sáng vừa", "Theo dõi từng loại cây, tưới đều mỗi ngày", 4.8, 201, 35},
            {"Kit Combo Hoa & Rau Thơm", Product.Category.combo, 249000, "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400",
             "Bộ đôi hoa cúc và rau thơm hỗn hợp. Vừa đẹp vừa thực dụng cho không gian sống.",
             Product.Difficulty.Dễ, "Sáng vừa", "Kết hợp chăm sóc linh hoạt theo loại cây", 4.6, 89, 20},
            {"Chậu Thông Minh Tự Tưới", Product.Category.accessory, 159000, "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=400",
             "Chậu cây có hệ thống tự tưới nước. Bình chứa nước 500ml, tự động cấp ẩm cho đất trong 2 tuần.",
             Product.Difficulty.Dễ, "Linh hoạt", "Đổ nước vào bình chứa 2 tuần/lần", 4.4, 67, 30},
            {"Đèn LED Trồng Cây Indoor", Product.Category.accessory, 299000, "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400",
             "Đèn LED spectrum đầy đủ cho cây phát triển trong nhà không có ánh sáng mặt trời.",
             Product.Difficulty.Dễ, "Đèn LED", "Bật đèn 14-16 tiếng/ngày", 4.5, 45, 20},
            {"Phân Bón Hữu Cơ Liquid", Product.Category.accessory, 69000, "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400",
             "Phân bón hữu cơ dạng lỏng, pha loãng tưới trực tiếp. An toàn cho rau ăn lá.",
             Product.Difficulty.Dễ, "Không cần", "Tưới phân 1 lần/tuần, pha 5ml/1 lít nước", 4.7, 188, 100},
            {"Bộ Dụng Cụ Làm Vườn Mini", Product.Category.accessory, 119000, "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400",
             "Set 5 dụng cụ: xẻng nhỏ, cào xới đất, bình tưới, kéo cắt và găng tay. Thiết kế gọn nhẹ.",
             Product.Difficulty.Dễ, "Không cần", "Vệ sinh sau mỗi lần dùng", 4.6, 92, 40}
        };

        for (Object[] row : data) {
            Product p = Product.builder()
                .name((String) row[0])
                .category((Product.Category) row[1])
                .price((int) row[2])
                .image((String) row[3])
                .description((String) row[4])
                .difficulty((Product.Difficulty) row[5])
                .light((String) row[6])
                .careLevel((String) row[7])
                .rating((Double) row[8])
                .reviewsCount((int) row[9])
                .stock((int) row[10])
                .inStock((int) row[10] > 0)
                .isActive(true)
                .build();
            productRepository.save(p);
        }
        log.info("[SEEDER] {} products created", data.length);
    }

    private void seedPromotions() {
        if (promotionRepository.count() > 0) return;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusMonths(3);

        Object[][] promos = {
            // code, discount, type, description, minOrder, maxUses
            {"VUON10", 10, "percent", "Giảm 10% cho đơn hàng đầu tiên", 0, 1000},
            {"VUON20", 20, "percent", "Giảm 20% cho đơn từ 200k", 200000, 500},
            {"FREESHIP", 30000, "fixed", "Miễn phí vận chuyển cho mọi đơn hàng", 0, 2000},
            {"SUMMER25", 25, "percent", "Ưu đãi hè: giảm 25% đơn từ 300k", 300000, 300},
        };

        for (Object[] row : promos) {
            Promotion p = new Promotion();
            p.setCode((String) row[0]);
            p.setTitle("Mã " + row[0]);
            p.setDiscount((int) row[1]);
            p.setType(Promotion.Type.valueOf((String) row[2]));
            p.setDescription((String) row[3]);
            p.setMinOrder((int) row[4]);
            p.setMaxUses((int) row[5]);
            p.setUsedCount(0);
            p.setValidFrom(now);
            p.setValidUntil(end);
            p.setActive(true);
            promotionRepository.save(p);
        }
        log.info("[SEEDER] {} promotions created", promos.length);
    }
}
