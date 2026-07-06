package com.stockdual.config;
 
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
@Configuration
public class WebConfig implements WebMvcConfigurer {
 
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /images/** requests to the local directory "uploaded-images/"
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploaded-images/");
    }
}
