package com.elearning.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
public class ELearningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ELearningServiceApplication.class, args);
        System.out.println("=================================================");
        System.out.println("🚀 E-Learning Service Started Successfully!");
        System.out.println("📚 Graduation Project - E-Learning Platform");
        System.out.println("🌐 Server running on: http://localhost:8080");
        System.out.println("=================================================");
    }
}
