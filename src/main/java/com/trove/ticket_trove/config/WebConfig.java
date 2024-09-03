//package com.trove.ticket_trove.config.cors;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:3000") // React 개발 서버 도메인
//                .allowedMethods("GET", "POST", "PATCH", "DELETE")
//                .allowedHeaders("*")
//                .allowCredentials(true);
//    }
//
//}
