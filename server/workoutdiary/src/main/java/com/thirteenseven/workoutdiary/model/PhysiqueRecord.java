package com.thirteenseven.workoutdiary.model;

import java.util.*;
import java.util.Map.Entry;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.util.Pair;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

@Document(collection="PhysiqueRecord")
@EqualsAndHashCode(callSuper = false)
public class PhysiqueRecord extends BaseRecord {
    // ==============================
    // Variables
    // ==============================
    @MongoId
    private ObjectId id;

    @NotNull(message = "weight cannot be null.")
    private Double weight; // lbs
    @NotNull(message = "height cannot be null.")
    private String height; // feet
    
    
    // ==============================
    // Constructor
    // ==============================
    
    // ==============================
    // Functions
    // ==============================
    

    // ==============================
    // Getters & Setters
    // ==============================
    // public ObjectId getObjectId() {
    //     return id;
    // }
    public String getId() {
        return id.toString();
    }

    
    public Double getWeight() {
        return weight;
    }
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    
    public String getHeight() {
        return height;
    }
    public void setHeight(String height) {
        this.height = height;
    }
}
