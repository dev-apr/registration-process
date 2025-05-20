import React, { useState, useEffect } from 'react';
import { taskApi } from '../../services/api';
import { CommonFormLayout, FormField } from './CommonFormLayout';
import './FormStyles.css';

/**
 * Head Action Form - For department head to make final decision
 */
export const HeadActionForm = ({ task, variables, onComplete }) => {
  const [formData, setFormData] = useState({
    headComments: variables?.headComments || ''
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Initialize form data from variables
  useEffect(() => {
    if (variables && Object.keys(variables).length > 0) {
      setFormData(prevData => ({
        ...prevData,
        headComments: variables.headComments || ''
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

  // Get decision status message
  const getDecisionStatusMessage = () => {
    if (!variables.legalDecision || !variables.financeDecision) {
      return "Missing decisions from legal or finance";
    }
    
    if (variables.sameDecision) {
      return `Legal and Finance made the same decision: ${variables.legalDecision}`;
    } else {
      return `Legal (${variables.legalDecision}) and Finance (${variables.financeDecision}) made different decisions`;
    }
  };

  // Define form sections
  const formSections = [
    {
      title: 'Case Information',
      content: (
        <>
          <div className="form-group">
            <label htmlFor="wage">Wage ($)</label>
            <input
              type="number"
              id="wage"
              name="wage"
              value={variables.wage || ''}
              readOnly
              className="form-control"
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="requestDate">Request Date</label>
            <input
              type="date"
              id="requestDate"
              name="requestDate"
              value={formatDate(variables.requestDate)}
              readOnly
              className="form-control"
            />
          </div>
        </>
      )
    },
    {
      title: 'Previous Assessments',
      content: (
        <>
          <div className="form-group">
            <label>Decision Status</label>
            <div className={`alert ${variables.sameDecision ? 'alert-success' : 'alert-warning'}`}>
              {getDecisionStatusMessage()}
            </div>
          </div>
          
          {variables.legalDecision && (
            <div className="form-group">
              <label htmlFor="legalDecision">Legal Decision</label>
              <input
                type="text"
                id="legalDecision"
                value={variables.legalDecision}
                readOnly
                className="form-control"
              />
            </div>
          )}
          
          {variables.legalComments && (
            <div className="form-group">
              <label htmlFor="legalComments">Legal Comments</label>
              <textarea
                id="legalComments"
                name="legalComments"
                value={variables.legalComments}
                readOnly
                rows={4}
                className="form-control"
              />
            </div>
          )}
          
          {variables.financeDecision && (
            <div className="form-group">
              <label htmlFor="financeDecision">Finance Decision</label>
              <input
                type="text"
                id="financeDecision"
                value={variables.financeDecision}
                readOnly
                className="form-control"
              />
            </div>
          )}
          
          {variables.financeComments && (
            <div className="form-group">
              <label htmlFor="financeComments">Finance Comments</label>
              <textarea
                id="financeComments"
                name="financeComments"
                value={variables.financeComments}
                readOnly
                rows={4}
                className="form-control"
              />
            </div>
          )}
        </>
      )
    },
    {
      title: 'Final Approval',
      content: (
        <>
          <div className="form-group">
            <p className="alert alert-info">
              Review the legal and finance decisions above and provide your comments before completing the task.
            </p>
          </div>
          
          <FormField
            label="Comments"
            id="headComments"
            name="headComments"
            type="textarea"
            value={formData.headComments || ''}
            onChange={handleChange}
            placeholder="Enter your comments here..."
            rows={4}
          />
        </>
      )
    }
  ];

  return (
    <CommonFormLayout
      title="Head Action"
      taskInfo={task}
      formSections={formSections}
      success={success}
      error={error}
      submitting={submitting}
      onSubmit={handleSubmit}
    />
  );
};