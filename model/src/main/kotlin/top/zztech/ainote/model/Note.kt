package top.zztech.ainote.model

import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import top.zztech.ainote.model.common.BaseEntity
import top.zztech.ainote.model.common.PassiveEntity
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
interface Note : PassiveEntity, BaseEntity {

    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    @Key
    val title: String

    val content: String?
}
