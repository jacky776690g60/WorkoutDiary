/** =================================================================
| Logger.js  --  usersite/src/utility/Logger.js
|
| @author Jack
| Created on 06/04, 2024
| Copyright © 2024 jacktogon. All rights reserved.
================================================================= */

/**
 * Only log when the REACT_APP_MODE is set to one of these: "development", 
 * "debug", "dev"
 * 
 * @param  {...any} values any
 */
export function devLog(...values) {
  /** 
   * Might have to npm run start again to make it load process.env 
   * 
   * @implnote
   * In React, it must be prefixed with REACT_APP_
   */
  if (process.env.REACT_APP_MODE === "development" 
      || process.env.REACT_APP_MODE === "debug"
      || process.env.REACT_APP_MODE === "dev"
  ) {
    console.log('%c(DevLog)\n', 'color: #fae43b; background-color: black font-weight: bold; font-size: 12px;', ...values);
  }
}