package com.thirteenseven.workoutdiary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * annotation order doesn't matter in most cases; but in worst 
 * case, regarding compile-time effects, the order may change 
 * the compilation process
 */
@SpringBootApplication
@EnableMongoRepositories
public class WorkoutdiaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkoutdiaryApplication.class, args);
	}

}
