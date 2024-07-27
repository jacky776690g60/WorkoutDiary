import React, { useEffect, useState, useCallback, useRef, useMemo } from 'react';
import { debounce } from 'lodash';

import { devLog } from '../../utility/Logger';
import { getMuscleGrpOptions } from '../../utility/ElementUtil';
import ExerciseCard from '../../components/exercise.card/ExerciseCard';
import ExercisesController from '../../controllers/ExercisesController';
import MuscleGroupController from '../../controllers/MuscleGroupController';

import "./ExercisesPage.css";

const PAGE_SIZE = 20;

// ============================================================================
// Custom Hooks
// ============================================================================
const useMuscleGroups = () => {
  const [muscleGroups, setMuscleGroups] = useState([]);

  useEffect(() => {
    MuscleGroupController.getAll()
      .then((res)  => setMuscleGroups([...res.data]))
      .catch((err) => console.error("Error fetching muscle groups:", err));
  }, []);

  return muscleGroups;
};



const useExercises = () => {
  const [state, setState] = useState({
    exercises:    [],
    currentPage:  0,
    hasNextPage:  true,
    loading:      false,
    error:        null,
  });

  const latestRequest = useRef(0);

  const getExercises = useCallback(async (page, options) => {
    /** 
     * @note added or overwritten if they already exists. */
    setState(prev => ({ ...prev, loading: true, error: null })); 
    const requestId = ++latestRequest.current;

    try {
      const result = await ExercisesController.search(
        page, 
        ExercisesController.buildOptions(options)
      );

      if (requestId === latestRequest.current) {
        setState(prev => ({
          ...prev,
          hasNextPage: result.hasNextPage,
          exercises: page > 0 ? [...prev.exercises, ...result.data] : result.data,
          loading: false,
        }));
      }
    } catch (error) {
      devLog('Failed to fetch exercises:', error);
      setState(prev => ({ ...prev, loading: false, error: 'Failed to fetch exercises' }));
    }
  }, []);

  return [state, getExercises, setState];
};



// ============================================================================
// Functional Components
// ============================================================================
const ExercisesPage = () => {
  const [{ exercises, currentPage, hasNextPage, loading, error }, getExercises, setExercisesState] = useExercises();
  
  const muscleGroups                                  = useMuscleGroups();
  const [selectedMuscleGroup, setSelectedMuscleGroup] = useState(undefined);
  const muscleGroupSelect                             = useRef(null);


  const [searchTerm, setSearchTerm] = useState('');
  const debouncedSearch = useMemo(() => debounce(
    (term) => {
      setExercisesState(prev => ({ ...prev, exercises: [] }));
      getExercises(0, { size: PAGE_SIZE, substring: term, muscleGroups: selectedMuscleGroup });
    }, 300), 
    [getExercises, selectedMuscleGroup]
  );


  const exsContainerRef   = useRef(null);
  const extendLoaderRef   = useRef(null);


  useEffect(() => {
    debouncedSearch(searchTerm);
  }, [searchTerm, selectedMuscleGroup, debouncedSearch]);

  const handleObserver = useCallback((entries) => {
    const target = entries[0];
    if (target.isIntersecting && !loading && hasNextPage) {
      if (exsContainerRef.current && exsContainerRef.current.children.length > PAGE_SIZE && exsContainerRef.current.scrollHeight > exsContainerRef.current.clientHeight - 30) {
        devLog("Exercise page reached bottom to load more...");
        setExercisesState(prev => ({ ...prev, currentPage: prev.currentPage + 1 }));
        getExercises(currentPage + 1, { size: PAGE_SIZE, substring: searchTerm, muscleGroups: selectedMuscleGroup });
      }
    }
  }, [loading, currentPage, searchTerm, selectedMuscleGroup, hasNextPage, getExercises]);

  useEffect(() => {
    const observer = new IntersectionObserver(handleObserver, {
      root: null,
      rootMargin: "20px",
      threshold: 0
    });
    if (extendLoaderRef.current) {
      observer.observe(extendLoaderRef.current);
    }
    return () => observer.disconnect();
  }, [handleObserver]);

  // Add these two handlers back
  const handleSearchChange = (event) => {
    setSearchTerm(event.target.value);
  };

  const handleMuscleGroupChange = (event) => {
    const selectedVal = event.target.value;
    setSelectedMuscleGroup(selectedVal === "ALL" ? undefined : selectedVal);
  };



  // =====================================================
  // Returning Component
  // =====================================================
  return (
    <div className="app-exercises">
      <div className="flex-row-container search-bar">
        <input
          type="text"
          placeholder="E.g., Bench Press"
          value={searchTerm}
          onChange={handleSearchChange}
          className="search-input"
          aria-label="Search exercises"
        />
        <select 
          ref={muscleGroupSelect} 
          onChange={handleMuscleGroupChange}
          aria-label="Select muscle group"
        >
          <option value="ALL">ALL</option>
          {
            getMuscleGrpOptions(muscleGroups)
          }
        </select>
      </div>
      <div className="exercises-container" ref={exsContainerRef}>
        {loading && exercises.length === 0 ? (
          <p>Loading ...</p>
        ) : (
          <>
            {error && <p className="error-message">{error}</p>}
            {exercises.length > 0 ?
              exercises.map(exercise => (
                <ExerciseCard key={exercise.id} exercise={exercise} />
              ))
              : <p>No exercises found.</p>
            }
            {loading && exercises.length > 0 && <p>Loading more exercises...</p>}
          </>
        )}
        <div ref={extendLoaderRef} />
      </div>
    </div>
  );
};

export default ExercisesPage;