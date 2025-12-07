# Ainote Server

基于 Kotlin + Jimmer ORM 构建的现代化 REST API 服务器。

## 技术栈

- **语言**: Kotlin 2.1.20
- **框架**: Spring Boot 3.5.6
- **ORM**: Jimmer 0.9.117
- **构建工具**: Gradle + KSP (Kotlin Symbol Processing)
- **数据库**: H2 (默认), MySQL, PostgreSQL (可配置)
- **缓存**: Redis + Redisson (可选)

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

```bash
./gradlew :service:bootRun
```

或在 IDE 中直接运行 `service/src/main/kotlin/top/zztech/ainote/App.kt` 中的 `main` 函数。

应用将启动在 `http://localhost:8080`

### API 文档

应用启动后，可访问：

- **Swagger UI**: http://localhost:8080/openapi.html
- **OpenAPI 规范**: http://localhost:8080/openapi.yml
- **TypeScript 客户端**: http://localhost:8080/ts.zip

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

### H2 (默认 - 内存数据库)

无需配置。应用启动时自动初始化数据库。

### MySQL

更新 `application.yml`:
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ainote
    username: root
    password: your_password
```

### PostgreSQL

更新 `application.yml`:
```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/ainote
    username: postgres
    password: your_password
```

## 缓存配置 (可选)

启用 Redis 缓存：

1. 安装并启动 Redis 服务器
2. 更新 `application.yml`:
   ```yaml
   spring:
     redis:
       host: localhost
       port: 6379
   ```
3. 重启应用

## 生产构建

```bash
./gradlew clean build
```

可执行 JAR 将生成在 `service/build/libs/`

## 生产运行

```bash
java -jar service/build/libs/service-1.0.0.jar
```

## 测试

```bash
./gradlew test
```

## 自定义配置

本项目可作为模板使用。您可以：

1. **删除示例实体**: 删除 `Note.kt`, `NoteRepository.kt`, `NoteService.kt`
2. **添加自己的实体**: 参考示例文件的结构
3. **禁用多租户**: 移除 `TenantAware` 接口和相关过滤器
4. **自定义缓存**: 修改 `runtime/cache/CacheConfig.kt`
5. **添加自定义拦截器**: 在 `runtime/interceptor/` 中创建新类

## 参考资源

- [Jimmer 官方文档](https://babyfish-ct.github.io/jimmer-doc/)
- [Spring Boot 文档](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Kotlin 文档](https://kotlinlang.org/docs/home.html)

## 许可证

MIT License

## 作者

zztech
