DROP TABLE IF EXISTS note CASCADE;
DROP TABLE IF EXISTS account_company CASCADE;
DROP TABLE IF EXISTS log CASCADE;
DROP TABLE IF EXISTS static_file CASCADE;
DROP TABLE IF EXISTS account CASCADE;
DROP TABLE IF EXISTS company CASCADE;

CREATE TABLE account(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_time TIMESTAMP NOT NULL,
    modified_time TIMESTAMP NOT NULL,
    CONSTRAINT business_key_account UNIQUE(username)
);

CREATE TABLE company(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    contact VARCHAR(50),
    phone VARCHAR(20),
    status SMALLINT NOT NULL DEFAULT 1,
    created_time TIMESTAMP NOT NULL,
    modified_time TIMESTAMP NOT NULL,
    CONSTRAINT business_key_company UNIQUE(code)
);

CREATE TABLE account_company(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    company_id UUID NOT NULL,
    role VARCHAR(50),
    created_time TIMESTAMP NOT NULL,
    modified_time TIMESTAMP NOT NULL,
    CONSTRAINT fk_account_company_account FOREIGN KEY(account_id) REFERENCES account(id),
    CONSTRAINT fk_account_company_company FOREIGN KEY(company_id) REFERENCES company(id),
    CONSTRAINT business_key_account_company UNIQUE(account_id, company_id)
);

CREATE TABLE log(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id UUID,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    request_method VARCHAR(10),
    request_url VARCHAR(500),
    response_status INT,
    error_message TEXT,
    created_time TIMESTAMP NOT NULL,
    modified_time TIMESTAMP NOT NULL
);

CREATE TABLE static_file(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100),
    file_type VARCHAR(50),
    uploader_id UUID,
    created_time TIMESTAMP NOT NULL,
    modified_time TIMESTAMP NOT NULL
);

CREATE TABLE note(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_time TIMESTAMP NOT NULL,
    modified_time TIMESTAMP NOT NULL,
    CONSTRAINT business_key_note UNIQUE(title)
);

CREATE INDEX idx_account_company_account_id ON account_company(account_id);
CREATE INDEX idx_account_company_company_id ON account_company(company_id);
CREATE INDEX idx_log_account_id ON log(account_id);
CREATE INDEX idx_log_created_time ON log(created_time);
CREATE INDEX idx_static_file_uploader_id ON static_file(uploader_id);

INSERT INTO account(username, password, email, phone, role, status, created_time, modified_time) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com', '13800138000', 'ADMIN', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user1@example.com', '13800138001', 'USER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO company(name, code, address, contact, phone, status, created_time, modified_time) VALUES
    ('示例企业A', 'COMP001', '北京市朝阳区', '张三', '010-12345678', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('示例企业B', 'COMP002', '上海市浦东新区', '李四', '021-87654321', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO note(title, content, created_time, modified_time) VALUES
    ('Sample Note 1', 'This is a sample note content', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Sample Note 2', 'Another sample note', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
