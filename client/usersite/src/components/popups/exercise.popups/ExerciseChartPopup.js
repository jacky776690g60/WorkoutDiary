import React, { useRef, useEffect, useState } from 'react';
import * as d3 from 'd3';

import ExerciseRecordCtrl from '../../../controllers/ExerciseRecordController';

import "./ExerciseChartPopup.css";
import BasePopup from '../BasePopup';
import PushBtn from '../../push.btn/PushBtn';
import { devLog } from '../../../utility/Logger';

class ExerciseChartPopup extends BasePopup {
  constructor(props) {
    super(props);
    this.state = {
      data:         [],
      offset:       0,
      hasNextPage:  true,
      isFetching:   false,
      selectedData: null
    };
    this.exerciseName = props.exercise.name;
    this.svgRef       = React.createRef();

    // Bind methods
    this.plotData = this.plotData.bind(this);
  }


  // =====================================================
  // Functions
  // =====================================================
  fetchData = async (offset) => {
    try {
      const response = await 
        ExerciseRecordCtrl.search(
          offset,
          { size: 5, exerciseNames: this.exerciseName, strict: true }
        );
      const newData      = response.data;
      const combinedData = [...this.state.data, ...newData];
      const uniqueData   = Array.from( // Assuming 'id' is a unique identifier
        new Map(combinedData.map(item => [item.id, item])).values()
      );
      const dataLimit    = 10; // Optionally, limit the size of data to the most recent N records
      const limitedData  = uniqueData.slice(
        Math.max(uniqueData.length - dataLimit, 0)
      );

      this.setState( // This is async actually
        { data: limitedData, hasNextPage: response.hasNextPage },
        () => {
          devLog("Currently Stored Data:\n", this.state.data);
          this.plotData();
        }
      );
      
    } catch (error) {
      console.error("Error fetching exercise record data:", error)
    }
  }
  
  
  

  handlePageChange(direction) {
    if (this.state.isFetching) return;
    if (!this.state.hasNextPage && direction > 0) return; // Ensure not to proceed if no next page
    const oldOffset = this.state.offset;
    const newOffset = oldOffset + direction < 0 ? 0 : oldOffset + direction;

    this.setState(
      { offset: newOffset },
      async () => {
        this.setState({isFetching: true});
        await this.fetchData(newOffset);
        this.setState({isFetching: false});
    });
  }




  // =====================================================
  // Utility 
  // =====================================================
  componentDidMountExtended() {
    super.componentDidMountExtended && super.componentDidMountExtended();
    document.addEventListener('keydown', this.handleKeyDown);
    this.fetchData(0);
  }

  componentWillUnmountExtended() {
    super.componentWillUnmountExtended && super.componentWillUnmountExtended();
    document.removeEventListener('keydown', this.handleKeyDown);
  }

   


