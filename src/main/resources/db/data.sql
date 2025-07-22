-- AGI Project Initial Data
-- Insert sample data for development and testing

-- Insert Roles
INSERT INTO tb_roles (name, description) VALUES 
('ADMIN', 'System Administrator with full access'),
('USER', 'Regular user with basic access'),
('DEVELOPER', 'Developer with extended access to tools and sandbox');

-- Insert Permissions
INSERT INTO tb_permissions (name, description, resource, action) VALUES 
('USER_READ', 'Read user information', 'USER', 'READ'),
('USER_WRITE', 'Create and update user information', 'USER', 'WRITE'),
('USER_DELETE', 'Delete user information', 'USER', 'DELETE'),
('CONVERSATION_READ', 'Read conversations', 'CONVERSATION', 'READ'),
('CONVERSATION_WRITE', 'Create and update conversations', 'CONVERSATION', 'WRITE'),
('CONVERSATION_DELETE', 'Delete conversations', 'CONVERSATION', 'DELETE'),
('KNOWLEDGE_READ', 'Read knowledge base', 'KNOWLEDGE', 'READ'),
('KNOWLEDGE_WRITE', 'Create and update knowledge', 'KNOWLEDGE', 'WRITE'),
('KNOWLEDGE_DELETE', 'Delete knowledge', 'KNOWLEDGE', 'DELETE'),
('TOOL_READ', 'Read tool information', 'TOOL', 'READ'),
('TOOL_EXECUTE', 'Execute tools', 'TOOL', 'EXECUTE'),
('TOOL_MANAGE', 'Manage tools', 'TOOL', 'MANAGE'),
('PLAN_READ', 'Read plans', 'PLAN', 'READ'),
('PLAN_WRITE', 'Create and update plans', 'PLAN', 'WRITE'),
('PLAN_DELETE', 'Delete plans', 'PLAN', 'DELETE'),
('SANDBOX_READ', 'Read sandbox information', 'SANDBOX', 'READ'),
('SANDBOX_EXECUTE', 'Execute commands in sandbox', 'SANDBOX', 'EXECUTE'),
('SANDBOX_MANAGE', 'Manage sandbox environments', 'SANDBOX', 'MANAGE');

-- Assign permissions to roles
-- ADMIN role gets all permissions
INSERT INTO tb_role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM tb_roles r, tb_permissions p 
WHERE r.name = 'ADMIN';

-- USER role gets basic read/write permissions
INSERT INTO tb_role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM tb_roles r, tb_permissions p 
WHERE r.name = 'USER' 
AND p.name IN ('CONVERSATION_READ', 'CONVERSATION_WRITE', 'KNOWLEDGE_READ', 'PLAN_READ', 'PLAN_WRITE');

-- DEVELOPER role gets extended permissions
INSERT INTO tb_role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM tb_roles r, tb_permissions p 
WHERE r.name = 'DEVELOPER' 
AND p.name IN ('CONVERSATION_READ', 'CONVERSATION_WRITE', 'KNOWLEDGE_READ', 'KNOWLEDGE_WRITE', 
               'TOOL_READ', 'TOOL_EXECUTE', 'PLAN_READ', 'PLAN_WRITE', 'SANDBOX_READ', 'SANDBOX_EXECUTE');

-- Insert sample users
INSERT INTO tb_users (username, email, password, first_name, last_name, nickname) VALUES 
('admin', 'admin@agi.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin', 'User', 'Administrator'),
('testuser', 'test@agi.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Test', 'User', 'Tester'),
('developer', 'dev@agi.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Dev', 'User', 'Developer');

-- Assign roles to users
INSERT INTO tb_user_roles (user_id, role_id) VALUES 
((SELECT id FROM tb_users WHERE username = 'admin'), (SELECT id FROM tb_roles WHERE name = 'ADMIN')),
((SELECT id FROM tb_users WHERE username = 'testuser'), (SELECT id FROM tb_roles WHERE name = 'USER')),
((SELECT id FROM tb_users WHERE username = 'developer'), (SELECT id FROM tb_roles WHERE name = 'DEVELOPER'));

-- Insert sample tools
INSERT INTO tb_tool (name, description, version) VALUES 
('Text Analyzer', 'Analyze text content for sentiment and keywords', '1.0.0'),
('Image Generator', 'Generate images from text descriptions', '1.2.0'),
('Code Executor', 'Execute code in various programming languages', '2.1.0');

-- Insert tool parameters
INSERT INTO tb_tool_parameter (tool_id, parameter_name, parameter_type, is_required, description) VALUES 
((SELECT id FROM tb_tool WHERE name = 'Text Analyzer'), 'text', 'STRING', TRUE, 'Text content to analyze'),
((SELECT id FROM tb_tool WHERE name = 'Text Analyzer'), 'language', 'STRING', FALSE, 'Language of the text (default: auto-detect)'),
((SELECT id FROM tb_tool WHERE name = 'Image Generator'), 'prompt', 'STRING', TRUE, 'Text description for image generation'),
((SELECT id FROM tb_tool WHERE name = 'Image Generator'), 'style', 'STRING', FALSE, 'Art style (realistic, cartoon, abstract)'),
((SELECT id FROM tb_tool WHERE name = 'Code Executor'), 'code', 'TEXT', TRUE, 'Code to execute'),
((SELECT id FROM tb_tool WHERE name = 'Code Executor'), 'language', 'STRING', TRUE, 'Programming language (python, javascript, java)');

-- Insert sandbox templates
INSERT INTO tb_sandbox_template (name, description, environment_type, configuration) VALUES 
('Python Environment', 'Python 3.11 with common libraries', 'PYTHON', '{"python_version": "3.11", "packages": ["numpy", "pandas", "requests"]}'),
('Node.js Environment', 'Node.js 18 with npm packages', 'NODEJS', '{"node_version": "18", "packages": ["express", "axios", "lodash"]}'),
('Java Environment', 'Java 17 with Maven', 'JAVA', '{"java_version": "17", "build_tool": "maven"}');

-- Insert sample knowledge
INSERT INTO tb_knowledge (title, content, source, relevance_score, verified) VALUES 
('AGI Development Principles', 'Artificial General Intelligence development requires careful consideration of safety, alignment, and capability control...', 'Internal Research', 0.95, TRUE),
('Machine Learning Best Practices', 'When developing ML models, it is important to consider data quality, model validation, and ethical implications...', 'ML Guidelines', 0.88, TRUE),
('API Design Guidelines', 'RESTful API design should follow standard HTTP methods, use proper status codes, and maintain consistency...', 'Development Standards', 0.82, TRUE);

-- Insert knowledge tags
INSERT INTO tb_knowledge_tags (knowledge_id, tag_name) VALUES 
((SELECT id FROM tb_knowledge WHERE title = 'AGI Development Principles'), 'AGI'),
((SELECT id FROM tb_knowledge WHERE title = 'AGI Development Principles'), 'Safety'),
((SELECT id FROM tb_knowledge WHERE title = 'AGI Development Principles'), 'Ethics'),
((SELECT id FROM tb_knowledge WHERE title = 'Machine Learning Best Practices'), 'ML'),
((SELECT id FROM tb_knowledge WHERE title = 'Machine Learning Best Practices'), 'Best Practices'),
((SELECT id FROM tb_knowledge WHERE title = 'API Design Guidelines'), 'API'),
((SELECT id FROM tb_knowledge WHERE title = 'API Design Guidelines'), 'REST'),
((SELECT id FROM tb_knowledge WHERE title = 'API Design Guidelines'), 'Design');

