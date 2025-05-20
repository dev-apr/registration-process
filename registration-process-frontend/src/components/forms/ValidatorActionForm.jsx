import React, { useState, useEffect } from 'react';
import { taskApi } from '../../services/api';
import './FormStyles.css';

/**
 * Validator Action Form - Allows validator to approve, reject, request clarification, or raise inspection
 */
export const ValidatorActionForm = ({ task, variables, onComplete }) => {
  // Initialize form data with violationIndicator
  const [formData, setFormData] = useState({
    validateRequest: variables?.validateRequest || 'approved',
    comments: variables?.comments || '',
violationIndicator: variables?.violationIndicator || false
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  // Initialize violation state based on the initial value of violationIndicator
  const [violationIndicator, setViolationIndicator] = useState(variables?.violationIndicator || false);

  // Initialize form data from variables
  useEffect(() => {
    if (variables && Object.keys(variables).length > 0) {
      // Get the current validation request value
      const currentValidateRequest = variables.validateRequest || 'approved';
      
      // Check if "Raise Inspection" is selected
      const isRaiseInspection = currentValidateRequest === 'raiseInspection';
      
      // Update form data
      setFormData(prevData => ({
        ...prevData,
        validateRequest: currentValidateRequest,
        comments: variables.comments || '',
        // Set violationIndicator based on whether "Raise Inspection" is selected
        violationIndicator: isRaiseInspection || variables.violationIndicator || false
      }));
      
      // Update violation state for UI
      setViolationIndicator(isRaiseInspection || variables.violationIndicator || false);
      
      console.log('Updated form data from variables:', {
        validateRequest: currentValidateRequest,
        violationIndicator: isRaiseInspection || variables.violationIndicator || false
      });
    }
  }, [variables]);

  // Format date for display if available
  const formatDate = (dateString) => {
    if (!dateString) return '';
    
    try {
      const date = new Date(dateString);
      return date.toISOString().split('T')[0];
    } catch (e) {
      console.error('Error formatting date:', e);
      return '';
    }
  };

  // Format wage for display
  const formatWage = (wage) => {
    if (wage === undefined || wage === null || wage === '') return '';
    
    if (!isNaN(parseFloat(wage))) {
      return parseFloat(wage).toString();
    }
    
    return wage;
  };

  /**
   * Handle form field changes
   */
  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    
    // Handle different input types
    const fieldValue = type === 'checkbox' ? checked : value;
    
    setFormData(prevData => ({
      ...prevData,
      [name]: fieldValue
    }));
  };

  /**
   * Handle radio button changes
   */
  const handleRadioChange = (name, value) => {
    // Check if "Raise Inspection" is selected
    const isRaiseInspection = value === 'raiseInspection';
    
    // Update form data with the selected value
    setFormData(prevData => ({
      ...prevData,
      [name]: value,
      // Set violationIndicator to true if "Raise Inspection" is selected
      violationIndicator: isRaiseInspection
    }));

    // For debugging
    console.log(`Radio changed: ${name} = ${value}, violationIndicator = ${isRaiseInspection}`);
    
    // Update the violation state (for UI purposes if needed)
    setViolationIndicator(isRaiseInspection);
  };

  /**
   * Handle form submission
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      setSubmitting(true);
      setError(null);
      
      // Log the form data being sent to the backend
      console.log('Submitting task with data:', formData);
      
      // Send the form data to the backend
      await taskApi.completeTask(task.id, formData);
      
      setSuccess('Task completed successfully');
      
      // Call onComplete callback after a short delay
      setTimeout(() => {
        if (onComplete) {
          onComplete();
        }
      }, 1500);
    } catch (err) {
      console.error('Error completing task:', err);
      setError(err.message || 'Failed to complete task');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="task-form" onSubmit={handleSubmit}>
      <h2>Validate Request</h2>
      
      {/* Task Information Section */}
      {task && (
        <div className="form-section">
          <h3>Task Information</h3>
          <div className="task-info">
            {task.id && <p><strong>Task ID:</strong> {task.id}</p>}
            {task.name && <p><strong>Task Name:</strong> {task.name}</p>}
            {task.taskDefinitionId && (
              <p><strong>Task Type:</strong> {task.taskDefinitionId}</p>
            )}
            {task.candidateGroups && task.candidateGroups.length > 0 && (
              <p><strong>Candidate Groups:</strong> {task.candidateGroups.join(', ')}</p>
            )}
          </div>
        </div>
      )}
      
      {/* Request Information Section */}
      <div className="form-section">
        <h3>Request Information</h3>
        
        <div className="form-group">
          <label htmlFor="wage">Wage ($)</label>
          <input
            type="number"
            id="wage"
            name="wage"
            value={formatWage(variables.wage)}
            readOnly
            className="form-control"
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="requestDate">Date</label>
          <input
            type="date"
            id="requestDate"
            name="requestDate"
            value={formatDate(variables.requestDate)}
            readOnly
            className="form-control"
          />
        </div>
      </div>
      
      {/* Validation Section */}
      <div className="form-section">
        <h3>Validation</h3>
        
        <div className="form-group">
          <div className="radio-group">
            <div className="form-check">
              <input
                type="radio"
                id="validateRequest-approved"
                name="validateRequest"
                value="approved"
                checked={formData.validateRequest === 'approved'}
                onChange={() => handleRadioChange('validateRequest', 'approved')}
              />
              <label htmlFor="validateRequest-approved">
                Approve
              </label>
            </div>
            
            <div className="form-check">
              <input
                type="radio"
                id="validateRequest-rejected"
                name="validateRequest"
                value="rejected"
                checked={formData.validateRequest === 'rejected'}
                onChange={() => handleRadioChange('validateRequest', 'rejected')}
              />
              <label htmlFor="validateRequest-rejected">
                Reject
              </label>
            </div>
            
            <div className="form-check">
              <input
                type="radio"
                id="validateRequest-requestClarification"
                name="validateRequest"
                value="requestClarification"
                checked={formData.validateRequest === 'requestClarification'}
                onChange={() => handleRadioChange('validateRequest', 'requestClarification')}
              />
              <label htmlFor="validateRequest-requestClarification">
                Request Clarification
              </label>
            </div>
            
            <div className="form-check">
              <input
                type="radio"
                id="validateRequest-raiseInspection"
                name="validateRequest"
                value="raiseInspection"
                checked={formData.validateRequest === 'raiseInspection'}
                onChange={() => handleRadioChange('validateRequest', 'raiseInspection')}
              />
              <label htmlFor="validateRequest-raiseInspection" style={{ color: formData.validateRequest === 'raiseInspection' ? '#d9534f' : 'inherit' }}>
                Raise Inspection {formData.validateRequest === 'raiseInspection' && '(Violation Indicator: ON)'}
              </label>
            </div>
          </div>
        </div>
        
        <div className="form-group">
          <label htmlFor="comments">Comments</label>
          <textarea
            id="comments"
            name="comments"
            value={formData.comments || ''}
            onChange={handleChange}
            placeholder="Enter your comments here..."
            rows={5}
            className="form-control"
          />
        </div>
      </div>
      
      {/* Success message */}
      {success && (
        <div className="success-message">
          {success}
        </div>
      )}
      
      {/* Error message */}
      {error && (
        <div className="error-message">
          Error: {error}
        </div>
      )}
      
      {/* Submit button */}
      <div className="form-buttons">
        <button 
          type="submit" 
          className="btn btn-primary" 
          disabled={submitting}
        >
          {submitting ? 'Submitting...' : 'Complete Task'}
        </button>
      </div>
    </form>
  );
};