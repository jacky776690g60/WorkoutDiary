package com.thirteenseven.workoutdiary.exception;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;

public class EntityNotFoundException extends NotFoundException {

    private String message;
    private Object obj;

    // ==============================
    // Constructors
    // ==============================
    public EntityNotFoundException() {
        super();
    }

    public EntityNotFoundException(String message) {
        super();
        this.message = message;

    }

    public <T> EntityNotFoundException(T obj, String message) {
        super();
        this.message = message;
        this.obj = obj;
    }

    public EntityNotFoundException(Class<?> clazz, String message) {
        super();
        this.message = message;
        this.obj = clazz.getName();
    }

    
    // ==============================
    // Getters & Setters
    // ==============================
    public String getMessage() {
        return (this.obj != null) ? String.format("%s %s", this.obj.toString(), this.message) :
               String.format("%s", this.message);
    }
}
