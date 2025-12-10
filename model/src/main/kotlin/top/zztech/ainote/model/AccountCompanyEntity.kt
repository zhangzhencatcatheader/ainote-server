package top.zztech.ainote.model

import org.babyfish.jimmer.sql.Default
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.KeyUniqueConstraint
import org.babyfish.jimmer.sql.Table
import top.zztech.ainote.model.common.BaseEntity
import top.zztech.ainote.model.enums.RoleEnum
import java.util.UUID
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import top.zztech.ainote.model.common.PassiveEntity

@Entity
@Table(name = "account_company")
@KeyUniqueConstraint(
    noMoreUniqueConstraints = true,
    isNullNotDistinct = true
)
interface AccountCompanyEntity : PassiveEntity,BaseEntity {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID
    @Default("USER")
    val role: RoleEnum
    @Default("true")
    val choiceFlag : Boolean
    @ManyToOne
    val company: Company
}