# Ainote Server

基于 Kotlin + Jimmer ORM 构建的现代化 REST API 服务器。

## 技术栈

- **语言**: Kotlin
- **框架**: Spring Boot 3.5.6
- **ORM**: Jimmer 0.9.117
- **构建工具**: Gradle + KSP (Kotlin Symbol Processing)
- **数据库**: PostgreSQL (默认), H2, MySQL (可配置)
- **缓存**: Redis (可选)
- **文件存储**: Aliyun OSS (可配置)
- **认证**: JWT (Bearer Token)

## 项目结构

项目采用多模块架构，职责清晰分离：

```
ainote-server/
├── model/          # 实体定义和公共基类
├── repository/     # 数据访问层 (Spring Data 风格仓储)
├── runtime/        # 运行时配置 (过滤器、拦截器、解析器、缓存)
└── service/        # 服务层和 REST 控制器
```

### 模块依赖关系

```
service → repository → model
service → runtime → model
runtime → repository → model
```

## 快速开始

### 环境要求

- JDK 17 或更高版本
- Gradle 8.x (或使用项目自带的 wrapper)

### 首次设置

首次在 IntelliJ IDEA 中打开此项目时，部分生成代码可能尚不存在。您有两种选择：

1. **先构建，再打开**：
   ```bash
   ./gradlew build
   # 然后在 IntelliJ IDEA 中打开项目
   ```

2. **直接打开**：
   - 在 IntelliJ IDEA 中打开项目
   - 暂时忽略 IDE 报错
   - 等待依赖下载完成
   - 直接运行 main 方法 - 所有错误将自动消失

### 运行应用

#### 使用 Gradle

```bash
./gradlew :service:bootRun
```

#### 使用 IDE

在 IDE 中直接运行 `service/src/main/kotlin/top/zztech/ainote/App.kt` 中的 `main` 函数。

应用将启动在 `http://localhost:8080`

### API 文档

应用启动后，可访问：

- **Swagger UI**: http://localhost:8080/openapi.html
- **OpenAPI 规范**: http://localhost:8080/openapi.yml
- **TypeScript 客户端**: http://localhost:8080/ts.zip

**认证**: API 请求需要在 Header 中添加 JWT Token:
```bash
curl -H "Authorization: Bearer {token}" http://localhost:8080/api/endpoint
```

## 核心特性

### Jimmer 框架特性

1. **Object Fetcher** - 查询任意形状的数据
2. **Smart Save** - 保存任意形状的数据，自动检测 INSERT/UPDATE
3. **类型安全 DSL** - 编译时类型检查的查询语句
4. **DTO 语言** - 从 `.dto` 文件自动生成 DTO
5. **多租户支持** - 内置租户隔离
6. **多层缓存** - Redis 多层缓存支持
7. **计算属性** - Formula 和 Transient 属性
8. **全局过滤器** - 自动查询过滤
9. **Draft 拦截器** - 自动填充审计字段

### 项目功能

- ✅ 多模块架构
- ✅ 基础实体，包含审计字段 (createdTime, modifiedTime)
- ✅ 多租户支持 (可选)
- ✅ 自动时间戳管理
- ✅ CORS 跨域配置
- ✅ H2 内存数据库用于开发
- ✅ OpenAPI/Swagger 文档自动生成
- ✅ TypeScript 客户端自动生成

## 开发指南

### 添加新实体

1. 在 `model/src/main/kotlin/top/zztech/ainote/model/` 创建实体接口：
   ```kotlin
   @Entity
   interface YourEntity : BaseEntity {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       val id: Long
       
       @Key
       val name: String
   }
   ```

2. 在 `repository/src/main/kotlin/top/zztech/ainote/repository/` 创建仓储：
   ```kotlin
   @Repository
   class YourEntityRepository(sql: KSqlClient) 
       : AbstractKotlinRepository<YourEntity, Long>(sql)
   ```

3. 在 `service/src/main/kotlin/top/zztech/ainote/service/` 创建服务：
   ```kotlin
   @RestController
   @RequestMapping("/your-entity")
   class YourEntityService(
       private val repository: YourEntityRepository
   )
   ```

