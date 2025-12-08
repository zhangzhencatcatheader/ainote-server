-- ============================================
-- 台账模板系统 SQL 脚本
-- 支持创建灵活的台账模板，如安全生产巡查台账等
-- ============================================

-- 删除已存在的表（按依赖关系逆序删除）
DROP TABLE IF EXISTS ledger_record_value CASCADE;
DROP TABLE IF EXISTS ledger_record CASCADE;
DROP TABLE IF EXISTS ledger_template_field CASCADE;
DROP TABLE IF EXISTS ledger_template CASCADE;

-- ============================================
-- 1. 台账模板表 (ledger_template)
-- 定义台账的基本信息和结构
-- ============================================
CREATE TABLE ledger_template(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,                    -- 模板名称，如"安全生产巡查台账"
    description TEXT,                              -- 模板描述
    category VARCHAR(50),                          -- 分类，如"safety"、"equipment"、"maintenance"
    version INT NOT NULL DEFAULT 1,                -- 版本号
    enabled BOOLEAN NOT NULL DEFAULT TRUE,         -- 是否启用
    icon VARCHAR(100),                             -- 图标名称或URL
    color VARCHAR(20),                             -- 主题颜色
    sort_order INT DEFAULT 0,                      -- 排序顺序
    file_id UUID,
    -- 审计字段
    account_id UUID NOT NULL,                      -- 创建人
    tenant VARCHAR(50) NOT NULL,                   -- 租户标识
    created_time TIMESTAMP NOT NULL,               -- 创建时间
    modified_time TIMESTAMP NOT NULL,              -- 修改时间
    
    -- 约束
    CONSTRAINT business_key_ledger_template UNIQUE(tenant, name),
    CONSTRAINT fk_ledger_template_account FOREIGN KEY(account_id) REFERENCES account(id)
);

-- 索引
CREATE INDEX idx_ledger_template_tenant ON ledger_template(tenant);
CREATE INDEX idx_ledger_template_category ON ledger_template(category);
CREATE INDEX idx_ledger_template_enabled ON ledger_template(enabled);
CREATE INDEX idx_ledger_template_account_id ON ledger_template(account_id);

-- 注释
COMMENT ON TABLE ledger_template IS '台账模板表';
COMMENT ON COLUMN ledger_template.name IS '模板名称';
COMMENT ON COLUMN ledger_template.description IS '模板描述';
COMMENT ON COLUMN ledger_template.category IS '模板分类';
COMMENT ON COLUMN ledger_template.version IS '版本号';
COMMENT ON COLUMN ledger_template.enabled IS '是否启用';
COMMENT ON COLUMN ledger_template.tenant IS '租户标识';

-- ============================================
-- 2. 台账模板字段表 (ledger_template_field)
-- 定义台账模板的字段结构
-- ============================================
CREATE TABLE ledger_template_field(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID NOT NULL,                     -- 所属模板ID
    field_name VARCHAR(100) NOT NULL,              -- 字段名称（英文标识）
    field_label VARCHAR(100) NOT NULL,             -- 字段标签（显示名称）
    field_type VARCHAR(50) NOT NULL,               -- 字段类型：TEXT, TEXTAREA, NUMBER, DATE, DATETIME, SELECT等
    field_options TEXT,                            -- 字段选项（JSON格式，用于SELECT、MULTISELECT等）
    default_value VARCHAR(500),                    -- 默认值
    placeholder VARCHAR(200),                      -- 占位符提示
    help_text VARCHAR(500),                        -- 帮助说明
    
    -- 验证规则
    required BOOLEAN NOT NULL DEFAULT FALSE,       -- 是否必填
    min_length INT,                                -- 最小长度（文本类型）
    max_length INT,                                -- 最大长度（文本类型）
    min_value DECIMAL(20,4),                       -- 最小值（数字类型）
    max_value DECIMAL(20,4),                       -- 最大值（数字类型）
    pattern VARCHAR(200),                          -- 正则表达式验证
    
    -- 显示控制
    sort_order INT NOT NULL DEFAULT 0,             -- 字段排序
    width VARCHAR(20),                             -- 字段宽度（如"50%"、"200px"）
    visible BOOLEAN NOT NULL DEFAULT TRUE,         -- 是否可见
    editable BOOLEAN NOT NULL DEFAULT TRUE,        -- 是否可编辑
    searchable BOOLEAN NOT NULL DEFAULT FALSE,     -- 是否可搜索
    
    -- 审计字段
    created_time TIMESTAMP NOT NULL,
    modified_time TIMESTAMP NOT NULL,
    
    -- 约束
    CONSTRAINT business_key_ledger_template_field UNIQUE(template_id, field_name),
    CONSTRAINT fk_ledger_template_field_template FOREIGN KEY(template_id) REFERENCES ledger_template(id) ON DELETE CASCADE
);

