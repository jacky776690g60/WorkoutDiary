package com.thirteenseven.workoutdiary.dao;

import java.util.Optional;

import com.thirteenseven.workoutdiary.model.PhysiqueRecord;
import com.thirteenseven.workoutdiary.model.User;

public interface PhysiqueRecordRepository extends MRecordRepository<PhysiqueRecord, String> {
    // Optional<PhysiqueRecord> findByUser(User user);
}