package com.echommo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map đường dẫn URL "/uploads/**" vào thư mục "uploads/" trên ổ cứng
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}