-- AGI Project Database Schema
-- H2 Database with tb_ prefix to avoid reserved word conflicts

-- Drop tables if exists (for clean restart)
DROP TABLE IF EXISTS tb_role_permissions;
DROP TABLE IF EXISTS tb_user_roles;
DROP TABLE IF EXISTS tb_knowledge_tags;
DROP TABLE IF EXISTS tb_sandbox_execution;
DROP TABLE IF EXISTS tb_sandbox_template;
DROP TABLE IF EXISTS tb_tool_execution;
DROP TABLE IF EXISTS tb_tool_parameter;
DROP TABLE IF EXISTS tb_plan_step;
DROP TABLE IF EXISTS tb_message;
DROP TABLE IF EXISTS tb_conversation;
DROP TABLE IF EXISTS tb_knowledge;
DROP TABLE IF EXISTS tb_sandbox;
DROP TABLE IF EXISTS tb_tool;
DROP TABLE IF EXISTS tb_plan;
DROP TABLE IF EXISTS tb_permissions;
DROP TABLE IF EXISTS tb_roles;
DROP TABLE IF EXISTS tb_users;

-- User Domain Tables
CREATE TABLE tb_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    nickname VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    profile_image_url VARCHAR(500),
    preferences TEXT
);

CREATE TABLE tb_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES tb_roles(id) ON DELETE CASCADE,
    UNIQUE(user_id, role_id)
);

CREATE TABLE tb_role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES tb_roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES tb_permissions(id) ON DELETE CASCADE,
    UNIQUE(role_id, permission_id)
);

-- Conversation Domain Tables
CREATE TABLE tb_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE
);

CREATE TABLE tb_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM')),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    embedding BLOB,
    metadata TEXT,
    FOREIGN KEY (conversation_id) REFERENCES tb_conversation(id) ON DELETE CASCADE
);

-- Knowledge Domain Tables
CREATE TABLE tb_knowledge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    source VARCHAR(500) NOT NULL,
    relevance_score DOUBLE NOT NULL DEFAULT 0.0,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE tb_knowledge_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    knowledge_id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (knowledge_id) REFERENCES tb_knowledge(id) ON DELETE CASCADE,
    UNIQUE(knowledge_id, tag_name)
);

-- Tool Domain Tables
CREATE TABLE tb_tool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    version VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE tb_tool_parameter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    parameter_name VARCHAR(100) NOT NULL,
    parameter_type VARCHAR(50) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    default_value TEXT,
    description TEXT,
    FOREIGN KEY (tool_id) REFERENCES tb_tool(id) ON DELETE CASCADE,
    UNIQUE(tool_id, parameter_name)
);

CREATE TABLE tb_tool_execution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    input_parameters TEXT,
    output_result TEXT,
    execution_status VARCHAR(20) NOT NULL CHECK (execution_status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED')),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    FOREIGN KEY (tool_id) REFERENCES tb_tool(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE
);

-- Plan Domain Tables
CREATE TABLE tb_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    priority INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    due_date TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE
);

CREATE TABLE tb_plan_step (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    step_order INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'SKIPPED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES tb_plan(id) ON DELETE CASCADE,
    UNIQUE(plan_id, step_order)
);

-- Sandbox Domain Tables
CREATE TABLE tb_sandbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    environment_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('CREATING', 'ACTIVE', 'STOPPED', 'TERMINATED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE
);

CREATE TABLE tb_sandbox_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    environment_type VARCHAR(50) NOT NULL,
    configuration TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_sandbox_execution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sandbox_id BIGINT NOT NULL,
    command TEXT NOT NULL,
    output TEXT,
    error_output TEXT,
    exit_code INTEGER,
    execution_status VARCHAR(20) NOT NULL CHECK (execution_status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED')),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (sandbox_id) REFERENCES tb_sandbox(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_users_username ON tb_users(username);
CREATE INDEX idx_users_email ON tb_users(email);
CREATE INDEX idx_conversation_user_id ON tb_conversation(user_id);
CREATE INDEX idx_message_conversation_id ON tb_message(conversation_id);
CREATE INDEX idx_knowledge_verified ON tb_knowledge(verified);
CREATE INDEX idx_tool_execution_user_id ON tb_tool_execution(user_id);
CREATE INDEX idx_plan_user_id ON tb_plan(user_id);
CREATE INDEX idx_plan_status ON tb_plan(status);
CREATE INDEX idx_sandbox_user_id ON tb_sandbox(user_id);

