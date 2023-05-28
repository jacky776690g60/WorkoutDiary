package com.thirteenseven.workoutdiary.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thirteenseven.workoutdiary.dao.MBaseRepository;
import com.thirteenseven.workoutdiary.dao.MRecordRepository;
import com.thirteenseven.workoutdiary.dao.PhysiqueRecordRepository;
import com.thirteenseven.workoutdiary.dao.UserRepository;
import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;
import com.thirteenseven.workoutdiary.model.PhysiqueRecord;
import com.thirteenseven.workoutdiary.model.User;
import com.thirteenseven.workoutdiary.payload.response.DataResponse;
import com.thirteenseven.workoutdiary.utilities.MUtility;

import jakarta.validation.Valid;

@RequestMapping("api/v1/physiqueRecord")
@RestController
public class PhysiqueRecordController implements IRecordController<PhysiqueRecord> {
    
    @Autowired
    private PhysiqueRecordRepository physiqueRecordRepo;
    @Autowired
    private UserRepository userRepo;
    

    @Override
    public MRecordRepository<PhysiqueRecord, String> getModelRepo() {
        return this.physiqueRecordRepo;
    }

    @Override
    public UserRepository getUserRepo() {
        return this.userRepo;
    }



    // ==============================
    // REST API
    // ==============================
    @PostMapping("/add")
    public DataResponse<PhysiqueRecord> add(
        @RequestParam String username,
        @RequestParam(required = false) String datetime,
        @Valid @RequestBody PhysiqueRecord entity
        // @RequestBody Map<String, Object> payload
    ) throws EntityNotFoundException {
        User user = userRepo.findByName(username)
            .orElseThrow(()-> new EntityNotFoundException("Username not found."));
        final Date targetDate = (datetime == null) ? MUtility.to30MinInterval(new Date()) : 
                                        MUtility.to30MinInterval(MUtility.formatDateTimeStr(datetime));
        entity.setDate(targetDate);
        entity.setUser(user);

        PhysiqueRecord record = physiqueRecordRepo.findByUserDate(username, targetDate).orElse(entity);
        if (record != entity) {
            record.setWeight(entity.getWeight());
            record.setHeight(entity.getHeight());
        }
    
        physiqueRecordRepo.save(record);
        return new DataResponse<PhysiqueRecord>(record);
    }
    

    

}
