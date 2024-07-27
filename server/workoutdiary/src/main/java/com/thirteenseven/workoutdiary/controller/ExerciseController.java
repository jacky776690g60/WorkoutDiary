package com.thirteenseven.workoutdiary.controller;

// ~~~~~~~~ standard ~~~~~~~~
import java.util.*;
import java.util.stream.Collectors;

// ~~~~~~~~ Spring ~~~~~~~~
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

// ~~~~~~~~ Workout Diary ~~~~~~~~
import com.thirteenseven.workoutdiary.controller.interfaces.INameController;
import com.thirteenseven.workoutdiary.dao.DifficultyRepository;
import com.thirteenseven.workoutdiary.dao.ExerciseRepository;
import com.thirteenseven.workoutdiary.dao.ExerciseTypeRepository;
import com.thirteenseven.workoutdiary.dao.MuscleGroupRepository;
import com.thirteenseven.workoutdiary.dao.UserRepository;
import com.thirteenseven.workoutdiary.dao.interfaces.INameRepository;
import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;
import com.thirteenseven.workoutdiary.exception.UnauthorizedException;
import com.thirteenseven.workoutdiary.model.Difficulty;
import com.thirteenseven.workoutdiary.model.Exercise;
import com.thirteenseven.workoutdiary.model.ExerciseType;
import com.thirteenseven.workoutdiary.model.MuscleGroup;
import com.thirteenseven.workoutdiary.model.User;
import com.thirteenseven.workoutdiary.payload.request.ExercisePostRequest;
import com.thirteenseven.workoutdiary.payload.response.DataResponse;
import com.thirteenseven.workoutdiary.service.ExerciseService;
import com.thirteenseven.workoutdiary.service.base.BaseDocumentService;
import com.thirteenseven.workoutdiary.utilities.JwtUtility;

import jakarta.validation.Valid;

@RequestMapping("api/v1/exercise")
@RestController
public class ExerciseController implements INameController<Exercise>  {
    
    // ~~~~~~~~ dependency injection ~~~~~~~~
    @Autowired
    private ExerciseRepository      exerciseRepo;
    @Autowired 
    private ExerciseService         exerciseService;
    @Autowired
    private ExerciseTypeRepository  exerciseTypeRepository;
    @Autowired
    private UserRepository          userRepository;
    @Autowired
    private JwtUtility              jwtUtility;
    @Autowired
    private DifficultyRepository    difficultyRepo;
    @Autowired
    private MuscleGroupRepository   muscleGroupRepo;



    @Override
    public PasswordEncoder getEncoder() {
        throw new UnsupportedOperationException("Unimplemented method 'getEncoder'");
    }

    @Override
    public INameRepository<Exercise, String> getModelRepo() {
        return exerciseRepo;
    }

    @Override
    public BaseDocumentService getDocumentService() {
        return exerciseService;
    }


    // ==============================
    // REST
    // ==============================
    @GetMapping("/search")
    public DataResponse<List<Exercise>> search(
        @RequestParam(required = false) String[] difficulties,
        @RequestParam(required = false) String[] muscleGroups,
        @RequestParam(required = false) String[] exerciseTypes,
        @RequestParam(required = false) String   substring,
        @RequestParam(required = false) String   authorNames,
        @RequestParam(required = false, defaultValue = "false") boolean strict,
        @RequestParam(required = false, defaultValue = "0") int page, // Default page index is 0
        @RequestParam(required = false, defaultValue = "5") int size // Default page size is 5
    ) throws EntityNotFoundException {
        Query query = new Query();

        if (difficulties != null) {
            List<Difficulty> difficultyObjs = findByNameInSpecificRepo(difficultyRepo, difficulties);
            
            if (!difficultyObjs.isEmpty()) {
                if (strict) query.addCriteria(Criteria.where("difficulty").all(difficultyObjs));
                else        query.addCriteria(Criteria.where("difficulty").in(difficultyObjs));
            }
        }

        if (muscleGroups != null && muscleGroups.length > 0) {
            List<MuscleGroup> muscleGroupObjects = findByNameInSpecificRepo(muscleGroupRepo, muscleGroups);
            if (!muscleGroupObjects.isEmpty()) {
                if (strict) query.addCriteria(Criteria.where("muscleGroups").all(muscleGroupObjects));
                else        query.addCriteria(Criteria.where("muscleGroups").in(muscleGroupObjects));
            }
        }
        
        if (exerciseTypes != null && exerciseTypes.length > 0) {
            List<ExerciseType> exerciseTypeObjs = findByNameInSpecificRepo(exerciseTypeRepository, exerciseTypes);
            if (!exerciseTypeObjs.isEmpty()) {
                if (strict) query.addCriteria(Criteria.where("exerciseType").all(exerciseTypeObjs));
                else        query.addCriteria(Criteria.where("exerciseType").in(exerciseTypeObjs));
            }
        }

        if (substring != null) {
            if (strict) {
                query.addCriteria(Criteria.where("name").regex(substring, "i")); // Case-insensitive substring search
            } else {
                Criteria nameCriteria        = Criteria.where("name").regex(substring, "i");
                Criteria descriptionCriteria = Criteria.where("description").regex(substring, "i");
                query.addCriteria(new Criteria().orOperator(nameCriteria, descriptionCriteria));
            }
        }

        if (authorNames != null) {
            List<User> userObjs = findByNameInSpecificRepo(userRepository, new String[] {authorNames});
            if (!userObjs.isEmpty()) {
                if (strict) query.addCriteria(Criteria.where("author").all(userObjs));
                else        query.addCriteria(Criteria.where("author").in(userObjs));
            }
        }

        Pageable pageable = PageRequest.of(page, size);        
        List<Exercise> exercises = getDocumentService().queryOnClass(query.with(pageable), Exercise.class);


        Page<Exercise> resultPage = getDocumentService().findPaginated(query, Exercise.class, pageable);
        boolean hasNextPage = resultPage.getNumber() + 1 < resultPage.getTotalPages();
        logger.debug("Number (Idx) : " + resultPage.getNumber());
        logger.debug("Total Pages  : " + resultPage.getTotalPages());
        logger.debug("Has Next     : " + hasNextPage);


        return new DataResponse<>(exercises, "Success!", hasNextPage);
    }







