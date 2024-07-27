package com.thirteenseven.workoutdiary.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thirteenseven.workoutdiary.controller.interfaces.INameController;
import com.thirteenseven.workoutdiary.dao.MuscleGroupRepository;
import com.thirteenseven.workoutdiary.dao.interfaces.IBaseRepository;
import com.thirteenseven.workoutdiary.dao.interfaces.INameRepository;
import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;
import com.thirteenseven.workoutdiary.model.MuscleGroup;
import com.thirteenseven.workoutdiary.payload.response.DataResponse;

import jakarta.validation.Valid;


import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@RequestMapping("api/v1/muscleGroup")
@RestController
public class MuscleGrpController implements INameController<MuscleGroup> {

    @Autowired
    private MuscleGroupRepository muscleGroupRepo;

    @Override
    public INameRepository<MuscleGroup, String> getModelRepo() {
        return this.muscleGroupRepo;
    }


    @Override
    public PasswordEncoder getEncoder() {
        throw new UnsupportedOperationException("Unimplemented method 'getEncoder'");
    }





    @PutMapping(path="/tester")
    public DataResponse<List<MuscleGroup>> oneTimeUpdate(
        
    ) throws EntityNotFoundException {

        List<MuscleGroup> mgs = this.getModelRepo().queryField("group");

        


        

        return new DataResponse<List<MuscleGroup>>(mgs);
    }
    
}
