/** ================================================================
| WebSecurityConfig.java  --  WorkoutDiary/server/workoutdiary/src/main/java/com/thirteenseven/workoutdiary/config/WebSecurityConfig.java
|
| Created by Jack on 05/09, 2024
| Copyright © 2024 jacktogon. All rights reserved.
================================================================= */

package com.thirteenseven.workoutdiary.config;
// ~~~~~~~~ standard ~~~~~~~~
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

// ~~~~~~~~ Spring ~~~~~~~~
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// ~~~~~~~~ Workout Diary ~~~~~~~~
import com.thirteenseven.workoutdiary.advice.AuthEntryPointAdvisor;
import com.thirteenseven.workoutdiary.filter.AuthEntryFilter;
import com.thirteenseven.workoutdiary.filter.CORSFilter;
import com.thirteenseven.workoutdiary.service.MUserDetailsService;
import com.thirteenseven.workoutdiary.utilities.Constants;

/**
 * @note
 * {@code @EnableMethodSecurity(prePostEnabled = true)}: This will allow you to use PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')") in RestController
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
    
    @Value("${application.security.enabled:true}")
	private boolean                 isSecurityEnabled;

    // =====================================================
    // Dependency Injection
    // =====================================================
    @Autowired
	MUserDetailsService             userDetailsService;
    /** An advice to prompt for providing correct credentials. */
    @Autowired
	private AuthEntryPointAdvisor   authEntryPoint;
    @Autowired
	private AuthEntryFilter         authEntryFilter;
    

    // =====================================================
    // Beans
    // =====================================================
    /**
     * Creates and configures a {@link DaoAuthenticationProvider} bean for 
     * authentication. This provider leverages {@link UserDetailsService} to load 
     * user data and {@link PasswordEncoder} to encode passwords. It authenticates 
     * user credentials (username and password). If authentication is successful, 
     * it generates a {@link UsernamePasswordAuthenticationToken} that can be 
     * used by authentication filters throughout the security context.
     *
     * @return 
     * an instance of {@link DaoAuthenticationProvider} set up with a user details 
     * service and a password encoder (BCryptPasswordEncoder by default).
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder()); // bcrypt encoder
        return authProvider;
    }
    

    /**
     * Configures and retrieves an {@link AuthenticationManager} using the 
     * provided {@link AuthenticationConfiguration}. This manager is configured 
     * with a {@link DaoAuthenticationProvider} specifically for validating 
     * {@link UsernamePasswordAuthenticationToken} objects. The authentication 
     * manager centralizes authentication operations and is crucial for the 
     * security framework in handling login processes.
     *
     * @param authConfig An {@link AuthenticationConfiguration} used to export 
     * and configure authentication settings.
     * @return An instance of {@link AuthenticationManager} configured as per 
     * the provided settings.
     * @throws Exception If there is an error in configuring or retrieving the 
     * authentication manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    /** Return a BCrypt hashing encoder {@link BCryptPasswordEncoder} */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    /** This can be the first layer of CORS filter from Spring Security.
     * But we have a second customer layer named {@link CORSFilter}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedOriginPatterns(
            Constants.CORS.get_ALLOWED_ORIGINS()
        ); // Specify the allowed origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true); // Allow credentials
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply CORS configuration to all paths
        return source;
    }


    
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (isSecurityEnabled) {
            http
            .cors().and()
            /**
             * @note Don't need this in this app because it requires the client 
             * to send a special token with each state-changing request (such 
             * as POST, PUT, DELETE, etc.)
             */
            .csrf().disable()
            .exceptionHandling().authenticationEntryPoint(authEntryPoint)
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests((authorize) -> 
                authorize
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .logout(logout-> 
                logout.logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    SecurityContextHolder.getContext().setAuthentication(null);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.sendRedirect("http://localhost:3000/logout-success"); // send a signal to redirect in React
                })
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .deleteCookies( // This will remove the cookies' values
                    Constants.Cookies.getToken(),
                    Constants.Cookies.getUsername()
                ) 
            );
            
            http.authenticationProvider(authenticationProvider());
            http.addFilterBefore(authEntryFilter, UsernamePasswordAuthenticationFilter.class);

        }
        return http.build();
	}
}


