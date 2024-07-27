/** =====================================================================================
| IBaseController.java  --  WorkoutDiary/server/workoutdiary/src/main/java/com/thirteenseven/workoutdiary/controller/interfaces/IBaseController.java
|
| Created by Jack on 05/10, 2024
| Copyright © 2024 jacktogon. All rights reserved.
====================================================================================== */
package com.thirteenseven.workoutdiary.controller.interfaces;

import java.io.IOException;
// ~~~~~~~~ standard ~~~~~~~~
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// ~~~~~~~~ Spring ~~~~~~~~
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
// ~~~~~~~~ thirteenseven ~~~~~~~~
import com.thirteenseven.workoutdiary.dao.interfaces.*;
import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;
import com.thirteenseven.workoutdiary.model.annotations.PutUpdatableField;
import com.thirteenseven.workoutdiary.model.base.MongoBaseModel;
import com.thirteenseven.workoutdiary.payload.response.DataResponse;
import com.thirteenseven.workoutdiary.service.base.BaseDocumentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;




import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * This class is the base class of all Controllers. It mostly deal with _id related
 * CRUD.
 */
public interface IBaseController<T extends MongoBaseModel> {

    static Logger logger              = LoggerFactory.getLogger(IBaseController.class);

    /**
     * Override the main model repository in a RestController with this function. 
     * The functions defined in this interface are related to {@link IBaseRepository}.
     */
    IBaseRepository<T, String>  getModelRepo();
    /** Provide the PasswordEncoder for this. */
    default PasswordEncoder getEncoder() {
        throw new UnsupportedOperationException("Unimplemented method 'getEncoder'");
    }
    /** A corresponding service if necessary */
    default BaseDocumentService getDocumentService() {
        throw new UnsupportedOperationException("Unimplemented method 'getDocumentService'");
    };


    // ==========================================================================
    // post
    // ==========================================================================


    // ==========================================================================
    // Get
    // ==========================================================================
    /**
     * Get a document via id parameters.
     * 
     * @param id   ID of the document
     * @return DataResponse containing the requested document
     * @throws EntityNotFoundException if the ID cannot be found
     */
    @GetMapping(path = "/getByIds", params = "ids")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    default DataResponse<List<T>> getById(
        @NonNull @RequestParam List<String> ids
    ) throws EntityNotFoundException {
        List<T> entities = collectEntitiesById(ids);
        return new DataResponse<>(entities, "Found Id(s)");
    }



    /**
     * Retrieve all documents.
     * 
     * @return DataResponse containing the list of all documents
     */
    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ADMIN')")
    default DataResponse<List<T>> getAll() {
        List<T> ls = this.getModelRepo().findAll();
        return new DataResponse<List<T>>(ls);
    }


    /**
     * Retrieve all documents' IDs.
     */
    @GetMapping("/getAllIds")
    // @PreAuthorize("hasRole('ADMIN')")
    default DataResponse<List<String>> getAllIds(

    ) {
        List<T> ls = this.getModelRepo().findAll();
        List<String> ids = ls.stream()
            .filter(entity -> entity instanceof MongoBaseModel)
            .map(entity -> ((MongoBaseModel) entity).getId())
            .collect(Collectors.toList());

        return new DataResponse<List<String>>(ids, "All documents' ids.");
    }


    /**
     * Retrieve all documents' certain field.
     * @throws IllegalAccessException 
     */
    @GetMapping(path = "/queryField", params = "fieldName")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    default DataResponse<List<Object>> getAllIds(
        @NonNull @RequestParam String   fieldName,
        @RequestParam boolean           distinct
    ) throws IllegalAccessException {
        if (fieldName.contains(" "))
            throw new IllegalAccessException("Field name should not contains space");
        List<T> ls = this.getModelRepo().findAll();
    
        String funcName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    
        List<Object> values = ls.stream()
            .map(entity -> {
                try {
                    Method getMethod = entity.getClass().getMethod(funcName); // No parameters for standard getters
                    getMethod.setAccessible(true); // Make the method accessible if it's private/protected
                    return getMethod.invoke(entity);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    logger.error(e + " Error accessing " + funcName + " in " + entity.getClass().getName());
                    return null;
                }
            }).collect(Collectors.toList());

        if (distinct) {
            values = new ArrayList<>(new HashSet<>(values));
        }
    
        return new DataResponse<>(values, "Queried Field(s) and called function: " + funcName);
    }










