package com.thirteenseven.workoutdiary.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thirteenseven.workoutdiary.dao.DifficultyRepository;
import com.thirteenseven.workoutdiary.dao.ExerciseRepository;
import com.thirteenseven.workoutdiary.dao.MBaseRepository;
import com.thirteenseven.workoutdiary.dao.MuscleGroupRepository;
import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;
import com.thirteenseven.workoutdiary.model.Difficulty;
import com.thirteenseven.workoutdiary.model.Exercise;
import com.thirteenseven.workoutdiary.model.MuscleGroup;
import com.thirteenseven.workoutdiary.payload.request.AddExerciseRequest;
import com.thirteenseven.workoutdiary.payload.response.DataResponse;

import jakarta.validation.Valid;

@RequestMapping("api/v1/exercise")
@RestController
// @Validated
public class ExerciseController implements IRESTController<Exercise>  {
    
    @Autowired
    private ExerciseRepository exerciseRepo;
    @Autowired
    private DifficultyRepository difficultyRepo;
    @Autowired
    private MuscleGroupRepository muscleGroupRepo;


    // ==============================
    // Functions
    // ==============================
    @Override
    public MBaseRepository<Exercise, String> getModelRepo() {
        return exerciseRepo;
    }

    @GetMapping("/getAllUnauth")
    public DataResponse<List<Exercise>> getAllUnauth() {
        // TODO: cursor and doc_per_page?
        List<Exercise> ls = this.getModelRepo().findAll();
        return new DataResponse<List<Exercise>>(ls);
    }

    // ==============================
    // REST
    // ==============================
    @GetMapping("/getByMuscleGroup")
    public DataResponse<List<Exercise>> getByMuscleGroup(
        @RequestParam String ...muscleGroupNames
    ) {
        List<Exercise> exercises = exerciseRepo.findByMuscleGroups(muscleGroupNames);
        return new DataResponse<List<Exercise>>(exercises);
    }


    @GetMapping("/getByDifficulty")
    public DataResponse<List<Exercise>> getByDifficulty(
        @RequestParam String difficulty
    ) {
        List<Exercise> exercises = exerciseRepo.findByDifficulty(difficulty);
        return new DataResponse<List<Exercise>>(exercises);
    }


    @GetMapping("/getByDifficultyAndMG")
    public DataResponse<List<Exercise>> getByDifficultyAndMuscleGroup(
        @RequestParam String difficulty,
        @RequestParam String ...muscleGroupNames
    ) {
        List<Exercise> exercises = exerciseRepo.findByDifficultyAndMuscleGroup(difficulty, muscleGroupNames);
        return new DataResponse<List<Exercise>>(exercises);
    }


    @PostMapping("/add")
    public DataResponse<Exercise> add(
        @RequestBody @Valid AddExerciseRequest addExerciseReq
    ) throws EntityNotFoundException {
        final Difficulty difficulty = difficultyRepo.findByName(addExerciseReq.getDifficulty())
                                    .orElseThrow(()-> new EntityNotFoundException("Difficulty not found by name."));
        final Set<MuscleGroup> mgs = new HashSet<>();
        for (String mgStr : addExerciseReq.getMuscleGroups()) {
            MuscleGroup mg = muscleGroupRepo.findByName(mgStr.toUpperCase())
                                .orElseThrow(()-> new EntityNotFoundException(mgStr+" Muscle group not found by name."));
            mgs.add(mg);
        }

        
        Exercise exercise = exerciseRepo.save(
            new Exercise(
                addExerciseReq.getName(), 
                addExerciseReq.getVideoURL(), 
                mgs, 
                difficulty,
                addExerciseReq.getDescription()
            )
        );

        return new DataResponse<Exercise>(exercise);
    }
}
