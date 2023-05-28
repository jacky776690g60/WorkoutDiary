package com.thirteenseven.workoutdiary.advice;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.thirteenseven.workoutdiary.config.WebSecurityConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** <h3>Authentication entry point with Jwt.</h3>
 * <p>AuthenticationEntryPoint is used to send an HTTP response 
 * that requests credentials from a client.
 * </p>
 * Basically, a filter which is the first point of entry for 
 * Spring Security. Set in {@link WebSecurityConfig}.
*/
@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {
	private static final Logger logger = LoggerFactory.getLogger(AuthEntryPoint.class);

	@Override
	public void commence(
            HttpServletRequest request, 
            HttpServletResponse response,
			AuthenticationException authException) 
    throws IOException, ServletException, AuthenticationException {

        logger.error("Unauthorized Error: {}", authException.getMessage());
        // ========================
        // reference! don't delete!

        /* The error message and any binding errors are no longer included in 
        the default error page by default. This reduces the risk of leaking 
        information to a client. */
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
        // ========================
	}
}