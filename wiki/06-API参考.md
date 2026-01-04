# API参考

## 简介

本文档提供 ainote-server 项目中所有 REST API 的详细参考。包括认证、用户管理、笔记管理、公司管理、文件存储、日志记录、台账管理等核心功能的接口说明、请求参数、响应格式以及使用示例。

## 认证机制

### JWT Token 认证

所有 API 请求（除了登录和注册）都需要在请求头中包含 JWT Token：

```
Authorization: Bearer {token}
```

### 获取 Token

通过登录接口获取 JWT Token：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

响应示例：
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com"
  }
}
```

## 认证 API

### 用户注册

**POST** `/auth/register`

使用手机号和短信验证码注册新用户账户。

**请求体：**
```json
{
  "username": "newuser",
  "password": "password123",
  "phone": "13800138000",
  "code": "123456",
  "scene": "REGISTER"
}
```

**请求参数：**
- `username`: 用户名（必填）
- `password`: 密码（必填）
- `phone`: 手机号（必填）
- `code`: 短信验证码（必填）
- `scene`: 验证码场景（必填，如 "REGISTER"）

**响应：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER",
  "tenant": "default"
}
```

### 用户登录

**POST** `/auth/login`

使用用户名和密码登录，需要图形验证码。

**请求体：**
```json
{
  "username": "admin",
  "password": "password",
  "verCode": "abcd",
  "verKey": "uuid-key"
}
```

**请求参数：**
- `username`: 用户名（必填）
- `password`: 密码（必填）
- `verCode`: 图形验证码（必填）
- `verKey`: 验证码密钥（必填）

**响应：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "ADMIN",
  "tenant": "default"
}
```

### 短信验证码登录

**POST** `/auth/sms/login`

使用手机号和短信验证码登录。

**请求体：**
```json
{
  "phone": "13800138000",
  "code": "123456",
  "scene": "LOGIN"
}
```

**请求参数：**
- `phone`: 手机号（必填）
- `code`: 短信验证码（必填）
- `scene`: 验证码场景（必填）

**响应：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER",
  "tenant": "123456"
}
```

### 发送短信验证码

**POST** `/auth/sms/send`

发送短信验证码到指定手机号。

**请求体：**
```json
{
  "phone": "13800138000",
  "scene": "REGISTER"
}
```

**请求参数：**
- `phone`: 手机号（必填）
- `scene`: 验证码场景（必填，如 "REGISTER"、"LOGIN"）

**响应：**
```json
{
  "success": true,
  "message": "验证码已发送",
  "ttl": 300
}
```

### 获取图形验证码

**GET** `/auth/captcha`

获取图形验证码，用于登录/注册时的安全验证。

**响应：**
```json
{
  "key": "550e8400-e29b-41d4-a716-446655440000",
  "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
}
```

**响应参数：**
- `key`: 验证码密钥，用于验证时提交
- `image`: Base64 编码的验证码图片

### 验证图形验证码

**POST** `/auth/captcha/verify`

验证图形验证码是否正确。

**请求体：**
```json
{
  "verCode": "abcd",
  "verKey": "550e8400-e29b-41d4-a716-446655440000"
}
```

**响应：**
```json
{
  "ok": true
}
```

## 用户管理 API

### 获取个人信息

**GET** `/account/me`

获取当前用户的个人信息。

**请求头：**
```
Authorization: Bearer {token}
```

**响应：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "username": "admin",
  "phone": "13800138000",
  "role": "ADMIN",
  "companies": [
    {
      "name": "示例公司"
    }
  ]
}
```

### 更新个人信息

**PUT** `/account/update`

更新当前用户的个人信息。

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**
```json
{
  "email": "newemail@example.com",
  "phone": "13900139000"
}
```

### 加入公司

**POST** `/account/joinCompany`

当前用户加入指定公司。

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**
```json
{
  "companyId": "550e8400-e29b-41d4-a716-446655440002",
  "role": "MEMBER"
}
```

## 笔记 API

### 创建笔记

**POST** `/note/add`

创建新的笔记。

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**
```json
{
  "title": "新笔记标题",
  "content": "笔记内容..."
}
```

**响应：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440003"
}
```

## 模板 API（台账模板）

### 创建模板

**POST** `/template/add`

创建新的台账模板。

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
tenant: {tenant_id}
```

**请求体：**
```json
{
  "name": "安全生产巡查台账",
  "description": "用于记录巡查情况",
  "category": "safety",
  "iconFileId": "550e8400-e29b-41d4-a716-446655440010",
  "fileId": "550e8400-e29b-41d4-a716-446655440011",
  "sortOrder": 0,
  "color": "#1677ff"
}
```

**响应：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440012"
}
```

### 获取我可见的模板分页

