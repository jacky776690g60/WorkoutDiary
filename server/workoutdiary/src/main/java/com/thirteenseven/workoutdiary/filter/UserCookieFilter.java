package com.thirteenseven.workoutdiary.filter;

// ~~~~~~~~ standard ~~~~~~~~
import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ~~~~~~~~ Spring ~~~~~~~~
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;

// ~~~~~~~~ Workout Diary ~~~~~~~~
import com.thirteenseven.workoutdiary.dao.UserRepository;
import com.thirteenseven.workoutdiary.exception.UnauthorizedException;
import com.thirteenseven.workoutdiary.utilities.Constants;
import com.thirteenseven.workoutdiary.utilities.JwtUtility;

/**
 * Fornow, this class further check if the token needs renewal.
 */
@Component
public class UserCookieFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(UserCookieFilter.class);
    // =====================
    // dependency injections
    // =====================
    @Autowired
    private JwtUtility      jwtUtils;
    // @Autowired
    // private PasswordEncoder encoder;
    @Autowired
    private UserRepository  userRepo;

    // ==============================
    // Functions
    // ==============================
    @Override
    public void doFilter(
        ServletRequest  request,
        ServletResponse response,
        FilterChain     chain
    ) throws IOException, ServletException, UnauthorizedException {
        HttpServletRequest  httpRequest  = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        /** For reference */
        SecurityContextHolderAwareRequestWrapper securityRequest = 
            (SecurityContextHolderAwareRequestWrapper) request;

        try {
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    logger.debug("Found Cookie Name: " + cookie.getName() + ", Value: " + cookie.getValue());
                    if (Constants.Cookies.getToken().equals(cookie.getName())) {
                        String token = cookie.getValue();
                        if (jwtUtils.isTokenCloseToExpiry(token)) {
                            String newToken = jwtUtils.renewToken(token);
                            Cookie newCookie = new Cookie(
                                Constants.Cookies.getToken(), 
                                newToken
                            );
                            newCookie.setHttpOnly(true);
                            newCookie.setPath("/");
                            newCookie.setMaxAge(jwtUtils.getExpiryDuration());
                            httpResponse.addCookie(newCookie);
                            logger.info("JWT token renewed for user.");
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in processing JWT for renewal: ", e);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Error in token renewal.");
            return;
        }

        chain.doFilter(request, response); // proceed to the next filter element in the chain
        logger.debug("Finished " + this.getClass().getSimpleName());
    }


    // =====================================================
    // Utility
    // =====================================================
}
