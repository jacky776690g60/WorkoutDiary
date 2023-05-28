package com.thirteenseven.workoutdiary.advice;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.thirteenseven.workoutdiary.exception.EntityNotFoundException;
import com.thirteenseven.workoutdiary.exception.UnauthorizedAttemptException;
import com.thirteenseven.workoutdiary.payload.response.SimpleResponse;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;

/** Exception handler for the RESTful api 
 * Filter exception won't be caught by this handler
 * @apiNote (In production) Should not return exception itself for security reason
*/
@RestControllerAdvice
public class RestExceptionHandler {
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public SimpleResponse<?> handleInvalidArgument(MethodArgumentNotValidException ex) {
        List<Object> ls = Arrays.asList(ex.getBindingResult().getFieldErrors());
        return new SimpleResponse<Exception>(ex); // tmp
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicateKeyException.class)
    public SimpleResponse<?> handleDuplicateKeyException(DuplicateKeyException ex) {
        return new SimpleResponse<Exception>(ex); // tmp
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingRequestValueException.class)
    public SimpleResponse<?> handleMissingFieldException(MissingRequestValueException ex) {
        return new SimpleResponse<Exception>(ex); // tmp
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EntityNotFoundException.class)
    public SimpleResponse<?> handleEntityNotFoundException(EntityNotFoundException ex) {
        return new SimpleResponse<NotFoundException>(ex); // tmp
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedAttemptException.class)
    public SimpleResponse<?> handleUnauthorizedAttemptException(UnauthorizedAttemptException ex) {
        return new SimpleResponse<Exception>(ex); // tmp
    }
}