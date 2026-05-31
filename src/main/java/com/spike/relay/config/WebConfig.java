package com.spike.relay.config;

import com.spike.relay.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** 文件落盘根目录，与 StorageService 一致 */
    @Value("${storage.local.dir}")
    private String storageDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                // 登录接口、错误转发页、静态留痕图片、运营后台放行
                // /admin/** 不走用户 JWT，由 AdminController 用独立运营口令(X-Admin-Token)自行鉴权
                .excludePathPatterns("/auth/login", "/error", "/uploads/**", "/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把磁盘上的上传目录映射为 /uploads/** 可被 HTTP 访问
        String location = Paths.get(storageDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
