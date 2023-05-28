package com.thirteenseven.workoutdiary.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.thirteenseven.workoutdiary.exception.UnauthorizedAttemptException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class NeedAdminInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(VerifyAuthorityInterceptor.class);

    @Override
    public boolean preHandle(
        HttpServletRequest request, 
        HttpServletResponse response, 
        Object handler
    ) throws Exception, UnauthorizedAttemptException {
        if (!request.isUserInRole("ADMIN"))
            throw new UnauthorizedAttemptException("Required admin");
        return true;
    }



    @Override
    public void postHandle(HttpServletRequest request, 
        HttpServletResponse response, 
        Object handler,
        @Nullable ModelAndView modelAndView
    ) throws Exception {
        // logger.info("posthandle... ");
    }



    /** We can handle error here */
    @Override
    public void afterCompletion(
        HttpServletRequest request, 
        HttpServletResponse response, 
        Object handler,
        @Nullable Exception ex
    ) throws Exception {
        
        if (ex != null) {
            logger.error("Error in interceptor..");
        }

        // logger.info("after completion... ");
    }
}