4. 重新构建项目以生成 KSP 代码：
   ```bash
   ./gradlew build
   ```

### 多租户使用

在 HTTP 请求中添加 `tenant` 头实现租户隔离：

```bash
curl -H "tenant: a" http://localhost:8080/note
```

可用租户: `a`, `b` (可配置)

## 数据库配置

### PostgreSQL (默认)

默认配置使用 PostgreSQL，环境变量配置：

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=ainote
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

或直接修改 `application.yml`：
```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/ainote
    username: postgres
    password: your_password
```

### MySQL

修改 `application.yml`:
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ainote
    username: root
    password: your_password
```

### H2 (内存数据库)

修改 `application.yml`:
```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    username: sa
    password:
```

## 缓存配置 (可选)

启用 Redis 缓存：

```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=  # 如果有密码
```

或修改 `application.yml`:
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:  # 如果需要
```

重启应用后生效。

## 生产构建

```bash
./gradlew clean build
```

可执行 JAR 将生成在 `service/build/libs/`

## 生产运行

### Docker 运行

```bash
java -jar service/build/libs/service-1.0.0.jar \
  -Dspring.profiles.active=prod \
  -DDB_HOST=your-db-host \
  -DDB_PORT=5432 \
  -DDB_NAME=ainote \
  -DDB_USERNAME=postgres \
  -DDB_PASSWORD=your_password
```

### 环境变量配置

关键环境变量：
- `SPRING_PROFILES_ACTIVE`: 应用配置文件 (默认: dev)
- `SERVER_PORT`: 服务器端口 (默认: 8080)
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`: 数据库配置
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`: Redis 缓存配置
- `ALIYUN_OSS_ENDPOINT`, `ALIYUN_ACCESS_KEY_ID`, `ALIYUN_ACCESS_KEY_SECRET`, `ALIYUN_OSS_BUCKET`: Aliyun OSS 配置

## 测试

```bash
./gradlew test
```

## 项目实体说明

### 已实现的主要实体

- **Account**: 用户账户实体，包含认证信息
- **Company**: 公司/组织实体
- **AccountCompanyEntity**: 账户与公司的关联实体
- **Note**: 笔记实体
- **StaticFile**: 静态文件实体
- **Log**: 操作日志实体

## 扩展和自定义

1. **添加新实体**: 参考 `model/` 中的示例实体结构
2. **禁用多租户**: 移除 `TenantAware` 接口和相关过滤器
3. **自定义缓存**: 修改 `runtime/src/main/kotlin/top/zztech/ainote/runtime/cache/CacheConfig.kt`
4. **添加自定义拦截器**: 在 `runtime/src/main/kotlin/top/zztech/ainote/runtime/interceptor/` 中创建新类
5. **修改 OSS 配置**: 编辑 `service/src/main/kotlin/top/zztech/ainote/cfg/OssConfig.kt`

## 核心模块说明

### model
实体定义和基础类，包含公共审计字段、多租户支持、枚举定义等。

### repository
数据访问层，使用 Jimmer 的 KRepository 接口实现 Spring Data 风格的仓储。

### runtime
运行时配置和增强，包括：
- 过滤器和拦截器 (自动填充审计字段、多租户隔离)
- JWT 认证和授权
- Redis 缓存配置
- 安全工具类和 DTO 定义

### service
业务逻辑层和 REST 控制器，提供 API 端点和业务服务。

## 参考资源

- [Jimmer 官方文档](https://babyfish-ct.github.io/jimmer-doc/)
- [Spring Boot 文档](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Kotlin 文档](https://kotlinlang.org/docs/home.html)
- [PostgreSQL 文档](https://www.postgresql.org/docs/)

## 许可证

MIT License

## 作者

zztech


## todolist
1. 笔记的crud，存储原始的资料
2. springAi的接入
3. qwen 的接入，模版的分解。
4. 按照模版分解原始笔记，生成表单
5. ？手机号注册和接入