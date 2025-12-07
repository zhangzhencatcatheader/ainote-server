package top.zztech.ainote.model.common

import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.MappedSuperclass
import top.zztech.ainote.model.Account

@MappedSuperclass
interface PassiveEntity {
    @ManyToOne
    val account: Account
}