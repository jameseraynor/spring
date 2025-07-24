-- Insert roles
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Administrator with full access'),
('USER', 'Regular user with limited access');

-- Insert users with encoded passwords (password123 encoded with BCrypt)
INSERT INTO users (name, email, password, department, enabled) VALUES 
('John Doe', 'john.doe@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jCSKPNX.Uy6G', 'Engineering', true),
('Jane Smith', 'jane.smith@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jCSKPNX.Uy6G', 'Marketing', true),
('Bob Johnson', 'bob.johnson@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jCSKPNX.Uy6G', 'Engineering', true),
('Alice Brown', 'alice.brown@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jCSKPNX.Uy6G', 'HR', true),
('Charlie Wilson', 'charlie.wilson@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jCSKPNX.Uy6G', 'Sales', true),
('Admin User', 'admin@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jCSKPNX.Uy6G', 'IT', true);

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 2), -- John Doe -> USER
(2, 2), -- Jane Smith -> USER
(3, 2), -- Bob Johnson -> USER
(4, 2), -- Alice Brown -> USER
(5, 2), -- Charlie Wilson -> USER
(6, 1), -- Admin User -> ADMIN
(6, 2); -- Admin User -> USER (admin can also have user privileges)