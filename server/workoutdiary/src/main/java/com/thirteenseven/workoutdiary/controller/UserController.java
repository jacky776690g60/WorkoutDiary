package com.thirteenseven.workoutdiary.controller;

// ~~~~~~~~ standard ~~~~~~~~
import java.util.*;
import java.util.stream.Stream;
// ~~~~~~~~ Spring ~~~~~~~~
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thirteenseven.workoutdiary.controller.interfaces.INameController;
import com.thirteenseven.workoutdiary.dao.UserRepository;
import com.thirteenseven.workoutdiary.dao.UserRoleRepository;
import com.thirteenseven.workoutdiary.dao.interfaces.INameRepository;
import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;
import com.thirteenseven.workoutdiary.model.User;
import com.thirteenseven.workoutdiary.payload.response.DataResponse;
import com.thirteenseven.workoutdiary.service.UserService;
import com.thirteenseven.workoutdiary.service.base.BaseDocumentService;
import com.thirteenseven.workoutdiary.utilities.TimeUtility;

@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*")
@RequestMapping("api/v1/user")
@RestController
public class UserController implements INameController<User> {

    // =======================================================================
    // dependency injection
    // =======================================================================
    @Autowired
    private PasswordEncoder         encoder;
    @Autowired
    private UserRoleRepository      roleRepo;
    @Autowired
    private UserRepository          userRepo;
    @Autowired
    private UserService             userService;
    

    // =======================================================================
    // Constructor
    // =======================================================================
    public UserController(@Autowired UserRepository userRepo) {
        this.userRepo = userRepo;
    }


    // =======================================================================
    // Override
    // =======================================================================
    @Override
    public INameRepository<User, String> getModelRepo() {
        return userRepo;
    }

    @Override
    public PasswordEncoder getEncoder() {
        return encoder;
    }


    @Override
    public void updateWithSpecificRule(
        User newEntity, Map<String, Object> json
    ) throws EntityNotFoundException {

        // TODO: do something mit AWS for profilePicURL

        if (json.containsKey("roles")) {
            Set<String> roleStrings = new HashSet<>((List<String>) json.get("roles"));
            newEntity.setRoles(roleRepo.getRoles(roleStrings));
        }
    }


    @Override
    public BaseDocumentService getDocumentService() {
        return userService;
    }

    // =======================================================================
    // New CRUD
    // =======================================================================

    /**
     * Get a document via name parameter.
     * 
     * @param name name of the document
     * @return DataResponse containing the requested document
     * @throws EntityNotFoundException if the name cannot be found
     */
    @GetMapping(path = "/getCurrent")
    public DataResponse<User> getCurrentUser(
        @CookieValue(name = "username", required = true) String username
    ) throws EntityNotFoundException {
        User cUser = userRepo.findByName(username).orElse(null);
        return new DataResponse<>(cUser, "Found user.");
    }
}