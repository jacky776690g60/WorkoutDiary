import React, {useRef} from 'react';
import BasePopup from '../BasePopup'; // Adjust the import path as necessary

import "./ExerciseCardPopup.css";
import PushBtn from '../../push.btn/PushBtn';
import ExerciseRecordPopup from './ExerciseRecordPopup';
import ExerciseChartPopup from './ExerciseChartPopup';

class ExerciseCardPopup extends BasePopup {

  // =====================================================
  // Default
  // =====================================================
  constructor(props) {
    super(props);
    this.state = {
      ...this.state, // inherit state from BasePopup
    };
    this.recordPopupRef = React.createRef();
    this.chartPopupRef  = React.createRef();

  }

  // =====================================================
  // Functions
     /**
     * @note arrow functions will automatically bind the function to class, so
     * you don't have to call bind in the constructor.
     * @example
     * // To bind in constructor
     * this.handleKeyDown = this.handleKeyDown.bind(this);
     */
  // =====================================================
  /**
   * Handler for key down
   * @param {KeyboardEvent} event 
   */
  handleKeyDown = (event) => {
    if (event.key === "Enter" ) {}
  }

  componentDidMountExtended() {
    document.addEventListener('keydown', this.handleKeyDown);
  }
  
  componentWillUnmountExtended() {
    document.removeEventListener('keydown', this.handleKeyDown);
  }

  handleShowRecordPopup = () => {
    if (this.recordPopupRef.current) {
      this.recordPopupRef.current.showPopup();
    }
  }

  handleShowChartPopup = () => {
    if (this.chartPopupRef.current) {
      this.chartPopupRef.current.showPopup();
    }
  }




  #convertToEmbedUrl(youtubeUrl) {
    try {
      const url = new URL(youtubeUrl);
      const videoId = url.searchParams.get("v");
      const startTime = url.searchParams.get("t");
  
      // Convert time in various formats to seconds
      const timeInSeconds = startTime ? this.#parseTime(startTime) : 0;
      const embedUrl = `https://www.youtube.com/embed/${videoId}?start=${timeInSeconds}`;
  
      return embedUrl;
    } catch (error) {
      console.error("Invalid URL:", error);
      return null;
    }
  }
  
  #parseTime(time) {
    // Check if time is in seconds (e.g., '10s') or in other formats
    if (time.endsWith('s')) {
      return parseInt(time.slice(0, -1), 10); // Remove the last character 's' and parse to integer
    } else {
      // Additional parsing logic here if needed
      return parseInt(time, 10);
    }
  }


  // =====================================================
  // Rendering
  // =====================================================
  renderContent() {
    const { exercise } = this.props;
    // console.log(exercise);
    return (
      <div className='exercise-card-popup'>
        <div className="flex-col-container">
          {exercise ? 
            <>
              <h2>{exercise ? exercise.name : 'Unknown Exercise Name'}</h2>
              <iframe
                src={this.#convertToEmbedUrl(exercise.videoURL)}
                title='Tutorial'
                allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen="1"
                frameBorder={0}
              ></iframe>
              <div className="exercise-popup-detailed-info">
                <h3>Description</h3>
                <p>{exercise.description}</p>
              </div>
              <div className='exercise-popup-detailed-info'>
                <h3>Difficulty</h3>
                <p>{exercise.difficulty.name.toLowerCase()}</p>
              </div>
              <div className="exercise-popup-detailed-info">
                <h3>Worked Muscle Groups</h3>
                {exercise.muscleGroups
                .sort((a, b) => a.name.localeCompare(b.name)) // sorting alphabetically by name
                .map(gp => (
                  <p key={gp.name}>{gp.name.replace("_", " ").toLowerCase()}</p>  // Added a unique 'key' prop here
                ))}
              </div>
            </>
            :
            <p>Loading Exercise...</p>
          }
          <div className="flex-row-container">
            <PushBtn 
              title="New Record" 
              iconName="radio_button_checked"
              className="push-btn btn-blue"
              onClick={this.handleShowRecordPopup}
            />
            <PushBtn 
              title="Chart" 
              iconName="insights"
              className="push-btn btn-yellow"
              onClick={this.handleShowChartPopup}
            />
          </div>
          <div className="flex-row-container">
            <PushBtn 
              title="Close" 
              iconName="close"
              className="close-btn push-btn btn-red"
              onClick={this.hidePopup}
            />
          </div>
        </div>
        <ExerciseRecordPopup ref={this.recordPopupRef} isOverlay={true} data={this.props} />
        <ExerciseChartPopup  ref={this.chartPopupRef}  isOverlay={true} exercise={this.props.exercise} />
      </div>
    );
  }
}

export default ExerciseCardPopup;
