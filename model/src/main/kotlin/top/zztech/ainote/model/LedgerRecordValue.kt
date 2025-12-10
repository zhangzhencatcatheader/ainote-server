package top.zztech.ainote.model

import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import top.zztech.ainote.model.common.BaseEntity
import top.zztech.ainote.model.dto.FieldDefinitionDto
import java.util.UUID

/**
 * 台账记录值实体
 * 
 * 采用 EAV (Entity-Attribute-Value) 模式存储台账记录的字段值
 * 支持动态字段的灵活存储
 */
@Entity
@Table(name = "ledger_record_value")
@KeyUniqueConstraint(
    noMoreUniqueConstraints = true,
    isNullNotDistinct = true
)
interface LedgerRecordValue : BaseEntity {

    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    /**
     * 所属记录
     */
    @Key
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    @JoinColumn(name = "record_id")
    val record: LedgerRecord

    val fieldDefinition: FieldDefinitionDto

    /**
     * 字段值（所有类型统一存储为文本，前端根据 field.fieldType 解析）
     * 
     * 存储格式说明：
     * - TEXT/TEXTAREA: 直接存储文本
     * - NUMBER/INTEGER/DECIMAL: 存储数字的字符串形式
     * - DATE: "yyyy-MM-dd"
     * - DATETIME: "yyyy-MM-dd HH:mm:ss"
     * - TIME: "HH:mm:ss"
     * - BOOLEAN: "true" 或 "false"
     * - SELECT: 存储选中的值
     * - MULTISELECT: JSON数组格式，如 ["选项1", "选项2"]
     * - FILE: JSON数组格式，存储文件ID列表，如 ["uuid1", "uuid2"]
     */
    val fieldValue: String?
}
