package com.thirteenseven.workoutdiary.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.thirteenseven.workoutdiary.interceptor.NeedAdminInterceptor;
import com.thirteenseven.workoutdiary.interceptor.VerifyAuthorityInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    
    @Autowired
    private VerifyAuthorityInterceptor verifyAuthorityInterceptor;
    @Autowired
    private NeedAdminInterceptor needAdminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(needAdminInterceptor)
                .addPathPatterns(
                    "/api/v1/**/delete*/**"
                );
        registry.addInterceptor(verifyAuthorityInterceptor)
                .addPathPatterns(
                    "/api/v1/user/**",
                    "/api/v1/userRecord/**",
                    "/api/v1/exercise/add",
                    "/api/v1/exerciseRecord/get*"
                ).excludePathPatterns(
                    "/api/v1/user/getall",
                    "/api/v1/userRecord/getall"
                );
        
    }    
}
