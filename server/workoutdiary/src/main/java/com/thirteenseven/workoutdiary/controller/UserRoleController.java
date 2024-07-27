package com.thirteenseven.workoutdiary.controller;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.thirteenseven.workoutdiary.controller.interfaces.INameController;
import com.thirteenseven.workoutdiary.dao.UserRoleRepository;
import com.thirteenseven.workoutdiary.dao.interfaces.INameRepository;
import com.thirteenseven.workoutdiary.model.Role;
import com.thirteenseven.workoutdiary.model.base.NameBasedModel;

// ~~~~~~~~ standard ~~~~~~~~
// ~~~~~~~~ Spring ~~~~~~~~
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;;




@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*")
@RequestMapping("api/v1/userRole")
@RestController
public class UserRoleController implements INameController<Role> {

    // =====================================================
    // DI
    // =====================================================
    @Autowired
    private UserRoleRepository  userRoleRepository;


    // =====================================================
    // Override
    // =====================================================
    @Override
    public INameRepository<Role, String> getModelRepo() {
        return userRoleRepository;
    }

    @Override
    public Role newEntityByName(String name) {
        if (NameBasedModel.class.isAssignableFrom(Role.class)) {
            return new Role(name);
        }
        return INameController.super.newEntityByName(name);
    }
}
