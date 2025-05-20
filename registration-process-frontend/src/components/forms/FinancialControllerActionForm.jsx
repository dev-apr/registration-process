import React, { useState, useEffect } from 'react';
import { taskApi } from '../../services/api';
import './FormStyles.css';

/**
 * Financial Controller Action Form - Allows financial controller to approve or return
 */
export const FinancialControllerActionForm = ({ task, variables, onComplete }) => {
  const [formData, setFormData] = useState({
    actionPerformed: variables?.actionPerformed || 'approved',
    financialComments: variables?.financialComments || ''
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Initialize form data from variables
  useEffect(() => {
    if (variables && Object.keys(variables).length > 0) {
      setFormData(prevData => ({
        ...prevData,
        actionPerformed: variables.actionPerformed || 'approved',
        financialComments: variables.financialComments || ''
      }));
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
    setFormData(prevData => ({
      ...prevData,
      [name]: value
    }));
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
      <h2>Financial Controller Action</h2>
      
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
        
        {variables.comments && (
          <div className="form-group">
            <label htmlFor="comments">Comments</label>
            <textarea
              id="comments"
              name="comments"
              value={variables.comments}
              readOnly
              rows={4}
              className="form-control"
            />
          </div>
        )}
      </div>
      
      {/* Financial Assessment Section */}
      <div className="form-section">
        <h3>Financial Assessment</h3>
        
        <div className="form-group">
          <div className="radio-group">
            <div className="form-check">
              <input
                type="radio"
                id="actionPerformed-approved"
                name="actionPerformed"
                value="approved"
                checked={formData.actionPerformed === 'approved'}
                onChange={() => handleRadioChange('actionPerformed', 'approved')}
              />
              <label htmlFor="actionPerformed-approved">
                Approve
              </label>
            </div>
            
            <div className="form-check">
              <input
                type="radio"
                id="actionPerformed-returned"
                name="actionPerformed"
                value="returned"
                checked={formData.actionPerformed === 'returned'}
                onChange={() => handleRadioChange('actionPerformed', 'returned')}
              />
              <label htmlFor="actionPerformed-returned">
                Return
              </label>
            </div>
          </div>
        </div>
        
        <div className="form-group">
          <label htmlFor="financialComments">Financial Comments</label>
          <textarea
            id="financialComments"
            name="financialComments"
            value={formData.financialComments || ''}
            onChange={handleChange}
            placeholder="Enter your financial assessment here..."
            rows={4}
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