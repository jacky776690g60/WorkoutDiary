package com.thirteenseven.workoutdiary.model;

import java.beans.Transient;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Muscle Group model */
@Document(collection = "MuscleGroup")
public class MuscleGroup {
    // public static enum EMuscleGroup {
    //     BICEPS,
    //     TRICEPS,
    //     ABDOMINAL_MUSCLE,
    //     CORE,
    //     QUATDS,
    //     GLUTEUS,
    //     HAMSTRINGS,
    //     CALVES,
    //     QUADRICEPS,
    //     DELTOIDS,
    //     // above added
    // }

    // ==============================
    // Variables
    // ==============================
    @MongoId
    private ObjectId id;
    @Indexed(unique = true)
    private String name;

    // ==============================
    // Constructors
    // ==============================
    public MuscleGroup(
        @JsonProperty(value = "name") String name
    ) {
        this.name = name.toUpperCase();
    }

    // ==============================
    // Getters & Setters
    // ==============================
    @Transient
    public ObjectId getObjectId() {
        return id;
    }
    public String getId() {
        return id.toString();
    }

    
    public String getName() {
        return name;
    }
}
