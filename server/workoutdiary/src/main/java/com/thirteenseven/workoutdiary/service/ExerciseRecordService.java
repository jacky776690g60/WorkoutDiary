package com.thirteenseven.workoutdiary.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.thirteenseven.workoutdiary.model.ExerciseRecord;
import com.thirteenseven.workoutdiary.service.base.BaseDocumentService;

@Service
public class ExerciseRecordService extends BaseDocumentService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
