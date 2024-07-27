package com.thirteenseven.workoutdiary.controller;

import java.util.*;
import java.util.stream.Collectors;

// ~~~~~~~~ Spring ~~~~~~~~
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.domain.Page;

// ~~~~~~~~ Workout Diary ~~~~~~~~
import com.thirteenseven.workoutdiary.controller.interfaces.INameController;
import com.thirteenseven.workoutdiary.controller.interfaces.IRecordController;
import com.thirteenseven.workoutdiary.dao.DifficultyRepository;
import com.thirteenseven.workoutdiary.dao.ExerciseRecordRepository;
import com.thirteenseven.workoutdiary.dao.ExerciseRepository;
import com.thirteenseven.workoutdiary.dao.ExerciseTypeRepository;
import com.thirteenseven.workoutdiary.dao.MuscleGroupRepository;
import com.thirteenseven.workoutdiary.dao.UserRepository;
import com.thirteenseven.workoutdiary.dao.UserRoleRepository;
import com.thirteenseven.workoutdiary.dao.interfaces.IBaseRepository;
import com.thirteenseven.workoutdiary.dao.interfaces.INameRepository;
import com.thirteenseven.workoutdiary.dao.interfaces.IRecordRepository;
import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;
import com.thirteenseven.workoutdiary.exception.UnauthorizedException;
import com.thirteenseven.workoutdiary.model.Difficulty;
import com.thirteenseven.workoutdiary.model.Exercise;
import com.thirteenseven.workoutdiary.model.ExerciseRecord;
import com.thirteenseven.workoutdiary.model.ExerciseType;
import com.thirteenseven.workoutdiary.model.MuscleGroup;
import com.thirteenseven.workoutdiary.model.Role;
import com.thirteenseven.workoutdiary.model.User;
import com.thirteenseven.workoutdiary.model.base.Location;
import com.thirteenseven.workoutdiary.model.base.RepetitionsModel;
import com.thirteenseven.workoutdiary.payload.request.ExerciseRecordPostRequest;
import com.thirteenseven.workoutdiary.payload.response.DataResponse;
import com.thirteenseven.workoutdiary.service.ExerciseRecordService;
import com.thirteenseven.workoutdiary.service.ExerciseService;
import com.thirteenseven.workoutdiary.service.base.BaseDocumentService;
import com.thirteenseven.workoutdiary.utilities.JwtUtility;
import com.thirteenseven.workoutdiary.utilities.TimeUtility;
import com.thirteenseven.workoutdiary.controller.interfaces.IBaseController;

import jakarta.validation.Valid;


@RequestMapping("api/v1/exerciseRecord")
@RestController
public class ExerciseRecordController implements IRecordController<ExerciseRecord> {

    // =====================================================
    // DI
    // =====================================================
    @Autowired
    private ExerciseRecordRepository exerciseRecordRepository;
    @Autowired
    private ExerciseRecordService    exerciseRecordService;
    @Autowired
    private ExerciseRepository       exerciseRepository;
    @Autowired
    private UserRepository           userRepository;
    @Autowired
    private MuscleGroupRepository    muscleGroupRepository;
    @Autowired
    private UserRoleRepository       userRoleRepository;


    @Override
    public PasswordEncoder getEncoder() {
        throw new UnsupportedOperationException("Unimplemented method 'getEncoder'");
    }
    
    @Override
    public IRecordRepository<ExerciseRecord, String> getModelRepo() {
        return exerciseRecordRepository;
    }
    
    @Override
    public BaseDocumentService getDocumentService() {
        return exerciseRecordService;
    }

    @Override
    public UserRepository getUserRepo() {
        return userRepository;
    }

