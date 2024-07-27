/** ================================================================
| IBaseController.java  --  WorkoutDiary/server/workoutdiary/src/main/java/com/thirteenseven/workoutdiary/controller/interfaces/IBaseController.java
|
| Created by Jack on 05/10, 2024
| Copyright © 2024 jacktogon. All rights reserved.
================================================================= */
package com.thirteenseven.workoutdiary.controller.interfaces;

// ~~~~~~~~ standard ~~~~~~~~
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ~~~~~~~~ Spring ~~~~~~~~
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
// ~~~~~~~~ thirteen ~~~~~~~~
import com.thirteenseven.workoutdiary.dao.interfaces.*;
import com.thirteenseven.workoutdiary.dao.interfaces.INameRepository;
import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;
import com.thirteenseven.workoutdiary.model.base.MongoBaseModel;
import com.thirteenseven.workoutdiary.model.base.NameBasedModel;
import com.thirteenseven.workoutdiary.payload.response.DataResponse;




/**
 * Name-related interface for REST API
 */
public interface INameController<T extends NameBasedModel> extends IBaseController<T> {
    
    /**
     * Override the main model repository in a RestController with this 
     * function. The functions defined in this interface are related to 
     * {@link IBaseRepository} and {@link INameRepository}
     */
    @Override
    INameRepository<T, String> getModelRepo();

    // ==========================================================================
    // post
    // ==========================================================================
    /**
     * This API will create and save an Entity with only general fields like Name.
     * 
     * @apiNote You should override {@link createEntity} if necessary.
     * @param json
     * @return
     * @throws EntityNotFoundException
     */
    @PostMapping(path = "/add/namelist")
    private DataResponse<List<T>> addnamelist(
        @RequestBody(required = true) Map<String, List<String>> json

    ) throws EntityNotFoundException {
        List<T> entities = new ArrayList<>();
        for (String name : json.getOrDefault("names", new ArrayList<>())) {
            T newEntity = newEntityByName(name);
            this.getModelRepo().save(newEntity);
            entities.add(newEntity);
        }
        return entities.size() > 0 ? 
            new DataResponse<>(entities, "Added entity(s).") : 
            new DataResponse<>(entities, "Nothing is added.");
    }

    /** This method should be override by child class if we want to use the funtion. */
    default T newEntityByName(String name) {
        throw new UnsupportedOperationException("Unsupported model type for name-based creation.");
    }

    
    // ==========================================================================
    // Get
    // ==========================================================================
    /**
     * Get a document via name parameter.
     * 
     * @param name name of the document
     * @return DataResponse containing the requested document
     * @throws EntityNotFoundException if the name cannot be found
     */
    @GetMapping(path = "/getByNames", params = "names")
    default DataResponse<List<T>> getByNames(
        @RequestParam List<String> names

    ) throws EntityNotFoundException {
        List<T> entities = collectEntitiesByNames(names);
        return new DataResponse<>(entities, "Found entity(s).");
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
    @PutMapping(path = "/addOrModifyFieldByNames", params = "names")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    default DataResponse<List<T>> addOrModifyFieldByNames(
        @RequestParam   List<String>       names,
        @RequestBody    FieldUpdateRequest body
    ) throws EntityNotFoundException {
        List<T> entities = collectEntitiesByNames(names);

        entities.forEach(entity -> 
            this.getModelRepo().addOrModifyField(
                entity, body.getFieldName(), body.getFieldTypeClass(), body.getValue()
            )
        );

        return new DataResponse<>(entities, entityAddOrModifiedFieldMsg(names.toString()));
    }

    
    
    
    // ==========================================================================
    // del
    // ==========================================================================
    /**
     * Delete a document name.
     * 
     * @param name optional name of the document to delete
     * @return DataResponse containing the deleted document
     * @throws EntityNotFoundException if neither the ID nor name can be found
     */
    @DeleteMapping(path = "/deleteByNames", params = "names")
    default DataResponse<List<T>> deleteByNames(
        @RequestParam List<String> names
    ) throws EntityNotFoundException {
                
        List<T> entities = collectEntitiesByNames(names);

        entities.forEach(entity -> {
            this.getModelRepo().delete(entity);
        });

        return new DataResponse<>(entities, "Deleted entities by name(s).");
    }








    // =========================================================================
    // Utility
    // =========================================================================
    default List<T> collectEntitiesByNames(List<String> names) {
        return names.stream()
            .map(name -> {
                try {
                    return this.getModelRepo().findByName(name)
                               .orElseThrow(() -> new EntityNotFoundException(entityNotFoundMsg(name)));
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

}