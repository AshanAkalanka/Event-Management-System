package com.event.event_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadsDir = Path.of("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadsDir);
        } catch (Exception ignored) {
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsDir.toUri().toString());
    }
}
