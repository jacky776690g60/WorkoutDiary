import React from 'react';
import BasePopup from './BasePopup'; // Adjust the import path as necessary
import UserController from '../../controllers/UserController';
import "./LoginPopup.css";
import PushBtn from '../push.btn/PushBtn';

class LoginPopup extends BasePopup {

  // =====================================================
  // Default
  // =====================================================
  constructor(props) {
    super(props);
    this.state = {
      ...this.state, // inherit state from BasePopup
      email:    '',
      password: '',
    };
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
   * This function will update the state based on the attribute "name" accrodingly.
   * @param {KeyboardEvent} event 
   */
  handleInputChange = (event) => {
    const { name, value } = event.target;
    this.setState({ [name]: value });
  };


  handleLogin = async () => {
    const { email, password } = this.state;
    if (email === "" || password === "")
      throw new Error("The login email and password cannot be empty");
    await UserController.login(email, password);
  };

  /**
   * Handler for key down
   * @param {KeyboardEvent} event 
   */
  handleKeyDown = (event) => {
    if (event.key === "Enter" ) {
      this.handleLogin();
    }
  }

  componentDidMountExtended() {
    document.addEventListener('keydown', this.handleKeyDown);
  }
  
  componentWillUnmountExtended() {
    document.removeEventListener('keydown', this.handleKeyDown);
  }

  // =====================================================
  // Rendering
  // =====================================================
  renderContent() {
    return (
      <div className='login-popup'>
        <form className="flex-col-container">
          <h2>Login to your account</h2>
          <input
            type="email"
            name="email"
            placeholder="Email"
            onChange={this.handleInputChange}
          />
          <input
            type="password"
            name="password"
            placeholder="Password"
            onChange={this.handleInputChange}
          />
          <div className="flex-row-container">
            <PushBtn 
              title="Login"
              iconName="send"
              className="btn-blue"
              onClick={this.handleLogin}
            />
            <PushBtn
              title="Close"
              iconName="close"
              className="btn-red"
              onClick={this.hidePopup}
            />
          </div>
        </form>
      </div>
    );
  }
}

export default LoginPopup;