    @PostMapping("/add")
    public DataResponse<Exercise> add(
        @RequestBody @Valid ExercisePostRequest exPostReq,
        @CookieValue(name = "token", required = true)    String token,
        @CookieValue(name = "username", required = true) String username
    ) throws EntityNotFoundException {
        final Difficulty difficulty = difficultyRepo.findByName(exPostReq.getDifficulty())
                                    .orElseThrow(()-> new EntityNotFoundException("Difficulty not found by name."));
        final Set<MuscleGroup> muscleGroups = new HashSet<MuscleGroup>(
                findByNameInSpecificRepo(muscleGroupRepo, exPostReq.getMuscleGroups().toArray(new String[0]))
            );

        Set<String> mainMuscleGroupIds = exPostReq.getMainMuscleGroupNames().stream()
            .map(mgName -> {
                try {
                    return muscleGroups.stream()
                        .filter(mg -> mg.getName().equals(mgName.toUpperCase()))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("Main muscle group not relevant with name: " + mgName))
                        .getId();
                } catch (EntityNotFoundException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toSet());


        
        String[] ciphers = jwtUtility.decipher(token);
        String tokenEmail           = ciphers[0];
        User user                   = userRepository.findByEmail(tokenEmail)
                                        .orElseThrow(() -> new EntityNotFoundException("User not found by email"));
        Exercise exercise = new Exercise(
            exPostReq.getName(), 
            exPostReq.getVideoURL(),
            muscleGroups,
            mainMuscleGroupIds,
            difficulty,
            exPostReq.getDescription()
        );
        exercise.setAuthor(user);

        exerciseRepo.save(exercise);

        return new DataResponse<Exercise>(exercise, "Added new exercise.");
    }









    // @PutMapping("/update/{id}")
    // public DataResponse<Exercise> update(
    //     @PathVariable String id,
    //     @RequestBody ExercisePostRequest addExerciseReq
    // ) throws EntityNotFoundException {
    //     Exercise exercise = exerciseRepo.findById(id).orElseThrow(()-> new EntityNotFoundException("Exercise not found by ID: " + id));

    //     if (addExerciseReq.getName()!= null && addExerciseReq.getName() != "")         exercise.setName(addExerciseReq.getName());
    //     if (addExerciseReq.getVideoURL()!= null && addExerciseReq.getVideoURL() != "") exercise.setVideoURL(addExerciseReq.getVideoURL());

    //     exerciseRepo.save(exercise);

    //     return new DataResponse<Exercise>(exercise, "Successfully modified!");
    // }

    // /** 
    //  * @note don't call the single update to prevent overhead.
    //  */
    // @PutMapping("/bulk-update")
    // public ResponseEntity<List<DataResponse<Exercise>>> bulkUpdate(
    //     @RequestParam String ids, // IDs as a comma-separated string
    //     @RequestBody ExercisePostRequest addExerciseReq
    // ) {
    //     List<DataResponse<Exercise>> responses = new ArrayList<>();
    //     String[] idArray = ids.split(",");

    //     for (String id : idArray) {
    //         try {
    //             Exercise exercise = exerciseRepo.findById(id).orElseThrow(()-> new EntityNotFoundException("Exercise not found by ID: " + id));
                
    //             if (addExerciseReq.getName()!= null && addExerciseReq.getName() != "")         exercise.setName(addExerciseReq.getName());
    //             if (addExerciseReq.getVideoURL()!= null && addExerciseReq.getVideoURL() != "") exercise.setVideoURL(addExerciseReq.getVideoURL());

    //             exerciseRepo.save(exercise);
    //             responses.add(new DataResponse<>(exercise, "Successfully modified!"));
    //         } catch (EntityNotFoundException e) {
    //             responses.add(new DataResponse<>(null, "Failed to modify Exercise with ID: " + id));
    //         }
    //     }

    //     return ResponseEntity.ok(responses);
    // }

}
