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

import com.thirteenseven.workoutdiary.dao.UserRepository;
// ~~~~~~~~ thirteen ~~~~~~~~
import com.thirteenseven.workoutdiary.dao.interfaces.*;
import com.thirteenseven.workoutdiary.dao.interfaces.INameRepository;
import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;
import com.thirteenseven.workoutdiary.model.User;
import com.thirteenseven.workoutdiary.model.base.MongoBaseModel;
import com.thirteenseven.workoutdiary.model.base.NameBasedModel;
import com.thirteenseven.workoutdiary.model.base.RecordBaseModel;
import com.thirteenseven.workoutdiary.payload.response.DataResponse;




public interface IRecordController<T extends RecordBaseModel> 
extends 
    IBaseController<T> 
{
    /**
     * Override the main model repository in a RestController with this 
     * function. The functions defined in this interface are related to 
     * {@link IBaseRepository} and {@link IRecordRepository}
     */
    @Override
    IRecordRepository<T, String> getModelRepo();
    UserRepository               getUserRepo();





    
    /**
     * Get a record document based on specific users.
     * 
     * @param users usernames
     * @return DataResponse containing the requested document
     * @throws EntityNotFoundException if the name cannot be found
     */
    @GetMapping(path = "/getByUsernames", params = "usernames")
    default DataResponse<List<List<T>>> getByNames(
        @RequestParam List<String> usernames

    ) throws EntityNotFoundException {
        List<List<T>> entities = collectEntitiesByUsers(usernames);
        return new DataResponse<>(entities, "Found entity(s).");
    }


    // =========================================================================
    // Utility
    // =========================================================================
    default List<List<T>> collectEntitiesByUsers(List<String> names) {
        return names.stream()
            .map(name -> {
                try {
                    User usr = this.getUserRepo().findByName(name)
                               .orElseThrow(() -> new EntityNotFoundException(entityNotFoundMsg(name)));
                    List<T> entity = getModelRepo().findByUser(usr);
                    return entity;
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
