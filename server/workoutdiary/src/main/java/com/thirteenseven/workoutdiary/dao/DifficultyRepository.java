package com.thirteenseven.workoutdiary.dao;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thirteenseven.workoutdiary.model.Difficulty;

public interface DifficultyRepository extends MBaseRepository<Difficulty, String> {
    Optional<Difficulty> findByName(Difficulty.EDifficulty name);
}
