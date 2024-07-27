package com.thirteenseven.workoutdiary.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.thirteenseven.workoutdiary.service.base.BaseDocumentService;

@Service
public class ExerciseService extends BaseDocumentService {
    
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
    
}
