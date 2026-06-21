package com.vuon.service;

import com.vuon.model.OtpCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service gửi email (OTP, thông báo đơn hàng)
 * Nếu app.email.enabled=false → chỉ log OTP ra console, không gửi thật
 */
@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Async
    public void sendOtpEmail(String to, String otp, OtpCode.Type type) {
        String subject = type == OtpCode.Type.verify
                ? "Xác thực tài khoản VƯƠN"
                : "Đặt lại mật khẩu VƯƠN";

        // Khi email disabled hoặc chưa cấu hình SMTP → log ra console để dev test
        if (!emailEnabled || mailSender == null) {
            log.info("========================================");
            log.info("[EMAIL DISABLED] To: {}", to);
            log.info("[EMAIL DISABLED] Subject: {}", subject);
            log.info("[EMAIL DISABLED] OTP CODE: {}", otp);
            log.info("========================================");
            return;
        }

        try {
            var msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

            String body = type == OtpCode.Type.verify
                    ? "Mã xác thực của bạn là: <b>" + otp + "</b>"
                    : "Mã đặt lại mật khẩu của bạn là: <b>" + otp + "</b>";

            helper.setText("""
                    <div style="font-family:sans-serif;padding:24px;max-width:480px">
                        <h2 style="color:#16a34a">🌱 VƯƠN App</h2>
                        <p>%s. Có hiệu lực trong <b>10 phút</b>.</p>
                        <p style="color:#666;font-size:12px">
                            Nếu bạn không yêu cầu điều này, hãy bỏ qua email này.
                        </p>
                    </div>
                    """.formatted(body), true);

            mailSender.send(msg);
            log.info("Đã gửi email {} tới {}", subject, to);
        } catch (Exception e) {
            log.error("Không gửi được email tới {}: {}", to, e.getMessage());
        }
    }
}