-- 索引
CREATE INDEX idx_ledger_template_field_template_id ON ledger_template_field(template_id);
CREATE INDEX idx_ledger_template_field_sort_order ON ledger_template_field(template_id, sort_order);

-- 注释
COMMENT ON TABLE ledger_template_field IS '台账模板字段表';
COMMENT ON COLUMN ledger_template_field.field_name IS '字段名称（英文标识）';
COMMENT ON COLUMN ledger_template_field.field_label IS '字段标签（中文显示名）';
COMMENT ON COLUMN ledger_template_field.field_type IS '字段类型';
COMMENT ON COLUMN ledger_template_field.field_options IS '字段选项（JSON格式）';
COMMENT ON COLUMN ledger_template_field.required IS '是否必填';

-- ============================================
-- 3. 台账记录表 (ledger_record)
-- 存储根据模板创建的实际台账记录
-- ============================================
CREATE TABLE ledger_record(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID NOT NULL,                     -- 所属模板ID
    record_no VARCHAR(100),                        -- 记录编号（可自定义规则生成）
    record_date DATE NOT NULL,                     -- 记录日期
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',   -- 状态：DRAFT草稿、SUBMITTED已提交、APPROVED已审批、REJECTED已驳回
    
    -- 关联信息
    company_id UUID,                               -- 关联的公司ID（可选）
    
    -- 审批信息
    submitter_id UUID,                             -- 提交人ID
    submitted_time TIMESTAMP,                      -- 提交时间
    approver_id UUID,                              -- 审批人ID
    approved_time TIMESTAMP,                       -- 审批时间
    approval_comment TEXT,                         -- 审批意见
    
    -- 备注
    remark TEXT,                                   -- 备注信息
    
    -- 审计字段
    account_id UUID NOT NULL,                      -- 创建人
    tenant VARCHAR(50) NOT NULL,                   -- 租户标识
    created_time TIMESTAMP NOT NULL,
    modified_time TIMESTAMP NOT NULL,
    
    -- 约束
    CONSTRAINT fk_ledger_record_template FOREIGN KEY(template_id) REFERENCES ledger_template(id),
    CONSTRAINT fk_ledger_record_account FOREIGN KEY(account_id) REFERENCES account(id),
    CONSTRAINT fk_ledger_record_company FOREIGN KEY(company_id) REFERENCES company(id),
    CONSTRAINT fk_ledger_record_submitter FOREIGN KEY(submitter_id) REFERENCES account(id),
    CONSTRAINT fk_ledger_record_approver FOREIGN KEY(approver_id) REFERENCES account(id)
);

-- 索引
CREATE INDEX idx_ledger_record_template_id ON ledger_record(template_id);
CREATE INDEX idx_ledger_record_tenant ON ledger_record(tenant);
CREATE INDEX idx_ledger_record_record_date ON ledger_record(record_date);
CREATE INDEX idx_ledger_record_status ON ledger_record(status);
CREATE INDEX idx_ledger_record_account_id ON ledger_record(account_id);
CREATE INDEX idx_ledger_record_company_id ON ledger_record(company_id);
CREATE INDEX idx_ledger_record_no ON ledger_record(record_no);

