import React, { useEffect, useState } from 'react';

import "./PushBtn.css";

const PushBtn = (props) => {
  
  useEffect(()=> {

  }, []);

  return (
    <button 
      className={`${props.className} push-button`}
      onClick={(e)=> {
        e.preventDefault();
        props.onClick && props.onClick()
      }}
    >
      <span>{props.title}</span>
      <i className="material-icons">{props.iconName}</i>
    </button>
  );
};

export default PushBtn;
