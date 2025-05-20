import React, { useState, useEffect } from 'react';
import { taskApi } from '../../services/api';
import { CommonFormLayout, FormField, VariablesDisplay } from './CommonFormLayout';
import './FormStyles.css';

/**
 * Generic Task Form - Default form for any task type not specifically handled
 */
export const GenericTaskForm = ({ task, variables, onComplete }) => {
  const [formData, setFormData] = useState({
    comments: variables?.comments || ''
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Initialize form data from variables
  useEffect(() => {
    if (variables && Object.keys(variables).length > 0) {
      setFormData(prevData => ({
        ...prevData,
        comments: variables.comments || ''
      }));
    }
  }, [variables]);

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

  // Define form sections
  const formSections = [
    // Task Variables Section (if any)
    ...(Object.keys(variables).length > 0 ? [
      {
        title: 'Task Variables',
        content: <VariablesDisplay variables={variables} excludeKeys={['comments']} />
      }
    ] : []),
    
    // Your Response Section
    {
      title: 'Your Response',
      content: (
        <FormField
          label="Comments"
          id="comments"
          name="comments"
          type="textarea"
          value={formData.comments}
          onChange={handleChange}
          placeholder="Enter your comments here..."
          rows={5}
        />
      )
    }
  ];

  return (
    <CommonFormLayout
      title={task.name || 'Generic Task'}
      taskInfo={task}
      formSections={formSections}
      success={success}
      error={error}
      submitting={submitting}
      onSubmit={handleSubmit}
    />
  );
};