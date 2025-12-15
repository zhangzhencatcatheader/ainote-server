# Jimmer JSON 字段使用经验

## 问题描述

在实现台账系统时，遇到了以下问题：
- `LedgerRecordValue` 需要记录对应哪个字段定义 (`LedgerTemplateField`)
- 如果直接使用 `@ManyToOne` 关联，会导致 Account 权限冲突
  - `LedgerRecord` 属于某个 Account
  - `LedgerTemplateField` 属于某个 `LedgerTemplate`，`LedgerTemplate` 也属于 Account
  - 直接关联会产生复杂的权限和多租户问题

## 解决方案

使用 Jimmer 内置的 JSON 支持，通过 `@Serialized` 注解将复杂对象存储为 JSON。

### 1. 创建 DTO 类

```kotlin
package top.zztech.ainote.model.dto

import org.babyfish.jimmer.sql.Serialized
import top.zztech.ainote.model.enums.FieldType
import java.math.BigDecimal

@Serialized  // 标记为可序列化的 JSON 对象
data class FieldDefinitionDto(
    val fieldName: String,
    val fieldLabel: String,
    val fieldType: FieldType,
    val fieldOptions: List<String>? = null,
    val required: Boolean = false,
    // ... 其他字段
)
```

### 2. 在实体中使用 JSON 字段

```kotlin
@Entity
@Table(name = "ledger_record_value")
interface LedgerRecordValue : BaseEntity {

    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    @Key
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val record: LedgerRecord

    // 直接使用 DTO 类型，Jimmer 会自动处理 JSON 序列化
    val fieldDefinition: FieldDefinitionDto

    val fieldValue: String?
}
```

## 关键要点

### 1. @Serialized 注解的两种使用方式

Jimmer 提供 `org.babyfish.jimmer.sql.Serialized`，有两种使用 JSON 映射的方式：

#### 方式一：全局 JSON 映射
- 在自定义类上使用 `@Serialized` 注解
- 这样，任何实体中相同类型的属性都会成为 JSON 属性

```kotlin
@Serialized  // 类级别注解
data class FieldDefinitionDto(
    val fieldName: String,
    val fieldLabel: String,
    val fieldType: FieldType,
    ...
)
```

#### 方式二：属性级 JSON 映射
- 直接在实体属性上使用 `@Serialized` 注解
- 无论属性是什么类型都可以使用
- 更灵活，可以针对特定属性进行 JSON 映射

```kotlin
@Entity
interface LedgerRecordValue {
    @Id
    val id: UUID

    @Serialized  // 属性级别注解
    val fieldDefinition: FieldDefinitionDto  // 即使 DTO 类没有 @Serialized 注解

    @Serialized
    val metadata: Map<String, Any>  // 也可以用于 Map、集合等类型
}
```

### 选择建议
- **使用类级别 `@Serialized`**：当该类型在多个地方都需要 JSON 存储时
- **使用属性级别 `@Serialized`**：当只有特定属性需要 JSON 存储，或想要覆盖类级别的设置时

### 2. 数据库映射
- 在数据库中，JSON 字段会被存储为 `JSONB` 类型（PostgreSQL）
- 或 `JSON` 类型（MySQL）
- 或 `TEXT` 类型（其他数据库）

### 3. 优势
- **避免关联问题**：不需要复杂的 `@ManyToOne` 关联
- **历史快照**：即使原始模板修改，历史记录保持不变
- **简化查询**：不需要 JOIN 多张表
- **类型安全**：使用 Kotlin 数据类，编译时检查

### 4. 使用场景
适合以下场景：
- 避免 Account 冲突或多租户问题
- 存储历史快照数据
- 简化复杂的关联关系
- 存储配置信息或元数据

### 5. 注意事项
- JSON 字段不适合需要索引的查询
- 修改 DTO 结构时需要考虑数据迁移
- JSON 内容不能直接在 SQL 中查询（除非使用数据库特定的 JSON 函数）

## 代码示例

### 创建 LedgerRecordValue

```kotlin
val fieldValue = LedgerRecordValue {
    record = ledgerRecord
    fieldDefinition = FieldDefinitionDto(
        fieldName = "name",
        fieldLabel = "姓名",
        fieldType = FieldType.TEXT,
        required = true,
        maxLength = 50
    )
    fieldValue = "张三"
}
```

### 查询时使用

```kotlin
val values = sqlClient.createQuery(LedgerRecordValue::class) {
    where(table.record.id eq recordId)
    select(table)
}.execute()
```

返回的对象中，`fieldDefinition` 会自动反序列化为 `FieldDefinitionDto` 实例。

## 相关文档

- [Jimmer 官方 JSON 文档](https://babyfish-ct.github.io/jimmer-doc/zh/docs/mapping/advanced/json)
- [PostgreSQL JSONB 文档](https://www.postgresql.org/docs/current/datatype-json.html)
- [MySQL JSON 文档](https://dev.mysql.com/doc/refman/8.0/en/json.html)