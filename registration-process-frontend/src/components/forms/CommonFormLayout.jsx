import React from 'react';
import './FormStyles.css';

/**
 * Common Form Layout - Provides consistent structure for all task forms
 */
export const CommonFormLayout = ({ 
  title, 
  taskInfo, 
  formSections, 
  success, 
  error, 
  submitting, 
  onSubmit 
}) => {
  return (
    <form className="task-form" onSubmit={onSubmit}>
      <h2>{title}</h2>
      
      {/* Task Information Section */}
      {taskInfo && (
        <div className="form-section">
          <h3>Task Information</h3>
          <div className="task-info">
            {taskInfo.id && <p><strong>Task ID:</strong> {taskInfo.id}</p>}
            {taskInfo.name && <p><strong>Task Name:</strong> {taskInfo.name}</p>}
            {taskInfo.taskDefinitionId && (
              <p><strong>Task Type:</strong> {taskInfo.taskDefinitionId}</p>
            )}
            {taskInfo.candidateGroups && taskInfo.candidateGroups.length > 0 && (
              <p><strong>Candidate Groups:</strong> {taskInfo.candidateGroups.join(', ')}</p>
            )}
          </div>
        </div>
      )}
      
      {/* Form Sections */}
      {formSections.map((section, index) => (
        <div className="form-section" key={`section-${index}`}>
          <h3>{section.title}</h3>
          {section.content}
        </div>
      ))}
      
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

/**
 * Form Field - Reusable component for form fields
 */
export const FormField = ({ 
  label, 
  id, 
  name, 
  type = 'text', 
  value, 
  onChange, 
  placeholder = '', 
  required = false, 
  readOnly = false,
  rows = 4,
  options = []
}) => {
  if (type === 'textarea') {
    return (
      <div className="form-group">
        <label htmlFor={id}>{label}</label>
        <textarea
          id={id}
          name={name}
          value={value || ''}
          onChange={onChange}
          placeholder={placeholder}
          rows={rows}
          required={required}
          readOnly={readOnly}
        />
      </div>
    );
  }
  
  if (type === 'select') {
    return (
      <div className="form-group">
        <label htmlFor={id}>{label}</label>
        <select
          id={id}
          name={name}
          value={value || ''}
          onChange={onChange}
          required={required}
          disabled={readOnly}
        >
          {options.map(option => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
    );
  }
  
  if (type === 'radio') {
    return (
      <div className="form-group">
        <label>{label}</label>
        <div className="radio-group">
          {options.map(option => (
            <div className="form-check" key={`${name}-${option.value}`}>
              <input
                type="radio"
                id={`${name}-${option.value}`}
                name={name}
                value={option.value}
                checked={value === option.value}
                onChange={(e) => {
                  // Create a synthetic event object that mimics the standard onChange event
                  onChange({
                    target: {
                      name: name,
                      value: option.value,
                      type: 'radio'
                    },
                    preventDefault: () => {}
                  });
                }}
                disabled={readOnly}
              />
              <label htmlFor={`${name}-${option.value}`}>
                {option.label}
              </label>
            </div>
          ))}
        </div>
      </div>
    );
  }
  
  return (
    <div className="form-group">
      <label htmlFor={id}>{label}</label>
      <input
        type={type}
        id={id}
        name={name}
        value={value || ''}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        readOnly={readOnly}
      />
    </div>
  );
};

/**
 * Variables Display - Shows task variables in a consistent format
 */
export const VariablesDisplay = ({ variables, excludeKeys = [] }) => {
  const filteredVariables = Object.entries(variables || {}).filter(
    ([key]) => !excludeKeys.includes(key)
  );
  
  if (filteredVariables.length === 0) {
    return null;
  }
  
  return (
    <div className="variables-list">
      {filteredVariables.map(([key, value]) => (
        <div className="variable-item" key={key}>
          <strong>{key}:</strong> {
            typeof value === 'object' ? JSON.stringify(value) : value
          }
        </div>
      ))}
    </div>
  );
};