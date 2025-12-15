# SQL 修改说明：JSON 字段支持

## 修改内容

### 1. 表结构修改 - ledger_record_value

#### 原结构
```sql
CREATE TABLE ledger_record_value(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    record_id UUID NOT NULL,                       -- 所属记录ID
    field_id UUID NOT NULL,                        -- 字段定义ID（外键）
    field_value TEXT,                              -- 字段值

    -- 审计字段
    created_time TIMESTAMP NOT NULL,
    modified_time TIMESTAMP NOT NULL,

    -- 约束
    CONSTRAINT business_key_ledger_record_value UNIQUE(record_id, field_id),
    CONSTRAINT fk_ledger_record_value_record FOREIGN KEY(record_id) REFERENCES ledger_record(id) ON DELETE CASCADE,
    CONSTRAINT fk_ledger_record_value_field FOREIGN KEY(field_id) REFERENCES ledger_template_field(id)
);
```

#### 新结构
```sql
CREATE TABLE ledger_record_value(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    record_id UUID NOT NULL,                       -- 所属记录ID
    field_definition JSONB NOT NULL,               -- 字段定义（JSON格式）
    field_value TEXT,                              -- 字段值

    -- 审计字段
    created_time TIMESTAMP NOT NULL,
    modified_time TIMESTAMP NOT NULL,

    -- 约束
    CONSTRAINT fk_ledger_record_value_record FOREIGN KEY(record_id) REFERENCES ledger_record(id) ON DELETE CASCADE
);
```

### 2. 新增索引

```sql
-- 为 JSON 字段创建 GIN 索引（支持高效的 JSON 查询）
CREATE INDEX idx_ledger_record_value_field_definition ON ledger_record_value USING GIN (field_definition);

-- 为常用的 JSON 字段路径创建表达式索引（用于查询特定字段名）
CREATE INDEX idx_ledger_record_value_field_name ON ledger_record_value USING BTREE ((field_definition->>'fieldName'));
```

### 3. 数据插入修改

#### 原插入语句
```sql
INSERT INTO ledger_record_value(record_id, field_id, field_value, created_time, modified_time)
SELECT
    record_uuid,
    field_id,  -- 直接引用字段ID
    field_value,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM ledger_template_field
WHERE ...;
```

#### 新插入语句
```sql
INSERT INTO ledger_record_value(record_id, field_definition, field_value, created_time, modified_time)
SELECT
    record_uuid,
    jsonb_build_object(
        'fieldName', field_name,
        'fieldLabel', field_label,
        'fieldType', field_type::text,
        'fieldOptions', CASE
            WHEN field_type = 'SELECT' AND field_options IS NOT NULL THEN field_options::jsonb
            WHEN field_type = 'MULTISELECT' AND field_options IS NOT NULL THEN field_options::jsonb
            ELSE NULL
        END,
        'required', required,
        'minLength', min_length,
        'maxLength', max_length,
        'placeholder', placeholder,
        'helpText', help_text,
        'sortOrder', sort_order,
        'width', width,
        'visible', visible,
        'editable', editable,
        'searchable', searchable
    ),
    field_value,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM ledger_template_field
WHERE ...;
```

## 主要变化

1. **移除外键关联**：不再引用 `ledger_template_field` 表，避免 Account 权限冲突
2. **使用 JSONB 存储**：将字段定义完整信息存储为 JSON
3. **新增 GIN 索引**：支持高效的 JSON 查询
4. **新增表达式索引**：针对常用查询路径优化

## JSON 结构示例

```json
{
    "fieldName": "inspection_date",
    "fieldLabel": "检查日期",
    "fieldType": "DATE",
    "fieldOptions": null,
    "required": true,
    "minLength": null,
    "maxLength": null,
    "placeholder": "请选择检查日期",
    "helpText": "选择实际进行检查的日期",
    "sortOrder": 1,
    "width": "200px",
    "visible": true,
    "editable": true,
    "searchable": false
}
```

## 查询示例

### 查找特定字段名的记录
```sql
SELECT * FROM ledger_record_value
WHERE field_definition->>'fieldName' = 'inspection_date';
```

### 查找必填字段的记录
```sql
SELECT * FROM ledger_record_value
WHERE (field_definition->>'required')::boolean = true;
```

### 按字段类型查询
```sql
SELECT * FROM ledger_record_value
WHERE field_definition->>'fieldType' = 'SELECT';
```

## 优势

1. **避免复杂关联**：不再需要多表 JOIN
2. **历史快照**：即使模板修改，历史记录保持不变
3. **灵活查询**：支持基于 JSON 内容的各种查询
4. **性能优化**：通过 GIN 索引实现高效 JSON 查询