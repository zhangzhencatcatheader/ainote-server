package top.zztech.ainote.model

import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import top.zztech.ainote.model.common.BaseEntity
import top.zztech.ainote.model.common.TenantAware
import top.zztech.ainote.model.enums.CompanyStatus
import java.util.UUID

/**
 * Company entity
 */
@Entity
@KeyUniqueConstraint(
    noMoreUniqueConstraints = true,
    isNullNotDistinct = true
)
interface Company : BaseEntity {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    val name: String

    val code: String

    val phone: String?

    val address: String?

    val contact: String?
    
    @Default(value = "1")
    val status: CompanyStatus

    @Default(value = "abc")
    val tenant: String

    @OneToMany(mappedBy = "company")
    val accountCompanies: List<AccountCompanyEntity>


    @ManyToManyView(
        prop = "accountCompanies",
        deeperProp = "account"
    )
    val accounts: List<Account>
}