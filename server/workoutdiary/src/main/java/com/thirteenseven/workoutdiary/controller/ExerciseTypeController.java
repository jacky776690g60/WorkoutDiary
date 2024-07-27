package com.thirteenseven.workoutdiary.controller;

// ~~~~~~~~ standard ~~~~~~~~
import java.util.*;
import java.util.stream.Stream;
// ~~~~~~~~ Spring ~~~~~~~~
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thirteenseven.workoutdiary.controller.interfaces.INameController;
import com.thirteenseven.workoutdiary.dao.ExerciseTypeRepository;
import com.thirteenseven.workoutdiary.dao.interfaces.INameRepository;
import com.thirteenseven.workoutdiary.model.ExerciseType;
import com.thirteenseven.workoutdiary.model.base.NameBasedModel;
import com.thirteenseven.workoutdiary.service.base.BaseDocumentService;


@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*")
@RequestMapping("api/v1/exerciseType")
@RestController
public class ExerciseTypeController implements INameController<ExerciseType> {

    // =======================================================================
    // dependency injection
    // =======================================================================
    @Autowired
    private PasswordEncoder         encoder;
    @Autowired
    private ExerciseTypeRepository  exerciseTypeRepository;
    

    // =======================================================================
    // Constructor
    // =======================================================================

    // =======================================================================
    // Override
    // =======================================================================
    @Override
    public INameRepository<ExerciseType, String> getModelRepo() {
        return exerciseTypeRepository;
    }

    @Override
    public PasswordEncoder getEncoder() {
        return encoder;
    }

    
    @Override
    public ExerciseType newEntityByName(String name) {
        if (NameBasedModel.class.isAssignableFrom(ExerciseType.class)) {
            return new ExerciseType(name);
        }
        return INameController.super.newEntityByName(name);
    }
}
