package com.thirteenseven.workoutdiary.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thirteenseven.workoutdiary.model.Role;
import com.thirteenseven.workoutdiary.model.User;
import com.thirteenseven.workoutdiary.payload.request.LoginRequest;
import com.thirteenseven.workoutdiary.payload.request.SignupRequest;
import com.thirteenseven.workoutdiary.payload.response.JwtResponse;
import com.thirteenseven.workoutdiary.payload.response.SimpleResponse;
import com.thirteenseven.workoutdiary.service.MUserDetails;
import com.thirteenseven.workoutdiary.utilities.JwtUtility;
import com.thirteenseven.workoutdiary.controller.handlers.ICookieHandler;
import com.thirteenseven.workoutdiary.dao.UserRepository;
import com.thirteenseven.workoutdiary.dao.UserRoleRepository;
import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/** 
 * Controller for authentication. Handle both sign in & sign up 
 * @apiNote
 * "maxAge": The maximum age (in seconds) of the cache duration for 'preflight' responses.
 */
@CrossOrigin(origins = "*", maxAge = 3600) 
@RestController
@RequestMapping("api/auth")
public class AuthController implements ICookieHandler {
    // ==============================
    // Variables
    // ==============================
    @Value("${WorkoutDiary.app.jwtSecret}")
	private String jwtSecretKey;
    @Value("${WorkoutDiary.app.development:true}")
	private boolean isDevelopmentMode;

	/** process authentication request */
    @Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository        userRepo;

	@Autowired
	UserRoleRepository    roleRepo;

	@Autowired
	PasswordEncoder       encoder;

	@Autowired
	JwtUtility            jwtUtils;


    // =====================================================
    // Override
    // =====================================================
    @Override
    public boolean isDevelopmentMode() {
        return isDevelopmentMode;
    }


    // =======================================================================
    // RESTful
    // =======================================================================
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(
        @Valid @RequestBody LoginRequest loginRequest,
        HttpServletResponse              response
    ) throws 
        BadCredentialsException, 
        AuthenticationException 
    {
        /** Try to authenticate user with provided credentials */
		Authentication authentication = 
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),    // using email as username
                    loginRequest.getPassword()
                )
            );

        /** Set authentication context if authentication is successful */
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication, encoder); // JSON web token


		MUserDetails userDetails = (MUserDetails)authentication.getPrincipal();
        List<String> roles       = userDetails.getAuthorities()
                                    .stream().map(item -> item.getAuthority())
                                    .collect(Collectors.toList());

        setCookieByClass(response, "token", jwt);
        setCookieByClass(response, "username",  userDetails.getUsername());
        
		return ResponseEntity.ok(
                new JwtResponse(
                    jwt, 
                    userDetails.getId(), 
                    userDetails.getUsername(), 
                    userDetails.getEmail(), 
                    roles
                )
            );
	}
    

    
	/** 
     *
	 * @throws EntityNotFoundException 
     */
	@PostMapping("/signup")
    public ResponseEntity<?> registerUser(
        @Valid @RequestBody SignupRequest signUpRequest
        
    ) throws EntityNotFoundException {
		if (userRepo.existsByName(signUpRequest.getUsername()))
			return ResponseEntity.badRequest().body(
                new SimpleResponse<String>("[Error] Username is already taken!")
            );
		if (userRepo.existsByEmail(signUpRequest.getEmail()))
			return ResponseEntity.badRequest().body(
                new SimpleResponse<String>("[Error] Email is already in use!")
            );

		User user = new User(
                        signUpRequest.getUsername(), 
                        encoder.encode(signUpRequest.getPassword()),
                        signUpRequest.getEmail(),
                        signUpRequest.getBirthday(),
                        signUpRequest.getProfilePicURL(),
                        signUpRequest.getGender()
                    );
		Set<String> strRoles = signUpRequest.getSecretKey().strip().equals(jwtSecretKey.strip()) ?
                                signUpRequest.getRoles() : 
                                new HashSet<String>(Arrays.asList("user"));
		Set<Role> roles = new HashSet<>();

		if (strRoles == null || strRoles.size() == 0) {
			Role userRole = roleRepo.findByName(Role.ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("[Error] ROLE_USER is not found."));
            roles.add(userRole);
            
		} else {
			roles = roleRepo.getRoles(strRoles);
		}

		user.setRoles(roles);
		userRepo.save(user);

		return ResponseEntity.ok(new SimpleResponse<String>("User registered successfully! User ID: " + user.getId()));
	}



    


    /**
     * @note
     * Logout
     * 
     * 1. Centralized Security Management
     * - Consistency: Using Spring Security's built-in logout mechanism ensures consistent security handling across the application.
     * - Centralized Configuration: All security-related configurations are managed in one place, making it easier to understand and maintain.
     * 2. Built-in Functionality
     * - Security Context Clearing: Spring Security automatically clears the SecurityContext and invalidates the session, preventing potential security vulnerabilities.
     * - Cookie Deletion: Cookies like JSESSIONID or custom authentication cookies can be deleted easily using Spring's logout handlers.
     * - Session Invalidating: Sessions are invalidated securely, preventing session fixation attacks.
     * 3. Extensibility
     * - Custom Handlers: The ability to plug in custom LogoutSuccessHandler provides flexibility to handle specific use cases like logging, redirection, or audit trails.
     * - Additional Logic: Spring Security's logout process can incorporate extra logic such as deleting tokens, sending logout notifications, etc.
     * 4. Stateless Support
     * - Token-Based Authentication: It handles logout for stateless authentication methods (e.g., JWTs) efficiently by offering handlers to invalidate tokens.
     */
















}