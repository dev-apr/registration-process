import React, { useState, useEffect } from 'react';
import { taskApi } from '../../services/api';
import { CommonFormLayout, FormField } from './CommonFormLayout';
import './FormStyles.css';

/**
 * Admin Action Form - Allows admin to edit wage and date
 */
export const AdminActionForm = ({ task, variables, onComplete }) => {
  const [formData, setFormData] = useState({
    wage: variables?.wage || '',
    requestDate: variables?.requestDate ? formatDate(variables.requestDate) : formatDate(new Date()),
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
        wage: variables.wage || '',
        requestDate: formatDate(variables.requestDate) || formatDate(new Date()),
        comments: variables.comments || ''
      }));
    }
  }, [variables]);

  // Format date for input if available
  function formatDate(dateString) {
    if (!dateString) return '';
    
    try {
      const date = new Date(dateString);
      return date.toISOString().split('T')[0];
    } catch (e) {
      console.error('Error formatting date:', e);
      return '';
    }
  }

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
    {
      title: 'Request Information',
      content: (
        <>
          <FormField
            label="Wage ($)"
            id="wage"
            name="wage"
            type="number"
            value={formData.wage}
            onChange={handleChange}
            required={true}
          />
          
          <FormField
            label="Request Date"
            id="requestDate"
            name="requestDate"
            type="date"
            value={formData.requestDate}
            onChange={handleChange}
            required={true}
          />
        </>
      )
    },
    {
      title: 'Additional Information',
      content: (
        <FormField
          label="Comments"
          id="comments"
          name="comments"
          type="textarea"
          value={formData.comments}
          onChange={handleChange}
          placeholder="Enter any comments here..."
          rows={4}
        />
      )
    }
  ];

  return (
    <CommonFormLayout
      title="Admin Action"
      taskInfo={task}
      formSections={formSections}
      success={success}
      error={error}
      submitting={submitting}
      onSubmit={handleSubmit}
    />
  );
};