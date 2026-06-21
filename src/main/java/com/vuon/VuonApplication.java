package com.vuon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * VƯƠN App - Điểm khởi chạy ứng dụng Spring Boot
 */
@SpringBootApplication
public class VuonApplication {

    public static void main(String[] args) {
        SpringApplication.run(VuonApplication.class, args);
        System.out.println("""

                🌱 ===================================
                   VƯƠN Backend đang chạy!
                   URL: http://localhost:5000
                   API: http://localhost:5000/api
                🌱 ===================================
                """);
    }
}
