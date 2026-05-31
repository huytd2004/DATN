package vn.hust.huy.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class UploadConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        // Limit file size to 10MB and request size to 20MB
        return new MultipartConfigElement("", 10L * 1024L * 1024L, 20L * 1024L * 1024L, 0);
    }
}
