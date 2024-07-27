import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { devLog } from '../../utility/Logger';
import UserController from '../../controllers/UserController';

import "./DashboardPage.css";

const DashboardPage = () => {
  /** This is for re-rendering */
  const [userData, setUserData] = useState(null);
  const navigate = useNavigate();

  const getCurrentUser = async () => {
    
    const retrievedUserData = await UserController.getCurrent();
    setUserData(retrievedUserData.data[0]);
    devLog(retrievedUserData.data[0]);
  };

  useEffect(() => {
    getCurrentUser();
  }, []);

  const handleButtonNavigation = (url) => {
    navigate(url);
  }

  return (
    <div className="app-dashboard">
      {userData ? (
          <div className='flex-col-container'>
            <img className='dashboard-profile-pic' src={userData.profilePicURL} alt='ProfilePicture'/>
            <table>
              <tbody>
                <tr><td>Username</td><td>{userData.name}</td></tr>
                <tr><td>Gender</td><td>{userData.gender}</td></tr>
                <tr><td>Birthday</td><td>{new Date(userData.birthday).toDateString()}</td></tr>
                <tr><td>Weight</td><td>160 lbs</td></tr>
              </tbody>
            </table>
            <div className="flex-row-container btns-group">
              <button 
                className='btn-red' 
                onClick={()=> handleButtonNavigation("/exercises")}
              >
                Exercises
              </button>
              <button 
                className='btn-blue'
                onClick={()=> handleButtonNavigation("/schedules")}
              >
                Schedules
              </button>
              <button 
                className='btn-green'
                onClick={()=> handleButtonNavigation("/exercises/post")}
              >
                Add Exercise
              </button>
            </div>
          </div>
        ) : (
          <div className='loading-div flex-col-container'>
            Loading user ...
          </div>
        )
      }
    </div>
  );
};

export default DashboardPage;
