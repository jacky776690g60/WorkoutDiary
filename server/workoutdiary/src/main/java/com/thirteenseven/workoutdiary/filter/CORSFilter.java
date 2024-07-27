package com.thirteenseven.workoutdiary.filter;
// ~~~~~~~~ standard ~~~~~~~~
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
// ~~~~~~~~ Spring ~~~~~~~~
import org.springframework.stereotype.Component;
// ~~~~~~~~ Workout Diary ~~~~~~~~
import com.thirteenseven.workoutdiary.exception.UnauthorizedException;
import com.thirteenseven.workoutdiary.utilities.Constants;


/** 
 * Custom CORS filter to handle Cross-Origin Resource Sharing (CORS) requests.
 * This filter ensures that the API complies with the same-origin policy and 
 * allows specific cross-origin requests. 
 * 
 * @note
 * In a typical Spring Boot application, any bean that implements the Filter 
 * interface can be automatically added to the filter chain unless explicitly 
 * configured otherwise.
 */
@Component
public class CORSFilter implements Filter {

    private static final Logger      logger          = LoggerFactory.getLogger(CORSFilter.class);
    private static final Set<String> ALLOWED_ORIGINS = new HashSet<>(Constants.CORS.get_ALLOWED_ORIGINS());
    private static final Pattern ALLOWED_REFERER_PATTERN = Pattern.compile(
        "^https?://workoutdiary\\.jacktogon\\.com(/.*)?$"
    );
    



    // =======================================================================
    // Constructor
    // =======================================================================
    public CORSFilter() {
        logger.debug("CORSFilter Initialized...");
    }

    // =======================================================================
    // Overridden functions
    // =======================================================================
    /**
     * Filters incoming requests to apply CORS headers and passes the request
     * to the next filter in the chain.
     * 
     * @param req   the incoming ServletRequest
     * @param res   the outgoing ServletResponse
     * @param chain the FilterChain for passing the request to the next filter
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(
        ServletRequest  req, 
        ServletResponse res, 
        FilterChain     chain
    ) throws IOException, ServletException, UnauthorizedException {

        HttpServletRequest  httpRequest  = (HttpServletRequest)  req;
        HttpServletResponse httpResponse = (HttpServletResponse) res;

        String origin  = httpRequest.getHeader("Origin");
        String referer = httpRequest.getHeader("Referer");


        try {
            if (isPreflightRequest(httpRequest)) {
                handlePreflightRequest(httpResponse, origin);
                return;
            }


            if (isValidOrigin(origin)) {
                configureCorsHeaders(httpResponse, origin);
            } else if (origin == null && !isValidReferer(referer)) {
                throw new UnauthorizedException("Request blocked due to unauthorized Referer.");
            }

            chain.doFilter(req, res);
        } catch (UnauthorizedException e) {
            logger.error("Unauthorized request: {}", e.getMessage());
            httpResponse.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        } finally {
            logger.debug("Finished CORS Filter");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

    // =======================================================================
    // Private 
    // =======================================================================
    private boolean isValidOrigin(String origin) {
        return ALLOWED_ORIGINS.contains(origin);
    }
    
    private boolean isValidReferer(String referer) {
        return referer != null && ALLOWED_REFERER_PATTERN.matcher(referer).matches();
    }

    private void configureCorsHeaders(HttpServletResponse response, String origin) {
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.setHeader("Access-Control-Expose-Headers", "Authorization, Set-Cookie");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    private boolean isPreflightRequest(HttpServletRequest request) {
        /**
         * A preflight request is an OPTIONS request that the browser 
         * sends automatically before the actual request in certain situations.
         * 
         * Its purpose is to check if the server will allow the actual request to be made.
         */
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private void handlePreflightRequest(HttpServletResponse response, String origin) {
        if (isValidOrigin(origin))
            response.setStatus(HttpStatus.OK.value());
        else
            response.setStatus(HttpStatus.FORBIDDEN.value());
    }


}