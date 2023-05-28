package com.thirteenseven.workoutdiary.controller;

import java.security.Principal;
import java.util.*;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thirteenseven.workoutdiary.dao.ExerciseRepository;
import com.thirteenseven.workoutdiary.dao.MRecordRepository;
import com.thirteenseven.workoutdiary.dao.ExerciseRecordRepository;
import com.thirteenseven.workoutdiary.dao.UserRepository;
import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;
import com.thirteenseven.workoutdiary.exception.UnauthorizedAttemptException;
import com.thirteenseven.workoutdiary.model.Exercise;
import com.thirteenseven.workoutdiary.model.User;
import com.thirteenseven.workoutdiary.model.ExerciseRecord;
import com.thirteenseven.workoutdiary.payload.response.DataResponse;
import com.thirteenseven.workoutdiary.utilities.MUtility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RequestMapping("api/v1/exerciseRecord")
@RestController
public class ExerciseRecordController implements IRecordController<ExerciseRecord> {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private ExerciseRecordRepository exerciseRecordRepo;
    @Autowired
    private ExerciseRepository exerciseRepo;
    // @Autowired
    // private MUtility mUtility;
    

    @Override
    public MRecordRepository<ExerciseRecord, String> getModelRepo() {
        return this.exerciseRecordRepo;
    }

    @Override
    public UserRepository getUserRepo() {
        return this.userRepo;
    }


    // ==============================
    // REST API
    // ==============================
    @GetMapping("/getByUserExername")
    public DataResponse<List<ExerciseRecord>> getByUserExername(
        @RequestParam String name,
        @RequestParam String exerciseName,
        @RequestParam int skipCount,
        @RequestParam int limit
    ) throws EntityNotFoundException {           

        List<ExerciseRecord> entity = null;

        User user = this.getUserRepo().findByName(name)
                    .orElseThrow(()-> new EntityNotFoundException("Cannot find user by name "+name));

        entity = exerciseRecordRepo.findByUserExercise(name, exerciseName, skipCount, limit);
        
        return new DataResponse<List<ExerciseRecord>>(entity);
    }


    @PostMapping("/add")
    public DataResponse<ExerciseRecord> add(
        @RequestParam String username,
        @RequestParam(required = false) String datetime,
        @Valid @RequestBody ExerciseRecord entity
        // @RequestBody Map<String, Object> payload
    ) throws EntityNotFoundException {
        User user = userRepo.findByName(username)
            .orElseThrow(()-> new EntityNotFoundException("Username not found."));
        final Date targetDate = (datetime == null) ? MUtility.to30MinInterval(new Date()) : 
                                        MUtility.to30MinInterval(MUtility.formatDateTimeStr(datetime));
        entity.setDate(targetDate);
        entity.setUser(user);

        ExerciseRecord record = exerciseRecordRepo
            .findByUserDateExercise(username, targetDate, entity.getExerciseName()).orElse(entity);
        if (record != entity) record.addExcerciseRecord(entity.getSets());
    
        exerciseRecordRepo.save(record);
        return new DataResponse<ExerciseRecord>(record, "Added record successfully.");
    }




    @DeleteMapping("/deleteByPrecision")
    public DataResponse<ExerciseRecord> deleteExerRecordByIdx(
        @RequestParam(name="name") String username,
        @RequestParam String exerciseName,
        @RequestParam String datetime
    ) throws UsernameNotFoundException, EntityNotFoundException {
        if (!userRepo.existsByName(username))
            throw new UsernameNotFoundException("Username not found.");
        ExerciseRecord record = exerciseRecordRepo
            .findByUserDateExercise(username, MUtility.to30MinInterval(MUtility.formatDateTimeStr(datetime)), exerciseName)
            .orElseThrow(()-> new EntityNotFoundException("Record not found."));
        exerciseRecordRepo.delete(record);
        return new DataResponse<ExerciseRecord>(record, "Excercise record has been deleted.");
    }



    @DeleteMapping("/deleteAllByExerNameForUser")
    public DataResponse<Integer> deleteAllByExerNameForUser(
        @RequestParam(name="name") String username,
        @RequestParam String exerciseName
    ) throws UsernameNotFoundException, EntityNotFoundException {
        if (!userRepo.existsByName(username))
            throw new UsernameNotFoundException("Username not found.");
        List<ExerciseRecord> ls = exerciseRecordRepo.findByUserExercise(username, exerciseName, 0, 100);
        exerciseRecordRepo.deleteAll(ls);

        return (ls.size() != 0) ? 
            new DataResponse<Integer>(ls.size(), "Deleted document count.") :
            new DataResponse<Integer>(0, "No document was found.");
    }
}