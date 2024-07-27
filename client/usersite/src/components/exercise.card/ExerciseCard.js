import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

import { devLog } from '../../utility/Logger';

import "./ExerciseCard.css";
import ExerciseCardPopup from '../popups/exercise.popups/ExerciseCardPopup';

const ExerciseCard = (data) => {
  const exercisePopupRef = useRef(null);
  
  const handleCardClicked = ()=> {
    if (exercisePopupRef.current) {
      // exercisePopupRef.current.showPopup({ top: 50, left: 50 });
      exercisePopupRef.current.showPopup();
    }
  };

  useEffect(()=> {
    // devLog(data.exercise);
  }, []);

  return (
    <div 
      className="exercise-card" 
      onClick={handleCardClicked}
    >
      <div className="flex-col-container">
        <h1 className="exercise-card-title">{data.exercise.name}</h1>
        <div className="flex-row-container">
          <div className='exercise-difficulty'>
            <p>{data.exercise.difficulty.name.toLowerCase()}</p>
          </div>
          <div className="exercise-musclegrps">
            {data.exercise.muscleGroups
            // .slice() // to create a copy of the array, so the original order in state isn't mutated
            // .sort((a, b) => a.name.localeCompare(b.name)) // sorting alphabetically by name
            .map(gp => (
              <p key={gp.name}>{gp.name.replace("_", " ").toLowerCase()}</p>  // Added a unique 'key' prop here
            ))}
          </div>
        </div>
      </div>
      <ExerciseCardPopup ref={exercisePopupRef} isOverlay={true} exercise={data.exercise}/>
    </div>
  );
};

export default ExerciseCard;