-- 注释
COMMENT ON TABLE ledger_record IS '台账记录表';
COMMENT ON COLUMN ledger_record.record_no IS '记录编号';
COMMENT ON COLUMN ledger_record.record_date IS '记录日期';
COMMENT ON COLUMN ledger_record.status IS '记录状态';

-- ============================================
-- 4. 台账记录值表 (ledger_record_value)
-- 存储台账记录的字段值（EAV模式）
-- ============================================
CREATE TABLE ledger_record_value(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    record_id UUID NOT NULL,                       -- 所属记录ID
    field_id UUID NOT NULL,                        -- 字段定义ID
    field_value TEXT,                              -- 字段值（所有类型统一存储为文本，前端根据field_type解析）
    
    -- 审计字段
    created_time TIMESTAMP NOT NULL,
    modified_time TIMESTAMP NOT NULL,
    
    -- 约束
    CONSTRAINT business_key_ledger_record_value UNIQUE(record_id, field_id),
    CONSTRAINT fk_ledger_record_value_record FOREIGN KEY(record_id) REFERENCES ledger_record(id) ON DELETE CASCADE,
    CONSTRAINT fk_ledger_record_value_field FOREIGN KEY(field_id) REFERENCES ledger_template_field(id)
);

-- 索引
CREATE INDEX idx_ledger_record_value_record_id ON ledger_record_value(record_id);
CREATE INDEX idx_ledger_record_value_field_id ON ledger_record_value(field_id);

-- 注释
COMMENT ON TABLE ledger_record_value IS '台账记录值表';
COMMENT ON COLUMN ledger_record_value.field_value IS '字段值（文本格式存储）';

-- ============================================
-- 示例数据：安全生产巡查台账模板
-- ============================================