    // =====================================================
    // Get
    // =====================================================
    /** 
     * This controller can be used to query record for a user.
     */
    @GetMapping("/search")
    public DataResponse<List<ExerciseRecord>> search(
        @RequestParam(required = false) String[] exerciseNames,
        // @RequestParam(required = false) String[] muscleGroups,
        // @RequestParam(required = false) String[] exerciseTypes,
        @RequestParam(required = false, defaultValue = "false") boolean strict,
        @RequestParam(required = false, defaultValue = "false") boolean self,
        @RequestParam(required = false, defaultValue = "0") int page, // Default page index is 0
        @RequestParam(required = false, defaultValue = "5") int size, // Default page size is 5
        @CookieValue(name = "username", required = true) String username
    ) throws EntityNotFoundException {
    
        User user = userRepository.findByName(username)
            .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));


        Query query = new Query();
        

        if (exerciseNames != null) {
            List<Exercise> exerciseObjs = findByNameInSpecificRepo(exerciseRepository, exerciseNames);
    
            if (!exerciseObjs.isEmpty()) {
                if (strict) query.addCriteria(Criteria.where("exercise").all(exerciseObjs));
                else        query.addCriteria(Criteria.where("exercise").in(exerciseObjs));
            }
        }

        Role adminRole = userRoleRepository.findByName(Role.ERole.ROLE_ADMIN)
            .orElseThrow(()-> new EntityNotFoundException("UserRole not found: Admin"));
            
        if (!user.getRoles().contains(adminRole) || self) {
            query.addCriteria(Criteria.where("user").is(user));
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "date");

        Pageable pageable = PageRequest.of(page, size, sort);

        List<ExerciseRecord> records = getDocumentService().queryOnClass(query.with(pageable), ExerciseRecord.class);

        Page<ExerciseRecord> resultPage = getDocumentService().findPaginated(query, ExerciseRecord.class, pageable);
        boolean hasNextPage = resultPage.getNumber() + 1 < resultPage.getTotalPages();
        logger.debug("Number (Idx) : " + resultPage.getNumber());
        logger.debug("Total Pages  : " + resultPage.getTotalPages());
        logger.debug("Has Next     : " + hasNextPage);

        for (ExerciseRecord record : records) {
            record.debulk(true);
        }

        return new DataResponse<>(records, "Success!", hasNextPage);
    }







    // =====================================================
    // Post
    // =====================================================
    @PostMapping(path = "/add", consumes = "application/json")
    public DataResponse<ExerciseRecord> add(
        @RequestBody @Valid ExerciseRecordPostRequest exRecordPostReq,
        @CookieValue(name = "username", required = true) String username
    
    ) throws EntityNotFoundException, IllegalArgumentException {
        String kExerciseName = exRecordPostReq.getExerciseName();
        final Exercise kExercise = 
                exerciseRepository.findByName(kExerciseName)
                .orElseThrow(()-> new EntityNotFoundException(entityNotFoundMsg(kExerciseName)));
        final User kUser = 
                userRepository.findByName(username)
                .orElseThrow(()-> new EntityNotFoundException(username));
        final ExerciseRecord exerciseRecord = new ExerciseRecord(
            exRecordPostReq.getDatetime(),
            kUser,
            kExercise,
            exRecordPostReq.getSets(),
            exRecordPostReq.getNote(),
            new Location(exRecordPostReq.getLocation()[0], exRecordPostReq.getLocation()[1])
        );
        exerciseRecordRepository.save(exerciseRecord);

        return new DataResponse<>(exerciseRecord, "Added new exercise record.");
    }
    

    

    /** 
     * @note
     * Fields like {@code List<List<RepetitionsModel>>} can actually be updated 
     * through generic function in {@link IBaseController}
    */
    @Override
    public void updateWithSpecificRule(
        ExerciseRecord      newEntity, 
        Map<String, Object> json
    ) throws EntityNotFoundException {
        if (json.containsKey("username")) {
            String username = (String) json.get("username");
            User user = userRepository.findByName(username)
                        .orElseThrow(()-> new EntityNotFoundException(entityNotFoundMsg(username)));
            newEntity.setUser(user);
        }
        if (json.containsKey("datetime")) {
            String datetimeStr = (String) json.get("datetime");
            Date datetime      = TimeUtility.formatDateTimeStr(datetimeStr);
            newEntity.setDate(datetime);
        }
        if (json.containsKey("sets")) {
            List<List<Integer>> sets = (List<List<Integer>>) json.get("sets");
            List<RepetitionsModel> mSets = sets.stream()
                .map((s) -> {
                    return new RepetitionsModel(s);
                }).collect(Collectors.toList());
            newEntity.setSets(mSets);
        }
        if (json.containsKey("location")) {
            String   locationStr = (String) json.get("location");
            String[] parts       = locationStr.split(",");
            
            if (parts.length == 2) {
                try {
                    Location newLocation = new Location(parts[0].trim(), parts[1].trim());
                    newEntity.setLocation(newLocation);
                } catch (NumberFormatException e) {
                    // Log error or handle the situation when parsing fails
                    System.err.println("Invalid latitude or longitude format: " + locationStr);
                }
            } else {
                throw new IllegalArgumentException("Incorrect format for location.");
            }
        }
    }




    
}
