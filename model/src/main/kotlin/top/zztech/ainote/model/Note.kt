package top.zztech.ainote.model

import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import top.zztech.ainote.model.common.BaseEntity
import top.zztech.ainote.model.common.PassiveEntity
import top.zztech.ainote.model.common.TenantAware
import java.util.UUID

/**
 * Sample Entity - Note
 * 
 * This is a sample entity to demonstrate the structure.
 * You can replace or extend this with your own entities.
 */
@Entity
@KeyUniqueConstraint(
    noMoreUniqueConstraints = true,
    isNullNotDistinct = true
)
interface Note : PassiveEntity, TenantAware, BaseEntity {

    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    @Key
    val title: String

    val content: String?
    @ManyToMany
    @JoinTable(
        name = "note_static_file_mapper",
        joinColumnName = "note_id",
        inverseJoinColumnName = "static_file_id"
    )
    val files:List<StaticFile>
    // 经纬度
    val lat:Double?
    val lng:Double?
    @ManyToMany
    @JoinTable(
        name = "note_about_account_mapper",
        joinColumnName = "note_id",
        inverseJoinColumnName = "account_id"
    )
    val aboutAccount:List<Account>

}
