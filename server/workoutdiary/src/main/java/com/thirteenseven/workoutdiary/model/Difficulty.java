package com.thirteenseven.workoutdiary.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

/** Exercise difficulty model */
@Document(collection = "Difficulty")
public class Difficulty {
    public static enum EDifficulty {
        EASY,
        INTERMEDIATE,
        ADVANCED,
        EXPERT,
    }

    // ==============================
    // Variables
    // ==============================
    @MongoId
    private ObjectId id;
    private EDifficulty name;

    // ==============================
    // Constructor
    // ==============================
    @PersistenceCreator
    public Difficulty(EDifficulty name) {
        this.name = name;
    }

    // ==============================
    // Getters & Setters
    // ==============================
    // public ObjectId getObjectId() {
    //     return id;
    // }
    public String getId() {
        return id.toString();
    }


    public EDifficulty getName() {
        return name;
    }
}