**GET** `/template/myTemplatePage`

按当前用户维度筛选可见模板（通常用于“我的模板”列表）。

**查询参数：**
- `pageIndex`: 页码（默认：0）
- `pageSize`: 每页大小（默认：10）
- `sort`: 排序规则（默认："createdTime desc"）
- `keyword`: 关键词（匹配 name/description，来自 `SearchTemplate`）
- `category`: 分类（来自 `SearchTemplate`）
- `enabled`: 启用状态（来自 `SearchTemplate`）

**响应：**
返回 `Page<LedgerTemplate>`，字段由 `LIST_TEMPLATE` Fetcher 控制（如：name/category/description/enabled/icon）。

### 获取租户下全部模板分页

**GET** `/template/tenantTemplatePage`

查询当前租户下的模板列表（不按用户筛选）。

**查询参数：**同 `myTemplatePage`

**响应：**
返回 `Page<LedgerTemplate>`，字段由 `LIST_TEMPLATE` Fetcher 控制。

### 获取模板详情

**GET** `/template/{id}`

获取单个模板详情。

**路径参数：**
- `id`: 模板 ID

**响应：**
返回 `LedgerTemplate`，字段由 `SIMPLE_TEMPLATE` Fetcher 控制（包含：icon/file/fields/color 等）。

### 修改模板启用状态

**PUT** `/template/changeStatus`

启用/禁用模板。

**请求体：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440012",
  "enabled": true
}
```

### 删除模板

**DELETE** `/template/{id}`

删除模板。

**路径参数：**
- `id`: 模板 ID

## 文件存储 API

### 上传文件

**POST** `/file/add`

上传文件到阿里云 OSS。

**请求头：**
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**请求体：**
```
file: [文件数据]
```

**响应：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440004"
}
```

### 获取文件信息

**GET** `/file/find/{id}`

获取文件信息。

**路径参数：**
- `id`: 文件 ID

**请求头：**
```
Authorization: Bearer {token}
```

**响应：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440004",
  "fileName": "document.pdf",
  "originalName": "我的文档.pdf",
  "fileSize": 1024000,
  "fileType": "PDF",
  "uploadedTime": "2024-01-01 14:00:00"
}
```

## 公司管理 API

### 获取公司列表

**GET** `/company/page`

分页获取公司列表（管理员权限）。

**查询参数：**
- `pageIndex`: 页码（默认：0）
- `pageSize`: 每页大小（默认：5）
- `sortCode`: 排序规则（默认："name asc, createdTime desc"）

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**
```json
{
  "name": "公司名称",
  "code": "公司编码"
}
```

**响应：**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440005",
      "name": "示例公司",
      "code": "DEMO001",
      "address": "公司地址",
      "tenant": "123456"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### 添加公司

**POST** `/company/add`

管理员添加新公司，系统自动生成租户标识。

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**
```json
{
  "name": "新公司",
  "code": "NEW001",
  "address": "公司地址",
  "contact": "联系人",
  "status": "ACTIVE"
}
```

**响应：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440006"
}
```

### 删除公司

**POST** `/company/delete`

