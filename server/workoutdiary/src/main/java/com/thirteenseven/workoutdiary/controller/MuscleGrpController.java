package com.thirteenseven.workoutdiary.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thirteenseven.workoutdiary.dao.MBaseRepository;
import com.thirteenseven.workoutdiary.dao.MuscleGroupRepository;
import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;
import com.thirteenseven.workoutdiary.model.MuscleGroup;
import com.thirteenseven.workoutdiary.payload.response.DataResponse;

import jakarta.validation.Valid;


@RequestMapping("api/v1/muscleGroup")
@RestController
public class MuscleGrpController implements IRESTController<MuscleGroup> {

    @Autowired
    private MuscleGroupRepository muscleGroupRepo;

    @Override
    public MBaseRepository<MuscleGroup, String> getModelRepo() {
        return this.muscleGroupRepo;
    }


    

    @PostMapping("/add")
    public DataResponse<MuscleGroup> add(
        @RequestBody @Valid MuscleGroup entity
    ) throws EntityNotFoundException {
        // System.out.println("thing");
        muscleGroupRepo.save(entity);
        return new DataResponse<MuscleGroup>(entity);
    }
}
