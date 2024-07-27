/** ================================================================
| InterceptorConfig.java  --  WorkoutDiary/server/workoutdiary/src/main/java/com/thirteenseven/workoutdiary/config/InterceptorConfig.java
|
| Created by Jack on 05/18, 2024
| Copyright © 2024 jacktogon. All rights reserved.
================================================================= */
/**
 * This file will configure interceptors, which are specific to Spring MVC. 
 * Interceptors will be executed before reaching the controller methods.
 */
package com.thirteenseven.workoutdiary.config;
// ~~~~~~~~ SrpingBoot ~~~~~~~~
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// ~~~~~~~~ thirteen ~~~~~~~~
import com.thirteenseven.workoutdiary.interceptor.NeedAdminInterceptor;
import com.thirteenseven.workoutdiary.interceptor.CheckUserAuthorityInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    
    // =====================================================
    // Dependency Injections
    // =====================================================
    @Autowired
    private CheckUserAuthorityInterceptor checkUserAuthorityInterceptor;
    @Autowired
    private NeedAdminInterceptor          needAdminInterceptor;

    @Override
    public void addInterceptors(
        @NonNull InterceptorRegistry registry
    ) {
        registry.addInterceptor(needAdminInterceptor)
                /** PathPatterns can take '**'; urlPatterns cannot */
                .addPathPatterns( 
                    "/api/v1/**/delete*/**"
                ).excludePathPatterns(
                    "/api/v*/**/*softDeleteByIds"
                );
        registry.addInterceptor(checkUserAuthorityInterceptor)
                .addPathPatterns(
                    "/api/v*/user/**"
                ).excludePathPatterns(
                    "/api/v*/user/*softDeleteByIds"
                );
        
    }    
}
