package com.thirteenseven.workoutdiary.service.base;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.ReflectionUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


import com.mongodb.client.result.UpdateResult;
import com.thirteenseven.workoutdiary.model.base.MongoBaseModel;



public abstract class BaseDocumentService {

    static Logger logger              = LoggerFactory.getLogger(BaseDocumentService.class);

    
    protected abstract MongoTemplate getMongoTemplate();




    public <T> List<T> queryOnClass(Query query, Class<T> clazz) {
        return getMongoTemplate().find(query, clazz);
    }


    public <T> Page<T> findPaginated(Query query, Class<T> clazz, Pageable pageable) {
        // Fetch the data within the page limits
        List<T> items = getMongoTemplate().find(query.with(pageable), clazz);
    
        // Count total matching documents
        long totalItems = getMongoTemplate().count(Query.of(query).limit(-1).skip(-1), clazz);
    
        return new PageImpl<>(items, pageable, totalItems);
    }
    



    

    /** Remove a field completely from MongoDB */
    public void removeField(MongoBaseModel entity, String fieldName, Class<?> clazz) {
        /** Direct-modification on Database */
        Query query   = new Query(Criteria.where("id").is(entity.getId()));
        Update update = new Update().unset(fieldName);
        UpdateResult result = getMongoTemplate().updateMulti(query, update, entity.getClass());
        /** Dealing with update on in-memory object */
        Field field = ReflectionUtils.findField(entity.getClass(), fieldName);
        if (field != null) {
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, entity, null);
        }
        if (result.getModifiedCount() == 0) 
            throw new UnsupportedOperationException(fieldName + " not found on id: " + entity.getId());
        logger.debug("Affected documents: " + result.getModifiedCount());
    }


    public <T> List<T> projectWithExclude(Class<T> clazz, String... fieldNames) {
        Query query = new Query();
        for (String fieldName : fieldNames) {
            query.fields().exclude(fieldName);
        }
        logger.debug(clazz.toString());
        return getMongoTemplate().find(query, clazz);
    }


    public <T> List<T> projectWithInclude(Class<T> clazz, String... fieldNames) {
        Query query = new Query();
        for (String fieldName : fieldNames) {
            query.fields().include(fieldName);
        }
        logger.debug(clazz.toString());
        return getMongoTemplate().find(query, clazz);
    }



    // public void renameField(String collectionName, String oldFieldName, String newFieldName) {
    //     // Create an instance of the update operation
    //     Update update = new Update().rename(oldFieldName, newFieldName);

    //     // Apply this update to all documents in the collection
    //     getMongoTemplate().updateMulti(new Query(), update, collectionName);
    // }
    public void renameField(String collectionName, String oldFieldName, String newFieldName, Criteria criteria) {
        // Create an instance of the update operation
        Update update = new Update().rename(oldFieldName, newFieldName);
    
        // Apply this update to documents matching the specified criteria in the collection
        UpdateResult result = getMongoTemplate().updateMulti(new Query(criteria), update, collectionName);
        System.out.println("Modified documents: " + result.getModifiedCount());

    }
    


}
