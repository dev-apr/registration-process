import React, { useState, useEffect } from 'react';
import { taskApi } from '../../services/api';
import { CommonFormLayout, FormField } from './CommonFormLayout';
import './FormStyles.css';

/**
 * Finance Action Form - For finance team to review inspection requests
 */
export const FinanceActionForm = ({ task, variables, onComplete }) => {
  const [formData, setFormData] = useState({
    financeDecision: variables?.financeDecision || 'class1',
    financeComments: variables?.financeComments || ''
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Initialize form data from variables
  useEffect(() => {
    if (variables && Object.keys(variables).length > 0) {
      setFormData(prevData => ({
        ...prevData,
        financeDecision: variables.financeDecision || 'class1',
        financeComments: variables.financeComments || ''
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
      
      // Check if legal decision exists and compare with finance decision
      const sameDecision = variables.legalDecision && 
                          variables.legalDecision === formData.financeDecision;
      
      // Add sameDecision to the form data if legal has already made a decision
      const submissionData = {
        ...formData,
        // Only set sameDecision if legal has already made a decision
        ...(variables.legalDecision && { sameDecision })
      };
      
      // Log the form data being sent to the backend
      console.log('Submitting task with data:', submissionData);
      
      // Send the form data to the backend
      await taskApi.completeTask(task.id, submissionData);
      
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

  // Finance decision options
  const decisionOptions = [
    { value: 'class1', label: 'Class 1' },
    { value: 'class2', label: 'Class 2' },
    { value: 'class3', label: 'Class 3' }
  ];

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
          
          {variables.comments && (
            <div className="form-group">
              <label htmlFor="previousComments">Previous Comments</label>
              <textarea
                id="previousComments"
                name="previousComments"
                value={variables.comments}
                readOnly
                rows={4}
                className="form-control"
              />
            </div>
          )}
          
          {variables.legalComments && (
            <div className="form-group">
              <label htmlFor="legalComments">Legal Assessment</label>
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
        </>
      )
    },
    {
      title: 'Financial Assessment',
      content: (
        <>
          <FormField
            label="Financial Decision:"
            id="financeDecision"
            name="financeDecision"
            type="select"
            value={formData.financeDecision}
            onChange={handleChange}
            options={decisionOptions}
          />
          
          <FormField
            label="Financial Assessment Comments"
            id="financeComments"
            name="financeComments"
            type="textarea"
            value={formData.financeComments || ''}
            onChange={handleChange}
            placeholder="Enter your financial assessment here..."
            rows={4}
          />
        </>
      )
    }
  ];

  return (
    <CommonFormLayout
      title="Finance Action"
      taskInfo={task}
      formSections={formSections}
      success={success}
      error={error}
      submitting={submitting}
      onSubmit={handleSubmit}
    />
  );
};