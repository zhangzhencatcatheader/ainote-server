package top.zztech.ainote.model

import org.babyfish.jimmer.sql.Default
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.KeyUniqueConstraint
import org.babyfish.jimmer.sql.OneToOne
import org.babyfish.jimmer.sql.ManyToManyView
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import top.zztech.ainote.model.common.BaseEntity
import top.zztech.ainote.model.enums.RoleEnum
import top.zztech.ainote.model.enums.UserStatus
import java.util.UUID

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

}