管理员删除公司。

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440005"
}
```

### 获取我的公司

**GET** `/company/my`

获取当前用户加入的公司列表。

**请求头：**
```
Authorization: Bearer {token}
```

**响应：**
```json
[
  {
    "name": "示例公司"
  }
]
```

### 获取公司名称列表

**GET** `/company/names`

获取所有公司名称列表（用于下拉选择等场景）。

**请求头：**
```
Authorization: Bearer {token}
```

**响应：**
```json
[
  {
    "name": "示例公司"
  },
  {
    "name": "测试公司"
  }
]
```

### 获取公司成员

**GET** `/company/members`

获取指定公司的所有成员信息。

**查询参数：**
- `companyId`: 公司 ID（必填）

**请求头：**
```
Authorization: Bearer {token}
```

**响应：**
```json
[
  {
    "role": "ADMIN",
    "account": {
      "username": "admin",
      "avatar": {
        "filePath": "https://...",
        "fileName": "avatar.jpg"
      }
    }
  }
]
```

### 设置公司管理员

**POST** `/company/setAdmin`

设置用户为公司的管理员（仅管理员可用）。

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**
```json
{
  "companyId": "550e8400-e29b-41d4-a716-446655440005",
  "account": "550e8400-e29b-41d4-a716-446655440001"
}
```

**响应：**
```json
"550e8400-e29b-41d4-a716-446655440001"
```

## AI 服务 API

### AI 对话

**POST** `/ai/chat`

使用 AI 模型进行智能对话。

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**
```json
{
  "prompt": "你好，请帮我写一段 Python 代码"
}
```

**响应：**
```json
{
  "result": "当然可以！以下是一段 Python 代码示例：..."
}
```

### AI 生成台账模板字段

**POST** `/ai/template-fields`

使用 AI 根据模板信息和文档自动生成台账模板字段定义。

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**
```json
{
  "name": "安全生产巡查台账",
  "description": "用于记录日常安全生产巡查情况",
  "category": "safety",
  "fileId": "550e8400-e29b-41d4-a716-446655440010"
}
```

**请求参数：**
- `name`: 模板名称（必填）
- `description`: 模板描述（可选）
- `category`: 模板分类（可选）
- `fileId`: 参考文档文件 ID（可选，如果提供则基于文档内容生成字段）

**响应：**
```json
[
  {
    "fieldName": "inspection_date",
    "fieldLabel": "巡查日期",
    "fieldType": "DATE",
    "required": true,
    "sortOrder": 0
  },
  {
    "fieldName": "inspector",
    "fieldLabel": "巡查人员",
    "fieldType": "TEXT",
    "required": true,
    "sortOrder": 1
  },
  {
    "fieldName": "location",
    "fieldLabel": "巡查地点",
    "fieldType": "TEXT",
    "required": true,
    "sortOrder": 2
  },
  {
    "fieldName": "issues_found",
    "fieldLabel": "发现问题",
    "fieldType": "TEXTAREA",
    "required": false,
    "sortOrder": 3
  },
  {
    "fieldName": "status",
    "fieldLabel": "状态",
    "fieldType": "SELECT",
    "fieldOptions": "[\"正常\",\"异常\",\"已整改\"]",
    "required": true,
    "sortOrder": 4
  }
]
```

## 日志 API

### 获取操作日志

**GET** `/log/page`

分页获取操作日志。

**查询参数：**
- `pageIndex`: 页码（默认：0）
- `pageSize`: 每页大小（默认：20）

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**
```json
{
  "action": "LOGIN",
  "startTime": "2024-01-01T00:00:00",
  "endTime": "2024-01-31T23:59:59"
}
```

## 错误响应格式

所有 API 在发生错误时都会返回统一的错误格式：

```json
{
  "timestamp": "2024-01-01T10:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "请求参数错误",
  "path": "/api/notes"
}
```

### 常见错误码

| 状态码 | 说明 | 示例场景 |
|--------|------|----------|
| 400 | 请求参数错误 | 缺少必需参数、参数格式错误 |
| 401 | 未授权 | 缺少 Token、Token 无效 |
| 403 | 禁止访问 | 权限不足 |
| 404 | 资源不存在 | 笔记 ID 不存在 |
| 409 | 资源冲突 | 用户名已存在 |
| 500 | 服务器内部错误 | 数据库连接失败 |

### 模板模块错误码（`TemplateError`）

模板（台账模板）相关业务异常会使用 `TemplateError` 表达错误语义，常见枚举值：

- **`TEMPLATE_NOT_FOUND`**
- **`INVALID_TEMPLATE_ID`**
- **`TEMPLATE_NAME_EMPTY`**
- **`TEMPLATE_ALREADY_EXISTS`**
- **`TEMPLATE_DISABLED`**
- **`TEMPLATE_CREATE_FAILED`**
- **`TEMPLATE_UPDATE_FAILED`**
- **`TEMPLATE_DELETE_FAILED`**

## 多租户支持

所有需要租户隔离的 API 都需要包含 `tenant` 请求头：

```
tenant: {tenant_id}
```

支持的操作：
- `a`: 租户 A
- `b`: 租户 B
- 可扩展其他租户

## 分页参数

支持分页的 API 使用统一的分页参数：

- `page`: 页码，从 0 开始
- `size`: 每页大小，默认 20
- `sort`: 排序字段，支持多字段排序

示例：
```
GET /api/notes?page=0&size=10&sort=createdTime,desc
```

## 搜索功能

支持搜索的 API 使用 `search` 参数：

```
GET /api/notes?search=关键词
```

搜索范围通常包括主要文本字段，如标题、内容等。

## API 版本控制

当前 API 版本为 v1，所有路径都以 `/api` 开头。未来版本可能通过路径前缀进行版本控制：
- `/api/v1/...` - 当前版本
- `/api/v2/...` - 未来版本

## 使用示例

### 完整的笔记操作流程

```bash
# 1. 用户登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'

# 2. 创建笔记（使用上一步获取的 token）
curl -X POST http://localhost:8080/api/notes \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "tenant: a" \
  -H "Content-Type: application/json" \
  -d '{"title": "我的笔记", "content": "笔记内容"}'

# 3. 获取笔记列表
curl -X GET "http://localhost:8080/api/notes?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "tenant: a"
```

通过以上 API 参考，开发者可以快速集成 ainote-server 的各种功能到自己的应用中。