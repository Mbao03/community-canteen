package com.example.kitchen.config;

import com.example.kitchen.utils.PathUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")

                .allowCredentials(true)

                .allowedOriginPatterns("*")

                .allowedMethods("GET", "POST", "PUT", "DELETE")

                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String winPath = PathUtils.getClassLoadRootPath() + "/src/main/resources/static/files/";


        registry.addResourceHandler("/files/**").addResourceLocations("file:" + winPath);
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}
