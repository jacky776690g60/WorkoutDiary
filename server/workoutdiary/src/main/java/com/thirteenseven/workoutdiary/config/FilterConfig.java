/** ================================================================
| FilterConfig.java  --  WorkoutDiary/server/workoutdiary/src/main/java/com/thirteenseven/workoutdiary/config/FilterConfig.java
|
| Created by Jack on 05/09, 2024
| Copyright © 2024 jacktogon. All rights reserved.
================================================================= */
package com.thirteenseven.workoutdiary.config;
// ~~~~~~~~ standard ~~~~~~~~
import jakarta.servlet.Filter;
// ~~~~~~~~ Spring ~~~~~~~~
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
// ~~~~~~~~ Workout Diary ~~~~~~~~
import com.thirteenseven.workoutdiary.dao.UserRepository;
import com.thirteenseven.workoutdiary.filter.UserCookieFilter;
import com.thirteenseven.workoutdiary.filter.CORSFilter;
import com.thirteenseven.workoutdiary.utilities.JwtUtility;
import com.thirteenseven.workoutdiary.advice.FilterChainAdvisor;


@Configuration
public class FilterConfig {

    // ==============================
    // Dependency Injections
    // ==============================
    @Autowired
    JwtUtility          jwtUtils;
    
    @Autowired
    PasswordEncoder     encoder;
    
    @Autowired
	UserRepository      userRepo;
    
    @Autowired
    CORSFilter          corsFilter;
    
    @Autowired
    UserCookieFilter    userCookieFilter;
    
    @Autowired
    FilterChainAdvisor  filterChainAdvisor;


    // =====================================================
    // Registering
    // 
    // You cannot use '**' here
    // =====================================================
    @Bean
    public FilterRegistrationBean<FilterChainAdvisor> filterChainAdvisorRegistrationBean() {
        /** The order for this should be the lowest */
        return createRegistrationBean(filterChainAdvisor, -1, "*");
    }

    @Bean
    public FilterRegistrationBean<CORSFilter> corsFilterRegistration() {
        return createRegistrationBean(corsFilter, 0, "*");
    }

    @Bean
    public FilterRegistrationBean<UserCookieFilter> userCookieRegistrationBean() {
        return createRegistrationBean(userCookieFilter, 1, "/api/v1/*");
    }

    // =====================================================
    // Utility
    // =====================================================
    /**
     * 
     * @param <T>
     * @param filter
     * @param order
     * @param urlPatterns Cannot take '**'; use '*' and it will work on subsequent as well
     * @return
     */
    private <T extends Filter> FilterRegistrationBean<T> createRegistrationBean(T filter, int order, String... urlPatterns) {
        FilterRegistrationBean<T> registrationBean = 
            new FilterRegistrationBean<T>(filter);
        /** PathPatterns can take '**'; urlPatterns cannot, but it can take '*.abc' */
        registrationBean.addUrlPatterns(urlPatterns);
        registrationBean.setName(filter.getClass().getSimpleName());
        registrationBean.setOrder(order);
        return registrationBean;
    }

}