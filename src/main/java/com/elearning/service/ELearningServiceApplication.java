package com.elearning.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Main Application Class for E-Learning Service
 * Graduation Project - E-Learning Platform Backend
 * 
 * @author Your Name
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableWebSecurity
@EnableJpaRepositories(basePackages = "com.elearning.service.repositories")
@EntityScan(basePackages = "com.elearning.service.entities")
public class ELearningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ELearningServiceApplication.class, args);
        
        // Beautiful ASCII Art Welcome Message
        System.out.println("\n" + 
            "╔═══════════════════════════════════════════════════════════════════════╗\n" +
            "║                    🌟 SMART FLASHCARD PLATFORM 🌟                    ║\n" +
            "╠═══════════════════════════════════════════════════════════════════════╣\n" +
            "║                                                                       ║\n" +
            "║                 🎉 Xin chào, Kiên! Chào mừng trở lại! 🎉             ║\n" +
            "║                                                                       ║\n" +
            "║           🚀 Hãy tiếp tục hành trình chinh phục tri thức của bạn      ║\n" +
            "║              📚 Mỗi thẻ học là một bước tiến mới! 📚                ║\n" +
            "║                                                                       ║\n" +
            "║                    ✨ Học tập thông minh, thành công vững chắc ✨     ║\n" +
            "║                                                                       ║\n" +
            "╠═══════════════════════════════════════════════════════════════════════╣\n" +
            "║  🌐 Server: http://localhost:8080                                    ║\n" +
            "║  🗄️  Database: H2 Console - http://localhost:8080/h2-console         ║\n" +
            "║  📊 Status: ✅ READY FOR LEARNING!                                   ║\n" +
            "║  🎓 Version: 1.0.0 - Graduation Project                              ║\n" +
            "╚═══════════════════════════════════════════════════════════════════════╝\n");
    }
}
