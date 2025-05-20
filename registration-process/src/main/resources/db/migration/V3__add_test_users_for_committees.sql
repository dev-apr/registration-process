-- Add test users for committee assignments
-- This is just for testing purposes, in production these would be managed in Keycloak

-- Add entries for users in both legal and c1
INSERT IGNORE INTO role_assignment_state (role_type, last_assigned_user_id) VALUES 
('legal-c1', NULL);

-- Add entries for users in both legal and c2
INSERT IGNORE INTO role_assignment_state (role_type, last_assigned_user_id) VALUES 
('legal-c2', NULL);

-- Add entries for users in both finance and c1
INSERT IGNORE INTO role_assignment_state (role_type, last_assigned_user_id) VALUES 
('finance-c1', NULL);

-- Add entries for users in both finance and c2
INSERT IGNORE INTO role_assignment_state (role_type, last_assigned_user_id) VALUES 
('finance-c2', NULL);

-- Add entries for users in both head and c1
INSERT IGNORE INTO role_assignment_state (role_type, last_assigned_user_id) VALUES 
('head-c1', NULL);

-- Add entries for users in both head and c2
INSERT IGNORE INTO role_assignment_state (role_type, last_assigned_user_id) VALUES 
('head-c2', NULL);

-- Add entries for the committees themselves
INSERT IGNORE INTO role_assignment_state (role_type, last_assigned_user_id) VALUES 
('c1', NULL),
('c2', NULL);