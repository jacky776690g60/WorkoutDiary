import React, { useRef } from 'react';
import LoginPopup from '../../components/popups/LoginPopup';
import "./IntroPage.css";

const IntroPage = () => {
  const loginPopupRef = useRef(null);

  const showLoginPopup = () => {
    if (loginPopupRef.current) {
      // loginPopupRef.current.showPopup({ top: 50, left: 50 });
      loginPopupRef.current.showPopup();
    }
  };

  return (
    <div className="app-introduction">
      <div className="flex-col-container">
        <div className="flex-col-container-item">
          <p>Tired of using paper and pencil to write down your workout progress?</p>
          <p>Workout Diary offers a structure way to record your progess.</p>
        </div>
        <div className="flex-col-container-item">
          <p>It is powered by React, D3.js, Spring Boot, and MongoDB. The sources of tutorials are all over from the Internet, mostly Youtube.</p>
          <p style={{color: "var(--google_yellow)", fontWeight: "800",}}>Enjoy using this web application.</p>
          <p style={{color: "var(--google_yellow)", fontWeight: "800",}}>Happy Working Out!</p>
          <p>If you have any recommendations, please let me know so I can improve the website :)</p>
        </div>
        <div className="flex-col-container-item">
          <div className="flex-row-container">
            <button className='btn-yellow' onClick={showLoginPopup}>Log In</button>
            {/* <button className='btn-blue' onClick={showLoginPopup}>Sign Up</button> */}
          </div>
        </div>
        <LoginPopup ref={loginPopupRef} isOverlay={true} />
      </div>
    </div>
  );
};

export default IntroPage;
