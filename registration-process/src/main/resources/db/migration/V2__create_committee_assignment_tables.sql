-- Create table for storing committee assignment state
CREATE TABLE IF NOT EXISTS committee_assignment_state (
  id INT PRIMARY KEY,
  last_assigned_committee VARCHAR(50),
  last_assigned_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert initial record
INSERT IGNORE INTO committee_assignment_state (id, last_assigned_committee) VALUES (1, NULL);

-- Create table for mapping process instances to committees
CREATE TABLE IF NOT EXISTS process_committee_mapping (
  process_instance_id VARCHAR(100) PRIMARY KEY,
  committee VARCHAR(50) NOT NULL,
  created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);