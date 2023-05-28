package com.thirteenseven.workoutdiary.advice;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.thirteenseven.workoutdiary.exception.CookieHeaderMismatchException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** This is actually a filter disguise as exception handler.
 * The order is being added in {@link WebSecurityConfig}.
 */
public class FilterChainExceptionHandler extends OncePerRequestFilter {
    // ==============================
    // Variables
    // ==============================
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;


    // ==============================
    // functions
    // ==============================
    @Override
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (CookieHeaderMismatchException e) { 
            setErrorResponse(HttpStatus.BAD_REQUEST, response, e);
            System.out.println("mismatch cookie and header: " + e);
        
        } catch (Exception e) {
            System.out.println("error in handling filter exception: " + e);
        }
    }


    
    public void setErrorResponse(HttpStatus status, HttpServletResponse response, Throwable ex){
        response.setStatus(status.value());
        response.setContentType("application/json");
        try {
            // TODO: create a proper json response class
            response.getWriter().write("Error: mismatched cookie and header");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
