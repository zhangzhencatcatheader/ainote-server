package top.zztech.ainote.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import top.zztech.ainote.model.common.BaseEntity
import top.zztech.ainote.model.common.PassiveEntity
import top.zztech.ainote.model.common.TenantAware
import top.zztech.ainote.model.enums.LedgerRecordStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * 台账记录实体
 * 
 * 存储根据模板创建的实际台账记录
 * 支持审批流程和状态管理
 */
@Entity
@Table(name = "ledger_record")
interface LedgerRecord : PassiveEntity, BaseEntity, TenantAware {

    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    /**
     * 所属模板
     */
    @ManyToOne
    val template: LedgerTemplate?

    /**
     * 记录编号（可自定义规则生成）
     */
    val recordNo: String?

    /**
     * 记录日期
     */
    @get:JsonFormat(pattern = "yyyy-MM-dd")
    val recordDate: LocalDate

    /**
     * 记录状态：DRAFT草稿、SUBMITTED已提交、APPROVED已审批、REJECTED已驳回
     */
    @Default("'DRAFT'")
    val status: LedgerRecordStatus

    /**
     * 关联的公司（可选）
     */
    @ManyToOne
    val company: Company?

    // ========== 审批信息 ==========

    /**
     * 提交人
     */
    @ManyToOne
    val submitter: Account?

    /**
     * 提交时间
     */
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val submittedTime: LocalDateTime?

    /**
     * 审批人
     */
    @ManyToOne
    val approver: Account?

    /**
     * 审批时间
     */
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val approvedTime: LocalDateTime?

    /**
     * 审批意见
     */
    val approvalComment: String?

    /**
     * 备注信息
     */
    val remark: String?

    /**
     * 记录的字段值集合
     */
    @OneToMany(mappedBy = "record")
    val values: List<LedgerRecordValue>
}
