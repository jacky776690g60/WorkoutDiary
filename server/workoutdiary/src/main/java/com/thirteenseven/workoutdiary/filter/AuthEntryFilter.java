/** ================================================================
| AuthTokenFilter.java  --  WorkoutDiary/server/workoutdiary/src/main/java/com/thirteenseven/workoutdiary/filter/AuthTokenFilter.java
|
| Created by Jack on 05/08, 2024
| Copyright © 2024 jacktogon. All rights reserved.
================================================================= */
package com.thirteenseven.workoutdiary.filter;

// ~~~~~~~~ standard ~~~~~~~~
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// ~~~~~~~~ Spring ~~~~~~~~
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
// ~~~~~~~~ Workout Diary ~~~~~~~~
import com.thirteenseven.workoutdiary.exception.CookieHeaderMismatchException;
import com.thirteenseven.workoutdiary.exception.InvalidJwtException;
import com.thirteenseven.workoutdiary.payload.response.FilterResponse;
import com.thirteenseven.workoutdiary.service.MUserDetailsService;
import com.thirteenseven.workoutdiary.utilities.Constants;
import com.thirteenseven.workoutdiary.utilities.JwtUtility;
import com.thirteenseven.workoutdiary.config.WebSecurityConfig;


/** 
 * This class mostly check the validity of JWT (Expired? Correct Format?), but
 * it DOES NOT check password.
 * </br></br>
 * The password verification typically happens earlier in the authentication 
 * process, often during the login phase where user credentials are initially 
 * provided and verified.
 * </br></br>
 * It is intended to be part of {@code Spring Security framework}, and should 
 * be added in {@link WebSecurityConfig}.
 * @note
 * This filter runs for every RESTful request. Verifying email from both cookie and token.
 */
@Component
public class AuthEntryFilter extends OncePerRequestFilter {

    private static final Logger       logger        = LoggerFactory.getLogger(AuthEntryFilter.class);
    private static final List<String> excluded_URLs = 
        Arrays.asList(
            "/api/auth/signin", 
            "/api/auth/signup"
        );

    // ===========================================================
    // Dependency Injection
    // ===========================================================
    @Autowired
    private JwtUtility            jwtUtils;
    @Autowired
    private MUserDetailsService   userDetailsService;

    // ===========================================================
    // Overridden Functions
    // ===========================================================
    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest  request, 
        @NonNull HttpServletResponse response, 
        @NonNull FilterChain         chain
    ) throws ServletException, IOException {
        logRequestDetails(request);
        logHttpServletRequestDetails((HttpServletRequest) request);
        /** JWT got from Header's Authentication */
        String clientJWT  = jwtUtils.parseJwt(request);
        // String authorizationHeader = request.getHeader("Authorization");
        // System.out.println("Authorization Header: " + authorizationHeader);

        String requestURI = request.getRequestURI();
        logger.debug("Processing request URI: {}", requestURI);

        try {
            if (excluded_URLs.contains(requestURI)) {
                logger.debug("Skipping JWT validation for URI: {}", requestURI);
                chain.doFilter(request, response);
                return;
            }

            if (isJwtMissing(clientJWT, request.getRequestURI()) ) 
                throw new InvalidJwtException("JSON Web Token is not found.");
                
            if (clientJWT != null && jwtUtils.validateJwtToken(clientJWT)) {
                String      tokenEmail  = jwtUtils.decipher(clientJWT)[0];
                verifyCookieEmailMatchesToken(request, tokenEmail);
                UserDetails userDetails = userDetailsService.loadUserByUsername(tokenEmail);
                setAuthenticationContext(request, userDetails);   
            }

            chain.doFilter(request, response);
            
        } catch (InvalidJwtException | CookieHeaderMismatchException e) {
            logger.error("Authentication error: {}", e.getMessage());
            FilterResponse.sendErrorResponse(response, HttpStatus.UNAUTHORIZED, e);
            
        } catch (Exception e) {
            logger.error("Unexpected error in AuthTokenFilter: {}", e.getMessage());
            FilterResponse.sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }






    // =====================================================
    // Utility
    // =====================================================
    /**
     * Checks whether the JWT is required based on the request URI.
     * 
     * @param jwt        JWT token value.
     * @param requestURI Request URI string.
     */
    private boolean isJwtMissing(String jwt, String requestURI) {
        return jwt == null && !excluded_URLs.contains(requestURI);
    }


    /**
     * Verifies that the email in the JWT token matches the email in the cookie.
     * 
     * @param request                        HTTP request object.
     * @param correctTokenEmail                     Email extracted from the JWT token.
     * @throws CookieHeaderMismatchException If the email does not match.
     */
    private void verifyCookieEmailMatchesToken(
        HttpServletRequest request,
        String             correctTokenEmail
    ) throws CookieHeaderMismatchException {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            logger.error("Cookies are null.");
            throw new CookieHeaderMismatchException("Cookies are null.");
        }


        Optional<Cookie> tokenCookie = 
            Arrays.stream(request.getCookies())
                .filter(c ->  c.getName().equals(Constants.Cookies.getToken()))
                .findFirst();
        if (tokenCookie.isPresent()) {
            String _tokenEmail = jwtUtils.decipher(tokenCookie.get().getValue())[0];

            if (!_tokenEmail.equals(correctTokenEmail)) {
                logger.error("Mismatched Jwt (cookie email and header email)");
                throw new CookieHeaderMismatchException("Mismatched JWT");
            }

        } else {
            logger.error("Missing 'token' in cookies.");
            throw new CookieHeaderMismatchException("Missing 'token' in cookies.");
        }
    }


    /**
     * Sets the authentication context using the provided user details.
     * 
     * @param request     HTTP request object.
     * @param userDetails User details to be used for authentication.
     */
    private void setAuthenticationContext(
        HttpServletRequest request, 
        UserDetails        userDetails
    ) {
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                userDetails, 
                null, 
                userDetails.getAuthorities()
            );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    private void logRequestDetails(ServletRequest request) {
        logger.debug("== " + this.getClass().getSimpleName() + " - doFilter ==");
        logger.debug("Remote Address/port: " + request.getRemoteAddr() + "/" + request.getRemotePort());
        logger.debug("Local  Address/port: " + request.getLocalAddr()  + "/" + request.getLocalPort());
        logger.debug("Protocol: " + request.getProtocol());
    }

    private void logHttpServletRequestDetails(HttpServletRequest request) {
        logger.debug("== HttpServletRequest Details ==");
        logger.debug("Method: {}", request.getMethod());
        logger.debug("AuthType: {}", request.getAuthType());
        logger.debug("Request URI: {}", request.getRequestURI());
        logger.debug("Request URL: {}", request.getRequestURL());
    }

}