import React from 'react';
import BasePopup from '../BasePopup';
import "./ExerciseRecordPopup.css";
import PushBtn from '../../push.btn/PushBtn';
import TimeUtil from '../../../utility/TimeUtil';
import ExerciseRecordController from '../../../controllers/ExerciseRecordController';
import { devLog } from '../../../utility/Logger';

class ExerciseRecordPopup extends BasePopup {
  constructor(props) {
    super(props);
    this.state = {
      ...this.state,
      sets: [],
      note: '',
      customDate: '',          // For storing the date
      customHour: undefined,   // For storing the hour
      customMinute: undefined  // For storing the minute
    };
  }

  handleAddSet = () => {
    this.setState(prevState => ({
      sets: [...prevState.sets, [{ weight: '', repetitions: '' }]]
    }), () => {
      if (this.props.isOverlay) {
        this.renderPopupContent();  // Force re-render of the popup content
      }
    });
  }

  handleRemoveSet = () => {
    this.setState(prevState => ({
      sets: prevState.sets.slice(0, -1)
    }), () => {
      if (this.props.isOverlay) {
        this.renderPopupContent();  // Force re-render of the popup content
      }
    });
  }

  handleAddWeight = (index) => {
    const newWeight = { weight: '', repetitions: '' };
    this.setState(prevState => ({
      sets: prevState.sets.map((set, idx) => idx === index ? [...set, newWeight] : set)
    }), () => {
      if (this.props.isOverlay) {
        this.renderPopupContent();  // Force re-render of the popup content
      }
    });
  }

  handleRemoveWeight = (setIndex) => {
    this.setState(prevState => ({
      sets: prevState.sets.map((set, index) => index === setIndex ? set.slice(0, set.length - 1) : set)
    }), () => {
      if (this.props.isOverlay) {
        this.renderPopupContent();  // Force re-render of the popup content
      }
    });
  }

  handleSetChange = (setIndex, weightIndex, field, value) => {
    this.setState(prevState => ({
      sets: prevState.sets.map((set, sIndex) => {
        if (sIndex === setIndex) {
          return set.map((weight, wIndex) => {
            if (wIndex === weightIndex) {
              return { ...weight, [field]: value };
            }
            return weight;
          });
        }
        return set;
      })
    }));
  }

  handleDateChange = (event) => {
    this.setState({ customDate: event.target.value });
  }
  
  handleHourChange = (event) => {
    this.setState({ customHour: event.target.value });
  }
  
  handleMinuteChange = (event) => {
    this.setState({ customMinute: event.target.value });
  }

  handleNoteChange = (event) => {
    this.setState({ note: event.target.value });
  }

  handleSubmit = async () => {

    if (!this.state.sets.length > 0
       || this.state.sets[0][0].weight === ""
       || this.state.sets[0][0].repetitions === ""
    ) {
      alert("Need at least one record.");
      return;
    }

    const formattedSets = this.state.sets.map(set =>
      set.filter(({ weight, repetitions }) => weight !== "" && repetitions !== "")
        .flatMap(({ weight, repetitions }) => 
          Array(parseInt(repetitions, 10)).fill(parseInt(weight, 10))
        )
    );
    

    let formattedDate = undefined;
    if (this.state.customDate !== ''
      && this.state.customHour !== undefined 
      && this.state.customMinute !== undefined
    )
      formattedDate = `${this.state.customDate.replace(/-/g, '-')}_${this.state.customHour}-${this.state.customMinute}`;
    
    const data = {
      exerciseName: this.props.data.exercise.name,
      sets:         formattedSets,
      datetime:     formattedDate ? formattedDate : TimeUtil.formatDate(new Date()),
      location:     "1,1",
      note:         this.state.note
    };

    ExerciseRecordController.add(data)
      .then(()=> {
        alert("Successful");
        // this.state.sets = [];
        // this.state.note = ""; // only show up when here
        this.setState({ sets: [], note: "" });
        this.renderPopupContent();  // Force re-render of the popup content
      })
      .catch((error) => {
        alert("Failed to add");
        console.error(error);
      })
      .finally(()=> {

      });
  }




  handleKeyDown = (event) => {
    if (event.key === 'Escape') {}
  }



  componentDidMountExtended() {
    super.componentDidMountExtended && super.componentDidMountExtended();
    document.addEventListener('keydown', this.handleKeyDown);
  }

  componentWillUnmountExtended() {
    super.componentWillUnmountExtended && super.componentWillUnmountExtended();
    document.removeEventListener('keydown', this.handleKeyDown);
  }

  renderContent() {
    const { exercise } = this.props.data;

    return (
      <div className='exercise-record-popup'>
        <div className="flex-col-container">
          <h4><u>{exercise.name}</u> Record</h4>
          <div className="flex-row-container">
            <PushBtn
              title="Add"
              iconName="add"
              className="btn-blue"
              onClick={this.handleAddSet}
            />
            <PushBtn
              title="Remove"
              iconName="remove"
              className="btn-red"
              onClick={this.handleRemoveSet}
            />
            <span>Set</span>
          </div>
          <div className="custom-date-container">
            <p>Custom Time (Optional)</p>
            <input type="date" onChange={this.handleDateChange} />
            <input name='hour'   type='number' inputmode="numeric" placeholder='hr' min={0} max={23} step={1} onChange={this.handleHourChange}  />
            <input name='minute' type='number' inputmode="numeric" placeholder='mm' min={0} max={59} step={1} onChange={this.handleMinuteChange}/>
          </div>
          <div className="sets-container">
            {this.state.sets.map((set, setIndex) => (
              <div className="set" key={setIndex}>
                <div className="flex-row-container">
                  <h3>Set {setIndex+1}</h3>
                  <PushBtn 
                    iconName="add"
                    className="btn-mermaid-tail weight-btn"
                    onClick={()=> this.handleAddWeight(setIndex)}
                  />
                  <PushBtn 
                      iconName="remove"
                      className="btn-bittersweet weight-btn"
                      onClick={()=> set.length > 1 && this.handleRemoveWeight(setIndex)}
                    />
                </div>
                {set.map((weight, weightIndex) => (
                  <div className="same-weight-row" key={weightIndex}>
                    <input type="number" inputmode="decimal" placeholder="Single Weight (lbs)" defaultValue={weight.weight || ""} onChange={(e) => this.handleSetChange(setIndex, weightIndex, 'weight', e.target.value)} />
                    x
                    <input type="number" inputmode="decimal" placeholder="Reps"  defaultValue={weight.repetitions || ""}  onChange={(e) => this.handleSetChange(setIndex, weightIndex, 'repetitions', e.target.value)} />
                  </div>
                ))}
              </div>
            ))}
          </div>
          <textarea
            name="note"
            placeholder='Leave a note'
            defaultValue={this.state.note || ""}
            onChange={this.handleNoteChange}
          />
          <PushBtn
            title="Submit"
            iconName="send"
            className="btn-blue"
            onClick={this.handleSubmit}
          />
          <PushBtn
            title="close"
            iconName="close"
            className="btn-red"
            onClick={this.hidePopup}
          />
          <span style={{color: "var(--white_overlay_5)", fontSize: ".85rem"}}>
            All records will be categorized based on 30-minute intervals. If there is already a record in this time slot, the new one will overwrite the old one.
          </span>
        </div>

        
      </div>
    );
  }
}

export default ExerciseRecordPopup;
