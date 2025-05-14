-- Create table for storing role assignment state
CREATE TABLE IF NOT EXISTS role_assignment_state (
  role_type VARCHAR(50) PRIMARY KEY,
  last_assigned_user_id VARCHAR(100),
  last_assigned_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert initial records for each role type
INSERT IGNORE INTO role_assignment_state (role_type, last_assigned_user_id) VALUES 
('admin', NULL),
('validator', NULL),
('financial-controller', NULL);