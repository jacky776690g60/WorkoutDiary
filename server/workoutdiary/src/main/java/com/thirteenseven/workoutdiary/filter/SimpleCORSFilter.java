package com.thirteenseven.workoutdiary.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SimpleCORSFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);


    public SimpleCORSFilter() {
        logger.info("SimpleCORSFilter init");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin")); // don't delete!!!!
        // response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        // response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");

        chain.doFilter(req, res);
        logger.debug("Finished Simple CORS Filter, Origin: " + request.getHeader("Origin"));
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}




// @Component
// public class SimpleCORSFilter extends OncePerRequestFilter {

//     private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);


//     public SimpleCORSFilter() {
//         logger.info("SimpleCORSFilter init");
//     }

//     @Override
//     public void destroy() {}

//     @Override
//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//             throws ServletException, IOException {
//                 // HttpServletRequest request = (HttpServletRequest) req;
//                 // HttpServletResponse response = (HttpServletResponse) res;
        
//                 response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin")); // don't delete!!!!
//                 // response.setHeader("Access-Control-Allow-Origin", "*");
//                 response.setHeader("Access-Control-Allow-Credentials", "true");
//                 response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
//                 response.setHeader("Access-Control-Max-Age", "3600");
//                 response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");
        
//                 filterChain.doFilter(request, response);
//                 logger.debug("Finished Simple CORS Filter, Origin: " + request.getHeader("Origin"));
//     }
// }
