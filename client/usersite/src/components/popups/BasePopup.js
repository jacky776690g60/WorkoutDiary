import React, { Component } from 'react';
import { createRoot } from 'react-dom/client';  // Import createRoot
import './BasePopup.css';


/**
 * BasePopup - A flexible and reusable popup component for React applications.
 * 
 * This component can be used to create both inline popups and overlay popups.
 * 
 * @prop {boolean} isOverlay   - Determines if the popup should be rendered as an overlay.
 * @prop {Object} [otherProps] - Additional props can be passed and will be available in child classes.
 * 
 * @state {boolean} isVisible - Controls the visibility of the popup.
 * @state {Object}  position  - Stores the position of the popup ({top: number, left: number}).
 * 
 * @example
 * class ExerciseCardPopup extends BasePopup {
 *   constructor(props) {
 *     super(props);
 *     this.state = {
 *       ...this.state, // inherit state from BasePopup
 *     };
 *   }
 *   // ...
 * }
 */
class BasePopup extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isVisible: false,
      position: { top: 0, left: 0 },
    };
    /** a root for the popup container */
    this.root = null;
    this.popupContainer = null; 
    this.baseClassName  = this.props.isOverlay ? "base-popup-overlay" : "base-popup";
  }

  // =====================================================
  // Natives
  // =====================================================
  /**
   * Lifecycle method called after the component is mounted.
   * 
   * @note
   * In React, "mounted" refers to the state of a component when it has been 
   * rendered and inserted into the DOM.
   */
  componentDidMount() {
    document.addEventListener('keydown', this.#handleKeyDown);
    this.componentDidMountExtended();
  }

  /**
   * Lifecycle method called before the component is unmounted.
   * Cleans up event listeners and DOM elements, and calls the extended unmount method.
   */
  componentWillUnmount() {
    document.removeEventListener('keydown', this.#handleKeyDown);
    if (this.root && this.props.isOverlay) {
      this.root.unmount();
      if (this.popupContainer && document.body.contains(this.popupContainer)) {
        document.body.removeChild(this.popupContainer);
      }
      this.popupContainer = null; // Clean up
    }
    this.componentWillUnmountExtended();
  }

  /**
   * Lifecycle method called after the component updates.
   * @param {Object} prevProps - The previous props.
   * @param {Object} prevState - The previous state.
   * 
   * @note
   * Updates means:
   * - state changes (via `setState`)
   * - props change
   * - parent component re-renders
   */
  componentDidUpdate(prevProps, prevState) {
    if (this.props.isOverlay && this.state.isVisible && !prevState.isVisible) {
      this.ensurePopupContainer();
      document.body.appendChild(this.popupContainer);
      this.renderPopupContent();
    } else if (!this.state.isVisible && prevState.isVisible && this.popupContainer) {
      setTimeout(() => {
        this.root.unmount();
        document.body.removeChild(this.popupContainer);
        this.popupContainer = null;
      }, 0); // Defer unmounting to after the current render cycle
    }
  }

  // =======================================================================
  // public Functions
  // =======================================================================
  /** 
   * Create a PopupContainer if not having one already. 
   */
  ensurePopupContainer() {
    if (!this.popupContainer) {
      this.popupContainer = document.createElement('div');
      this.popupContainer.className = this.baseClassName;
      this.popupContainer.addEventListener("click", (e)=> {
        // e.preventDefault();
        if (e.target !== this.popupContainer) return;
        this.hidePopup();
      });
      this.root = createRoot(this.popupContainer);
    }
  }

  showPopup = (position) => {
    this.setState({ isVisible: true, position: position });
  };

  hidePopup = () => {
    this.setState({ isVisible: false });
  };

  // =====================================================
  // private functions
  // =====================================================
  #handleKeyDown = (event) => {
    if (event.keyCode === 27) { // ESC key
      this.hidePopup();
    }
  };

  
  
  // =====================================================
  // Render
  // =====================================================
  renderContent() {
    throw new Error("You must implement the method renderContent() in the child class.");
  }
  
  renderPopupContent() {
    this.root.render(this.renderContent());
  }

  render() {
    if (!this.props.isOverlay) {
      const { isVisible, position } = this.state;
      if (!isVisible) return null;

      return (
        <div 
          className={this.baseClassName} 
          style={{ 
            position: 'fixed', 
            top: `${position.top}px`, left: `${position.left}px` 
          }}
        >
          <div className="flex-col-container">
            {this.renderContent()}
          </div>
        </div>
      );
      
    } else {
      // No need to return anything here for overlays; it's handled in componentDidUpdate
      return null;
    }
  }
}

export default BasePopup;
