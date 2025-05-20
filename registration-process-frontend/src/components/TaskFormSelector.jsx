import React from 'react';
import { AdminActionForm } from './forms/AdminActionForm';
import { ValidatorActionForm } from './forms/ValidatorActionForm';
import { FinancialControllerActionForm } from './forms/FinancialControllerActionForm';
import { LegalActionForm } from './forms/LegalActionForm';
import { FinanceActionForm } from './forms/FinanceActionForm';
import { HeadActionForm } from './forms/HeadActionForm';
import { GenericTaskForm } from './forms/GenericTaskForm';

/**
 * Task Form Selector - Determines which form to render based on task type
 */
export const TaskFormSelector = ({ task, variables, onComplete }) => {
  if (!task) {
    return <div>No task selected</div>;
  }

  // Determine task type from candidate groups or task ID
  const taskType = getTaskType(task);
  
  // Render the appropriate form based on task type
  switch (taskType) {
    case 'adminAction':
      return <AdminActionForm task={task} variables={variables} onComplete={onComplete} />;
    case 'validatorAction':
      return <ValidatorActionForm task={task} variables={variables} onComplete={onComplete} />;
    case 'financialControllerAction':
      return <FinancialControllerActionForm task={task} variables={variables} onComplete={onComplete} />;
    case 'legalAction':
      return <LegalActionForm task={task} variables={variables} onComplete={onComplete} />;
    case 'financeAction':
      return <FinanceActionForm task={task} variables={variables} onComplete={onComplete} />;
    case 'headAction':
      return <HeadActionForm task={task} variables={variables} onComplete={onComplete} />;
    default:
      return <GenericTaskForm task={task} variables={variables} onComplete={onComplete} />;
  }
};

/**
 * Get the task type from the task object
 */
const getTaskType = (task) => {
  // First try to get from task ID
  if (task.taskDefinitionId) {
    // Extract the task type from the task definition ID
    // Example: "adminAction" from "Process_registrationProcess:adminAction"
    const parts = task.taskDefinitionId.split(':');
    if (parts.length > 1) {
      return parts[1];
    }
  }
  
  // If not found, try to get from candidate groups
  if (task.candidateGroups && task.candidateGroups.length > 0) {
    const group = task.candidateGroups[0];
    console.log(group)
    
    // Map group to task type
    switch (group) {
      case 'admin':
        return 'adminAction';
      case 'validator':
        return 'validatorAction';
      case 'financial-controller':
        return 'financialControllerAction';
      case 'legal':
        return 'legalAction';
      case 'finance':
        return 'financeAction';
      case 'head':
        return 'headAction';
      default:
        return 'genericTask';
    }
  }
  
  // Default to generic task
  return 'genericTask';
};