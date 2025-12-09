# 数据模型与ORM

## 简介

本文档详细介绍 ainote-server 项目中的数据模型设计和 Jimmer ORM 的使用方法。涵盖实体定义、关系映射、Jimmer ORM 的核心特性、数据库配置以及高级功能的使用。

## 核心实体模型

### 基础实体类

#### BaseEntity

所有业务实体的基类，提供审计字段支持：

```kotlin
@MappedSuperclass
interface BaseEntity {
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdTime: LocalDateTime

    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val modifiedTime: LocalDateTime
}
```

特性：
- 自动管理 `createdTime` 和 `modifiedTime` 字段
- 通过 `BaseEntityDraftInterceptor` 自动填充时间戳
- 支持JSON格式化输出

#### TenantAware

多租户支持的接口：

```kotlin
interface TenantAware {
    val tenant: String
}
```

实现此接口的实体将自动具备租户隔离功能。

### 主要业务实体

#### Account（账户实体）

用户账户的核心实体，包含认证信息：

```kotlin
@Entity
@Table(name = "account")
@KeyUniqueConstraint(
    noMoreUniqueConstraints = true,
    isNullNotDistinct = true
)
interface Account : BaseEntity {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    val username: String
    val password: String
    val email: String?
    val phone: String?
    @Default(value = "1")
    val status: UserStatus
    @Default(value = "USER")
    val role: RoleEnum

    @OneToOne()
    @JoinColumn(name = "avatar_id")
    val avatar: StaticFile?

    @OneToMany(mappedBy = "account")
    val accountCompanies: List<AccountCompanyEntity>

    @ManyToManyView(
        prop = "accountCompanies",
        deeperProp = "company"
    )
    val companies: List<Company>
}
```

#### Company（公司实体）

组织/公司信息实体：

```kotlin
@Entity
@Table(name = "company")
interface Company : BaseEntity {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    @Key
    val name: String

    val code: String?
    val address: String?
    val description: String?

    @OneToMany(mappedBy = "company")
    val accountCompanies: List<AccountCompanyEntity>
}
```

#### Note（笔记实体）

笔记功能的核心实体：

```kotlin
@Entity
@Table(name = "note")
interface Note : BaseEntity {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    @Key
    val title: String

    @Column(columnDefinition = "TEXT")
    val content: String

    @ManyToOne
    val author: Account
}
```

#### AccountCompanyEntity（账户公司关联实体）

账户与公司的多对多关联实体：

```kotlin
@Entity
@Table(name = "account_company")
interface AccountCompanyEntity : PassiveEntity {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    @ManyToOne
    @JoinColumn(name = "account_id")
    val account: Account

    @ManyToOne
    @JoinColumn(name = "company_id")
    val company: Company

    @Key
    @Default("MEMBER")
    val role: String
}
```

#### LedgerTemplate（台账模板）

台账模板系统核心实体，支持多租户：

```kotlin
@Entity
@Table(name = "ledger_template")
@KeyUniqueConstraint(
    noMoreUniqueConstraints = true,
    isNullNotDistinct = true
)
interface LedgerTemplate : PassiveEntity, BaseEntity, TenantAware {

    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    @Key
    val name: String
    val description: String?
    val category: String?

    @Default("1")
    val version: Int

    @Default("true")
    val enabled: Boolean

    val icon: String?

    @OneToOne()
    @JoinColumn(name = "file_id")
    val file: StaticFile?

    val color: String?
    @Default("0")
    val sortOrder: Int

    @OneToMany(mappedBy = "template")
    val fields: List<LedgerTemplateField>

    @OneToMany(mappedBy = "template")
    val records: List<LedgerRecord>
}
```

## 实体关系映射

### 一对一关系

```kotlin
@Entity
interface User : BaseEntity {
    @Id
    val id: Long

    @OneToOne
    @JoinColumn(name = "profile_id")
    val profile: UserProfile?
}
```

### 一对多关系

```kotlin
@Entity
interface Company : BaseEntity {
    @Id
    val id: Long

    @OneToMany(mappedBy = "company")
    val employees: List<Employee>
}

@Entity
interface Employee : BaseEntity {
    @Id
    val id: Long

    @ManyToOne
    @JoinColumn(name = "company_id")
    val company: Company
}
```

### 多对多关系

```kotlin
@Entity
interface Account : BaseEntity {
    @Id
    val id: Long

    @ManyToMany
    @JoinTable(
        name = "account_company",
        joinColumns = [JoinColumn(name = "account_id")],
        inverseJoinColumns = [JoinColumn(name = "company_id")]
    )
    val companies: List<Company>
}
```

## Jimmer ORM 核心特性

### 类型安全查询 DSL

Jimmer 提供完全类型安全的查询构建器：