    @GetMapping("/projectWithExclude")
    default <T> DataResponse<List<T>> projectWithExclude(
            @RequestParam String       className,
            @RequestParam List<String> fields) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) Class.forName(className);
            List<T> result = getDocumentService().projectWithExclude(clazz, fields.toArray(new String[0]));
            return new DataResponse<>(result, "Exclude fields projection!");
        } catch (ClassNotFoundException e) {
            return new DataResponse<>(null, "Class not found: " + className);
        } catch (Exception e) {
            logger.error("Error during projection: " + e.getMessage());
            return new DataResponse<>(null, "Error during projection");
        }
    }


    @GetMapping("/projectWithInclude")
    default <T> DataResponse<List<T>> projectWithInclude(
            @RequestParam String       className,
            @RequestParam List<String> fields) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) Class.forName(className);
            List<T> result = getDocumentService().projectWithInclude(clazz, fields.toArray(new String[0]));
            return new DataResponse<>(result, "Include fields projection!");
        } catch (ClassNotFoundException e) {
            return new DataResponse<>(null, "Class not found: " + className);
        } catch (Exception e) {
            logger.error("Error during projection: " + e.getMessage());
            return new DataResponse<>(null, "Error during projection");
        }
    }


















    /**
     * Finds objects by their names in the given repository.
     *
     * @param <T> The type of objects in the repository.
     * @param repo The repository to search for the objects.
     * @param queryParam An array of names to search for.
     * @return A list of objects found in the repository.
     * @throws EntityNotFoundException if strict is true and one or more required named objects are not found.
     */
    default <S> List<S> findByNameInSpecificRepo(
        INameRepository<S, String> repo,
        String[]                   queryParam
    ) throws EntityNotFoundException {
        List<S> objects = new ArrayList<>();
        for (String s : queryParam) {
            S obj = repo.findByName(s)
                        .orElseThrow(() -> new EntityNotFoundException(entityNotFoundMsg(s)));
            if (obj != null) objects.add(obj);
        }
        return objects;
    }






    














    // ==========================================================================
    // put
    // ==========================================================================
    /**
     * Add or modify a field of the specified type and value.
     * 
     * @apiNote
     * The targeted Model class must have that new property set up.
     * 
     * @return DataResponse containing the list of all documents
     */
    @PutMapping(path = "/addOrModifyFieldByIds", params = "ids")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    default DataResponse<List<T>> addOrModifyFieldById(
        @NonNull @RequestParam List<String> ids,
        @NonNull @RequestBody FieldUpdateRequest body
    ) throws EntityNotFoundException {
        List<T> entities = collectEntitiesById(ids);

        entities.forEach(entity -> 
            this.getModelRepo().addOrModifyField(
                entity, body.getFieldName(), body.getFieldTypeClass(), body.getValue()
            )
        );

        return new DataResponse<>(entities, entityAddOrModifiedFieldMsg(ids.toString()));
    }



    

    @PutMapping(path = "/removeFieldByIds", params = {"ids", "fieldName"})
    @PreAuthorize("hasRole('ADMIN')")
    default DataResponse<List<T>> removeFieldByIds(
        @NonNull @RequestParam List<String> ids,
        @NonNull @RequestParam String       fieldName
    ) throws EntityNotFoundException {
        List<T> entities = collectEntitiesById(ids);

        List<T> abc = entities.stream().map(entity -> {
            getDocumentService().removeField(entity, fieldName, entity.getClass());
            return entity;
        }).collect(Collectors.toList());

        return new DataResponse<>(abc, fieldName + " removed from: " + ids.toString());
    }





















    


    


    /**
     * This function is used to update specific fields in an Entity. These fields
     * cannot be processed through standard constructor. They may need to access
     * another repository.
     * 
     * @param newEntity temporary new Entity
     * @param Json      client-sent Json data
     */
    default void updateWithSpecificRule(T newEntity, Map<String, Object> json) throws EntityNotFoundException {};


    

    
    /**
     * Update an entity via id and new data object.
     * 
     * @param id ID of the document
     * @param json Object containing the fields to update
     * @return DataResponse containing the updated entity
     * @throws EntityNotFoundException if the ID cannot be found
     * @throws IOException 
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    @PutMapping("/updateById")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    default DataResponse<T> updateEntityById(
        // @PathVariable String id, /** For reference */
        @RequestParam String id,
        @RequestBody Map<String, Object> json
    ) throws 
        EntityNotFoundException, IllegalArgumentException, IOException, 
        InstantiationException, IllegalAccessException, InvocationTargetException, 
        NoSuchMethodException, SecurityException 
    {
        try {
            T oldEntity = this.getModelRepo().findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getClass(), entityNotFoundMsg(id)));
            T newEntity = (T) createEntityFromMap(oldEntity.getClass(), json);
            
            updateWithSpecificRule(newEntity, json);

            List<String>  notUpdatedFields = updateEntityFields(oldEntity, newEntity);
            StringBuilder sb               = new StringBuilder();
            
            sb.append("Entity updated.");
            if (notUpdatedFields.size() > 0) {
                sb.append(" Unchaged fields: ");
                sb.append(notUpdatedFields.toString());
            }

            this.getModelRepo().save(oldEntity);
            return new DataResponse<>(oldEntity, sb.toString());
        
        } catch (Exception e) { 
            logger.error("Error occured in " + getClass() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return new DataResponse<>(null, entityNotUpdateMsg(id));
    }















    // ==========================================================================
    // del
    // ==========================================================================
    /**
     * Delete a document by ID.
     * 
     * @param id   optional ID of the document to delete
     * @return DataResponse containing the deleted document
     * @throws EntityNotFoundException if neither the ID nor name can be found
     */
    @DeleteMapping(path = "/deleteByIds", params = "ids")
    default DataResponse<List<T>> deleteById(
        @NonNull @RequestParam List<String> ids
    ) throws EntityNotFoundException {

        List<T> entities = collectEntitiesById(ids);

        ids.forEach(id -> {
            this.getModelRepo().deleteById(id);
        });
        
        return new DataResponse<>(entities, entityDeletedMsg(ids.toString()));
    }


    /**
     * Delete all documents.
     * 
     * @return DataResponse containing the number of deleted documents
     */
    @DeleteMapping("/deleteAll")
    @PreAuthorize("hasRole('ADMIN')")
    default DataResponse<Integer> deleteAll() {
        List<T> recordLs = this.getModelRepo().findAll();
        this.getModelRepo().deleteAll();
        return new DataResponse<Integer>(recordLs.size(), entityDeletedMsg("All"));
    }


    /**
     * Soft delete a document by ID.
     * 
     * @param id   optional ID of the document to delete
     * @return DataResponse containing the deleted document
     * @throws EntityNotFoundException if neither the ID nor name can be found
     */
    @DeleteMapping(path = "/softDeleteByIds", params = "ids")
    default DataResponse<List<T>> softDeleteById(
        @NonNull @RequestParam List<String> ids
    ) throws EntityNotFoundException {
        final List<T> entities = collectEntitiesById(ids);
        entities.clear();
        ids.forEach(id -> {
            entities.add(this.getModelRepo().softDeleteById(id));
        });

        return new DataResponse<>(entities, entitySoftDeleteMsg(ids.toString()));
    }
    /**
     * Soft delete a document by ID.
     * 
     * @param id   optional ID of the document to delete
     * @return DataResponse containing the deleted document
     * @throws EntityNotFoundException if neither the ID nor name can be found
     */
    @DeleteMapping(path = "/unsoftDeleteByIds", params = "ids")
    default DataResponse<List<T>> unsoftDeleteById(
        @NonNull @RequestParam List<String> ids
    ) throws EntityNotFoundException {
        final List<T> entities = collectEntitiesById(ids);
        entities.clear();
        ids.forEach(id -> {
            entities.add(this.getModelRepo().unsoftDeleteById(id));
        });

        return new DataResponse<>(entities, entitySoftDeleteMsg(ids.toString()));
    }




    
    // ========================================================================
    // Utility
    // ========================================================================
    default String entityNotFoundMsg(String entityIdentifier) { 
        return "Entity(s) cannot be found by: " + entityIdentifier; 
    };
    default String entityAlreadyExistsMsg(String entityIdentifier) { 
        return "Entity already exists on : " + entityIdentifier; 
    };
    default String entitySoftDeleteMsg(String entityIdentifier) { 
        return "Entity(s) soft deleted on: " + entityIdentifier; 
    };
    default String entitySoftDeleteRestoredMsg(String entityIdentifier) { 
        return "Entity(s) no longer soft deleted on: " + entityIdentifier; 
    };
    default String entityDeletedMsg(String entityIdentifier) { 
        return "Entity(s) deleted on: " + entityIdentifier; 
    };
    default String entityNotUpdateMsg(String entityIdentifier) { 
        return "Entity(s) not updated on: " + entityIdentifier; 
    };
    default String entityAddOrModifiedFieldMsg(String entityIdentifier) { 
        return "Added/Modified field with specified value on entity(s): " + entityIdentifier; 
    };
    /**
     * Used the provided repo to locate all entities with related IDs
     * @param ids List of _id in MongoDB
     */
    default List<T> collectEntitiesById(List<String> ids) {
        return ids.stream()
                .map(id -> {
                    try {
                        return this.getModelRepo().findById(id)
                                .orElseThrow(() -> new EntityNotFoundException(entityNotFoundMsg(id)));
                    } catch (EntityNotFoundException e) {
                        /**
                         * It is an unchecked exception, meaning it doesn't need to
                         * be declared in the method signature or explicitly caught,
                         * making it convenient for use in lambda expressions where
                         * checked exceptions are not allowed.
                         */
                        throw new IllegalStateException(e); // Wrap the checked exception
                    }
                })
                .collect(Collectors.toList());
    }
    












    /**
     * Checks if a new value is invalid based on annotation constraints.
     *
     * @param newValue the new value to check
     * @param annotation the annotation specifying the constraints
     * @return true if the value is invalid, false otherwise
     */
    private boolean isInvalid(
        Object            newValue, 
        PutUpdatableField annotation
    ) {
        if (annotation.notNull()) {
            if (newValue == null) return true;
            if (newValue instanceof String && ((String) newValue).trim().isEmpty()) return true;
        }
        return false;
    }
    /**
     * Instantiates an object of the specified class using a map of field data.
     * This method finds a constructor in the given class that is annotated 
     * specifically for this purpose and uses the provided field data to 
     * instantiate the object via reflection.
     *
     * @param <T> the type of the object to create
     * @param clazz the Class object corresponding to the class of T
     * @param fieldData a map containing field names and their corresponding values to be used for creating the object
     * @return an instance of T, constructed using the provided field data
     * @throws ReflectiveOperationException if any reflection-based operations (such as finding constructors or instantiating the class) fail
     */
    private <T> T createEntityFromMap(
        Class<T>            clazz,
        Map<String, Object> fieldData
    ) throws ReflectiveOperationException {
        Constructor<T> kConstructor = null;

        System.out.println(fieldData.toString());
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            boolean isAnnotatedProperly = true;
            for (java.lang.reflect.Parameter parameter : constructor.getParameters()) {
                if (parameter.getAnnotation(JsonProperty.class) == null) {
                    isAnnotatedProperly = false;
                    break;
                }
            }
            if (isAnnotatedProperly) {
                kConstructor = (Constructor<T>) constructor;
                return instantiateUsingConstructor(kConstructor, fieldData);
            }
        }
        throw new NoSuchMethodException("No suitable constructor found for " + clazz.getName());
    }
    /**
     * Creates an instance of a class using a specific constructor and a map of 
     * field data. This method constructs an object by dynamically matching 
     * constructor parameters with provided field data based on annotations.
     * 
     * @note
     * Each parameter in the constructor is expected to be annotated with 
     * {@link JsonProperty} to indicate the corresponding key in the field data 
     * map. The method handles the creation of {@link Optional} types if the 
     * constructor expects them.
     *
     * @param <T> the type of the object to be instantiated
     * @param constructor the constructor of the class T to be used for instantiation
     * @param fieldData a map containing the data for fields, where each key corresponds to the value specified in the {@link JsonProperty} annotation of constructor parameters
     * @return an instance of T created using the provided constructor and field data
     * @throws ReflectiveOperationException if any reflection operations, like accessing the constructor or instantiating the class, fail
     */
    private <T> T instantiateUsingConstructor(Constructor<T> constructor, Map<String, Object> fieldData) throws ReflectiveOperationException {
        Object[] params = new Object[constructor.getParameterCount()];
        int i = 0;
        for (java.lang.reflect.Parameter parameter : constructor.getParameters()) {
            JsonProperty annotation = parameter.getAnnotation(JsonProperty.class);
            Object value = fieldData.get(annotation.value());
            if (parameter.getType().equals(Optional.class)) {
                params[i++] = Optional.ofNullable(value);
            } else {
                params[i++] = value;
            }
        }
        return constructor.newInstance(params);
        
    }
    /**
     * By default, this method will be able to handle the fields with correct 
     * constructor provided in model classes. But it cannot handle fields which
     * requires referencing another repository.
     */
    private List<String> updateEntityFields(
        T oldEntity, 
        T newEntity
    ) {
        List<String> notUpdatedFields = new ArrayList<>();
        Class<?> clazz = newEntity.getClass();
    
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) { // getDeclaredFields finds functions on current class only
                field.setAccessible(true);
                PutUpdatableField annotation = field.getAnnotation(PutUpdatableField.class);
                if (annotation == null) continue; // if not marked as PUT updatable
    
                try {
                    Object newValue = field.get(newEntity);
    
                    if (isInvalid(newValue, annotation)) {
                        notUpdatedFields.add(field.getName());
                        continue;
                    }
    
                    if (newValue != null) {
                        if (newValue instanceof String && annotation.encoded()) {
                            field.set(oldEntity, getEncoder().encode((String) newValue));
                        } else {
                            field.set(oldEntity, newValue);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            clazz = clazz.getSuperclass(); // move to the superclass
        }
        return notUpdatedFields;
    }




}





@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class FieldUpdateRequest {
    private String fieldName;
    private String fieldTypeClass;
    private String value;
}