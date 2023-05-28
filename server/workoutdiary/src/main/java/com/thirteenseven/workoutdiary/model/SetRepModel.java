package com.thirteenseven.workoutdiary.model;

import java.util.*;

/** A model for counting repetition each set.
 * The length of the rep array is used to keep track of how many
 * sets are in this record.
 */
public class SetRepModel {
    // ==============================
    // Variables
    // ==============================
    /** Record how many repetitions in each set */
    private List<List<Integer>> setReps = new ArrayList<>();


    // ==============================
    // Constructor
    // ==============================
    public SetRepModel(final List<List<Integer>> setReps) {
        this.setReps = setReps;
    }


    // ==============================
    // functions
    // ==============================
    /** Get the total weight lift given a set number */
    public int getTotalWeightInSet(int setIdx) {
        List<Integer> ref = setReps.get(setIdx);
        int res = 0;
        
        for (Integer n : ref) res += n;

        return res;
    }
    
    // ==============================
    // Getters & Setters
    // ==============================
    public List<List<Integer>> getSetReps() {
        return setReps;
    }

}