-- 插入模板
INSERT INTO ledger_template(
    id, name, description, category, version, enabled, 
    icon, color, sort_order, account_id, tenant, 
    created_time, modified_time
) VALUES (
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    '安全生产巡查台账',
    '用于记录日常安全生产巡查情况，包括巡查时间、地点、发现问题及整改措施',
    'safety',
    1,
    TRUE,
    'safety',
    '#FF5722',
    1,
    (SELECT id FROM account WHERE username = 'admin' LIMIT 1),
    'default',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 插入字段定义
INSERT INTO ledger_template_field(
    template_id, field_name, field_label, field_type, 
    required, sort_order, visible, editable, searchable,
    placeholder, help_text, created_time, modified_time
) VALUES 
-- 1. 巡查日期
(
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'inspection_date',
    '巡查日期',
    'DATE',
    TRUE,
    1,
    TRUE,
    TRUE,
    TRUE,
    '请选择巡查日期',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- 2. 巡查时间
(
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'inspection_time',
    '巡查时间',
    'TIME',
    TRUE,
    2,
    TRUE,
    TRUE,
    FALSE,
    '请选择巡查时间',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- 3. 巡查地点
(
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'inspection_location',
    '巡查地点',
    'TEXT',
    TRUE,
    3,
    TRUE,
    TRUE,
    TRUE,
    '请输入巡查地点',
    '如：生产车间、仓库、办公楼等',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- 4. 巡查人员
(
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'inspector',
    '巡查人员',
    'TEXT',
    TRUE,
    4,
    TRUE,
    TRUE,
    TRUE,
    '请输入巡查人员姓名',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- 5. 巡查类型
(
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'inspection_type',
    '巡查类型',
    'SELECT',
    TRUE,
    5,
    TRUE,
    TRUE,
    TRUE,
    '请选择巡查类型',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
-- 字段选项以JSON格式存储
UPDATE ledger_template_field 
SET field_options = '["日常巡查", "专项检查", "突击检查", "节假日检查"]'::jsonb
WHERE field_name = 'inspection_type';

INSERT INTO ledger_template_field(
    template_id, field_name, field_label, field_type, 
    required, sort_order, visible, editable, searchable,
    placeholder, help_text, created_time, modified_time
) VALUES
-- 6. 发现问题
(
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'issues_found',
    '发现问题',
    'TEXTAREA',
    FALSE,
    6,
    TRUE,
    TRUE,
    TRUE,
    '请详细描述发现的安全隐患或问题',
    '如发现多个问题请分条描述',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- 7. 问题等级
(
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'issue_level',
    '问题等级',
    'SELECT',
    FALSE,
    7,
    TRUE,
    TRUE,
    TRUE,
    '请选择问题严重程度',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- 8. 整改措施
(
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'corrective_action',
    '整改措施',
    'TEXTAREA',
    FALSE,
    8,
    TRUE,
    TRUE,
    FALSE,
    '请输入整改措施和建议',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- 9. 整改期限
(
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'deadline',
    '整改期限',
    'DATE',
    FALSE,
    9,
    TRUE,
    TRUE,
    FALSE,
    '请选择整改完成期限',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- 10. 责任人
(
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'responsible_person',
    '责任人',
    'TEXT',
    FALSE,
    10,
    TRUE,
    TRUE,
    TRUE,
    '请输入整改责任人',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- 11. 是否已整改
(
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'is_resolved',
    '是否已整改',
    'BOOLEAN',
    FALSE,
    11,
    TRUE,
    TRUE,
    FALSE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- 12. 现场照片
(
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'photos',
    '现场照片',
    'FILE',
    FALSE,
    12,
    TRUE,
    TRUE,
    FALSE,
    '上传现场照片（最多9张）',
    '支持JPG、PNG格式，单张不超过5MB',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 更新问题等级选项
UPDATE ledger_template_field 
SET field_options = '["一般隐患", "较大隐患", "重大隐患", "无问题"]'::jsonb
WHERE field_name = 'issue_level';

-- ============================================
-- 示例数据：台账记录示例
-- ============================================

-- 插入一条示例记录
INSERT INTO ledger_record(
    id, template_id, record_no, record_date, status,
    submitter_id, submitted_time, remark,
    account_id, tenant, created_time, modified_time
) VALUES (
    'b2c3d4e5-f6a7-4b5c-8d9e-0f1a2b3c4d5e',
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'AQ20251207001',
    '2025-12-07',
    'SUBMITTED',
    (SELECT id FROM account WHERE username = 'admin' LIMIT 1),
    CURRENT_TIMESTAMP,
    '第一次安全巡查记录',
    (SELECT id FROM account WHERE username = 'admin' LIMIT 1),
    'default',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 插入记录字段值
INSERT INTO ledger_record_value(record_id, field_id, field_value, created_time, modified_time)
SELECT 
    'b2c3d4e5-f6a7-4b5c-8d9e-0f1a2b3c4d5e',
    id,
    CASE field_name
        WHEN 'inspection_date' THEN '2025-12-07'
        WHEN 'inspection_time' THEN '14:30:00'
        WHEN 'inspection_location' THEN '生产车间A区'
        WHEN 'inspector' THEN '张三'
        WHEN 'inspection_type' THEN '日常巡查'
        WHEN 'issues_found' THEN '1. 消防栓前堆放杂物\n2. 部分工人未佩戴安全帽'
        WHEN 'issue_level' THEN '一般隐患'
        WHEN 'corrective_action' THEN '1. 立即清理消防栓周边杂物\n2. 加强安全教育，要求必须佩戴安全帽'
        WHEN 'deadline' THEN '2025-12-10'
        WHEN 'responsible_person' THEN '李四'
        WHEN 'is_resolved' THEN 'false'
    END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM ledger_template_field
WHERE template_id = 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d'
AND field_name IN ('inspection_date', 'inspection_time', 'inspection_location', 'inspector', 
                   'inspection_type', 'issues_found', 'issue_level', 'corrective_action', 
                   'deadline', 'responsible_person', 'is_resolved');