```kotlin
// 简单查询
val users = sqlClient.createQuery(User) {
    where(table.name eq "John")
    select(table.fetch {
        id()
        name()
        email()
    })
}.execute()

// 关联查询
val usersWithOrders = sqlClient.createQuery(User) {
    select(table.fetch {
        name()
        orders {
            orderDate()
            totalAmount()
        }
    })
}.execute()
```

### Object Fetcher

灵活的对象图获取，避免 N+1 查询：

```kotlin
val users = sqlClient.findForList(
    User::class,
    Fetcher.of(User::class).by {
        allScalarFields()
        orders {
            allScalarFields()
            items {
                productName()
                quantity()
            }
        }
    }
)
```

### Smart Save

智能保存，自动检测 INSERT/UPDATE：

```kotlin
// 自动判断是新增还是更新
sqlClient.save(userDraft)

// 批量保存
sqlClient.saveAll(listOf(user1, user2))
```

### DTO 语言支持

从 `.dto` 文件自动生成类型安全的 DTO：

```
dto UserView {
    id
    name
    email
    orders {
        id
        orderDate
        totalAmount
    }
}
```

生成的 DTO 可以直接用于 API 响应：

```kotlin
@GetMapping("/users")
fun getUsers(): List<UserView> {
    return sqlClient.createQuery(User) {
        select(table.fetch(UserView::class))
    }.execute()
}
```

## 数据库配置与初始化

### 数据库连接配置

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/ainote
    username: postgres
    password: your_password

jimmer:
  database-validation-mode: ERROR
  show-sql: true
  pretty-sql: true
```

### 数据库初始化

项目支持通过 SQL 脚本初始化数据库结构：

```sql
-- database/init.sql
CREATE TABLE IF NOT EXISTS account (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    tenant VARCHAR(50) NOT NULL,
    created_time TIMESTAMP NOT NULL,
    modified_time TIMESTAMP NOT NULL
);
```

## 拦截器与自动化

### BaseEntityDraftInterceptor

自动填充审计字段：

```kotlin
@Component
class BaseEntityDraftInterceptor : DraftInterceptor<BaseEntity, BaseEntityDraft> {
    override fun beforeSave(draft: BaseEntityDraft, original: BaseEntity?) {
        val now = LocalDateTime.now()
        draft.createdTime = now
        draft.modifiedTime = now
    }
}
```

### TenantAwareDraftInterceptor

自动填充租户信息：

```kotlin
@Component
class TenantAwareDraftInterceptor : DraftInterceptor<TenantAware, TenantAwareDraft> {
    override fun beforeSave(draft: TenantAwareDraft, original: TenantAware?) {
        draft.tenant = TenantProvider.getCurrentTenant()
    }
}
```

## 缓存机制

### 多层缓存配置

```kotlin
@Configuration
class CacheConfig {

    @Bean
    fun cacheFactory(redissonClient: RedissonClient): CacheFactory<*> {
        return CacheFactory.create(redissonClient) {
            withRemoteDuration(Duration.ofHours(1))  // 远程缓存1小时
            withLocalCache(100, Duration.ofMinutes(5))  // 本地缓存100条，5分钟过期
            withSoftLock()  // 软锁防止缓存击穿
            withTracking()  // 缓存跟踪
        }
    }
}
```

### 实体缓存

```kotlin
@Entity
@Cache(direct = true)  // 启用直接缓存
interface User : BaseEntity {
    // ... 实体定义
}
```

## 枚举类型支持

### 定义枚举

```kotlin
enum class UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
```

### 在实体中使用

```kotlin
@Entity
interface Account : BaseEntity {
    @Enumerated(EnumType.STRING)
    val status: UserStatus
}
```

## 计算属性

### Formula 属性

```kotlin
@Entity
interface User : BaseEntity {
    @Column(name = "first_name")
    val firstName: String

    @Column(name = "last_name")
    val lastName: String

    @Formula("CONCAT(first_name, ' ', last_name)")
    val fullName: String
}
```

### Transient 属性

```kotlin
@Entity
interface User : BaseEntity {
    val birthDate: LocalDate

    @Transient
    val age: Int
        get() = ChronoUnit.YEARS.between(birthDate, LocalDate.now()).toInt()
}
```

## 最佳实践

### 实体设计原则

1. **优先使用接口**：Jimmer 推荐使用接口定义实体
2. **合理使用继承**：通过基类减少重复代码
3. **明确关系映射**：正确定义一对一、一对多、多对多关系
4. **使用合适的数据类型**：根据业务需求选择字段类型

### 查询优化

1. **避免 N+1 查询**：使用 Object Fetcher 精确控制加载的数据
2. **合理使用缓存**：为频繁访问的数据配置缓存
3. **分页查询**：大数据量查询使用分页机制

### 性能优化

1. **批量操作**：使用 `saveAll` 等批量方法
2. **延迟加载**：合理使用延迟加载减少不必要的数据传输
3. **索引优化**：为常用查询字段创建数据库索引

通过合理使用 Jimmer ORM 的这些特性，可以构建出高性能、类型安全、易于维护的数据访问层。