  // =====================================================
  // Plotting Chart
  // =====================================================
  plotData = () => {
    
    d3.select(this.svgRef.current).selectAll('*').remove(); /** Remove all previous first */
    
    /** Size of each data window */
    const pageSize = 5;
    /** left index of window */
    const lIdx     = Math.max(this.state.offset, 0);
    /** Margins for the plot itself */
    const margin = { top: 20, right: 20, bottom: 50, left: 50 };
    /** width of the plot */
    const width  = 350 - margin.left - margin.right;
    /** height of the plot */
    const height = 320 - margin.top - margin.bottom;
    /** The format for X labels */
    const formatDate     = d3.timeFormat("%Y-%m-%d");
    const formatDateLong = d3.timeFormat("%Y-%m-%d %H:%M");

    
    /** Append the svg object to the body of the page */
    const svg = d3.select("#chart")
        .attr("width",  width  + margin.left + margin.right)
        .attr("height", height + margin.top  + margin.bottom)
        .append("g")
        .attr("transform", `translate(${margin.left}, ${margin.top})`);


    if (!this.state.isVisible) return; // No rendering if not visible

    /** 
     * The extracted subset of data for the current window with computed properties.
     * @note
     * The original data is fetched in reverse order.
     */
    const rawData = (() => {
      const totalWeightsArray = [];
    
      return this.state.data
        .slice(lIdx, lIdx + pageSize)
        .reverse()  // Reverse first, then process
        .map((d, index, array) => {
          const totalWeights = d.sets.reduce((sum, set) => sum + d3.sum(set.repetitions), 0);
          totalWeightsArray.push(totalWeights);

          const singleMaxWeight      = Math.max(...d.sets.map(set => Math.max(...set.repetitions)));
          const singleMaxRepetitions = Math.max(...d.sets.map(set => set.repetitions.length));
    
          let totalWeightChange = 0, dayPassed = 0;
          if (index > 0 && totalWeightsArray[index - 1] !== undefined && totalWeightsArray[index - 1] !== 0) {
            totalWeightChange = ((totalWeights - totalWeightsArray[index - 1]) / totalWeightsArray[index - 1]) * 100;
            
            const currentDate = new Date(d.date);
            const previousDate = new Date(array[index - 1].date);
            const timeDifference = currentDate.getTime() - previousDate.getTime();
            dayPassed = timeDifference / (1000 * 3600 * 24).toPrecision(1);
          }


          // Find the set with the highest single weight
          const strongestSet = d.sets.reduce((maxSet, set) => {
              const maxWeightInSet = Math.max(...set.repetitions);
              const countMaxWeightInSet = set.repetitions.filter(weight => weight === maxWeightInSet).length;

              if (maxWeightInSet > maxSet.maxWeight) {
                return { maxWeight: maxWeightInSet, repetitions: countMaxWeightInSet };
              }
              return maxSet;
            }, { maxWeight: 0, repetitions: 0 });
    
          return {
            date: new Date(d.date),
            strongestSet: strongestSet,
            totalWeights: totalWeights,
            singleMaxWeight: singleMaxWeight,
            singleMaxRepetitions: singleMaxRepetitions,
            totalWeightChange: totalWeightChange.toFixed(2) + '%',
            dayPassed: dayPassed,
            note: d.note
          };
        });
    })();
    
    
    // ----------
    // Add X axis
    // =================
    /** Set up the x scale */
    const x = d3.scalePoint()
      .domain(rawData.map(d => d.date)) // Use date strings as the domain
      .range([0, width])
      .padding(0.5);
    /** Append the x-axis to your SVG and format the ticks */
    svg.append("g")
      .attr("transform", `translate(0, ${height})`)
      .call(d3.axisBottom(x).tickFormat(d => formatDate(new Date(d))))
      .selectAll("text")              // select all the text elements for the x-axis
      .style("text-anchor", "end")    // this makes it easier to read labels
      .attr("transform", (d, i) => {
        const height = i % 2 === 0 ? 10 : 25;
        return `translate(25, ${height})`;
      });

    // ----------
    // Add Y axis
    // =================
    const minY    = d3.min(rawData, d => d.totalWeights);
    const maxY    = d3.max(rawData, d => d.totalWeights);
    const marginY = 0.2 * (maxY - minY); // 20% margin
    
    const y = d3.scaleLinear()
              .domain([minY - marginY, maxY + marginY])
              .range([height, 0]);
    svg.append("g")
      .call(d3.axisLeft(y).ticks(pageSize));
    
    // ----------
    // Add Line
    // =================
    const line = d3.line()
                  .x(d => x(d.date))
                  .y(d => y(d.totalWeights));
    svg.append("path")
      .datum(rawData)
      .attr("fill", "none")
      .attr("stroke", "steelblue")
      .attr("stroke-width", 1.5)
      .attr("d", line);

    // ------------
    // Add Circles
    // ===================
    svg.selectAll(".point")
      .data(rawData)
      .enter()
      .append("circle")
      .attr("class", "chart-data-circle")
      .attr("cx", d => x(d.date))      // pos X
      .attr("cy", d => y(d.totalWeights)) // pos Y
      .attr("r", 12)                   // Radius
      .on("mouseover", function(event, d) {
        /** @note for future reference */
      })
      .on("mouseout", function(event, d) {
        /** @note for future reference */
      })
      .on("click", (event, d) => {
        this.setState({
            selectedData: {
                date:                 formatDateLong(d.date),
                strongestSet:         d.strongestSet,
                totalWeights:         d.totalWeights,
                singleMaxWeight:      d.singleMaxWeight,
                singleMaxRepetitions: d.singleMaxRepetitions,
                totalWeightChange:    d.totalWeightChange,
                dayPassed:            d.dayPassed,
                note:                 d.note
            }
        }, ()=> {
          devLog("Clicked Data", this.state.selectedData);
          this.root.render(this.renderContent());
        });
    });
    
    // Adjust the circle attributes if needed for better visual distinction
    svg.selectAll("circle.point")
    .attr("stroke", "black")
    .attr("stroke-width", 10);

  }







  // =====================================================
  // Overriden
  // =====================================================
  renderContent() {
    return (
      <div className='exercise-chart-popup'>
        <div className="flex-col-container">
          <svg ref={this.svgRef} id="chart"/>
          <div className="data-pt-info">
            {this.state.selectedData && (
              <table>
                <tbody>
                  <tr>
                    <td>Date</td>
                    <td>{this.state.selectedData.date}</td>
                  </tr>
                  <tr>
                    <td>Best Set</td>
                    <td>{`${this.state.selectedData.strongestSet.maxWeight} x ${this.state.selectedData.strongestSet.repetitions}`}</td>
                  </tr>
                  <tr>
                    <td>Total (LBs)</td>
                    <td>{this.state.selectedData.totalWeights}</td>
                  </tr>
                  <tr>
                    <td>Total Change %</td>
                    <td>{this.state.selectedData.totalWeightChange}</td>
                  </tr>
                  <tr>
                    <td>Max Weight</td>
                    <td>{this.state.selectedData.singleMaxWeight}</td>
                  </tr>
                  <tr>
                    <td>Max Reps</td>
                    <td>{this.state.selectedData.singleMaxRepetitions}</td>
                  </tr>
                  <tr>
                    <td>Day Passed</td>
                    <td>{this.state.selectedData.dayPassed}</td>
                  </tr>
                  <tr>
                    <td>Note</td>
                    <td>{this.state.selectedData.note}</td>
                  </tr>
                </tbody>
              </table>
            )}
          </div>
          <div className="flex-col-container">
            <div className="flex-row-container">
              <PushBtn 
                title="Earlier"
                iconName="chevron_left"
                className="btn-green" 
                onClick={() => this.handlePageChange(1)} 
              />
              <PushBtn 
                title="Latest"
                iconName="chevron_right"
                className="btn-blue" 
                onClick={() => this.handlePageChange(-1)} 
              />
            </div>
            <PushBtn 
              title="Close"
              iconName="close" 
              className="btn-red"
              onClick={()=> this.hidePopup()} 
            />
          </div>
        </div>
      </div>
    );
  }
}

export default ExerciseChartPopup;
