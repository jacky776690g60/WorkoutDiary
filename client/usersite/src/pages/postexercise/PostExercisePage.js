import React, { useEffect, useState, useCallback, useRef } from 'react';
import { debounce } from 'lodash';

import { devLog } from '../../utility/Logger';
import { getMuscleGrpOptions } from '../../utility/ElementUtil.js';
import ExercisesController from '../../controllers/ExercisesController';
import MuscleGroupController from '../../controllers/MuscleGroupController';
import PushBtn from '../../components/push.btn/PushBtn.js';

import "./PostExercisePage.css";

const PostExercisePage = () => {
  const [muscleGrps, setMuscleGrps]       = useState([]);
  const [selectedMuscleGrps, setSelectedMuscleGrps]       = useState([]);
  const [selectedMainMuscleGrps, setSelectedMainMuscleGrps]       = useState([]);
  
  const [description, setDescription]   = useState("");
  const [exerciseName, setExerciseName] = useState("");
  const [videoURL, setVideoURL]         = useState("");
  const [difficulty, setDifficulty]     = useState("");

  
  useEffect(()=> {
    MuscleGroupController.getAll()
      .then((res) => {
        setMuscleGrps(res.data);
      });
  }, []);

  const handleMuscleGroupsSelection = (e) => {
    const selectedOptions = Array.from(e.target.selectedOptions, option => option.value);
    setSelectedMuscleGrps(selectedOptions);
    console.log(selectedMuscleGrps);
  };

  const handleMainMuscleGroupsSelection = (e) => {
    const selectedOptions = Array.from(e.target.selectedOptions, option => option.value);
    setSelectedMainMuscleGrps(selectedOptions);
    console.log(selectedMainMuscleGrps);
  };

  const submitNewExercise = async () => {
    const res = await ExercisesController.add(
      exerciseName,
      videoURL,
      description,
      selectedMuscleGrps,
      selectedMainMuscleGrps,
      difficulty
    );
    devLog("Added Exercise response:", res);
    alert("Success");
    setExerciseName("");
    setVideoURL("");
  };



  return (
    <div className="post-exericse-page">
      <h1>Add New Exercise</h1>
      <table>
        <tbody>
          <tr>
            <td>Exercise Name:</td>
            <td>
              <input 
                type="text" 
                placeholder='Bench Press'
                value={exerciseName}
                onChange={e => setExerciseName(e.target.value)}
              />
            </td>
          </tr>
          <tr>
            <td>Video URL:</td>
            <td>
              <input 
                type="text" 
                value={videoURL}
                placeholder='If YouTube, no need for embedded version.'
                onChange={e => setVideoURL(e.target.value)}
              />
            </td>
          </tr>
          <tr>
            <td>Muscle Groups:</td>
            <td>
            <select 
              multiple 
              onChange={handleMuscleGroupsSelection}
            >
                { getMuscleGrpOptions(muscleGrps) }
              </select>
            </td>
          </tr>
          <tr>
            <td>Main Muscle Groups:</td>
            <td>
              <select multiple onChange={handleMainMuscleGroupsSelection}>
              {selectedMuscleGrps.map((grp, i) => (
                <option key={i} value={grp}>
                  {grp}
                </option>
              ))}
              </select>
            </td>
          </tr>
          <tr>
            <td>
              Difficulty:
            </td>
            <td>
              <select 
                defaultValue={"Easy"}
                onChange={e => setDifficulty(e.target.value)}
              >
                <option value="Easy">Easy</option>
                <option value="Intermediate">Intermediate</option>
                <option value="Advanced">Advanced</option>
                <option value="Expert">Expert</option>
              </select>
            </td>
          </tr>
          <tr>
            <td>Description:</td>
            <td>
              <input 
                type="text" 
                placeholder='...' 
                onChange={(e) => setDescription(e.target.value)}
              />
            </td>
          </tr>
        </tbody>
      </table>
      <PushBtn 
        title="Add"
        iconName="send"
        className="btn-blue"
        onClick={submitNewExercise}
      /> 
    </div>
  );
};

export default PostExercisePage;
