/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 账户数据仓库 - 提供账户实体的数据访问功能
 * Account Repository - Provides data access functionality for Account entity
 */

package top.zztech.ainote.repository

import org.babyfish.jimmer.spring.repo.support.AbstractKotlinRepository
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.stereotype.Repository
import top.zztech.ainote.model.Account
import top.zztech.ainote.model.AccountCompanyEntity
import top.zztech.ainote.model.accountId
import top.zztech.ainote.model.choiceFlag
import top.zztech.ainote.model.fetchBy
import java.util.UUID

/**
 * 账户数据仓库 - Account实体的Repository
 * 
 * 提供CRUD操作和用户账户的数据访问功能
 */
@Repository
class AccountCompanyRepository(
    sql: KSqlClient
) : AbstractKotlinRepository<AccountCompanyEntity, UUID>(sql) {

    fun getTenantByAccount(id: UUID): AccountCompanyEntity? =
        sql.createQuery(AccountCompanyEntity::class) {
        where(table.accountId eq id)
            where(table.choiceFlag.eq(true))
            select(table)
        }.fetchOneOrNull()

    fun getChoiceCompanyByAccount(id: UUID, fetcher: Fetcher<AccountCompanyEntity>) =
        sql.createQuery(AccountCompanyEntity::class) {
            where(table.accountId eq id)
            where(table.choiceFlag.eq(true))
            select(table.fetch(fetcher))
        }.fetchOneOrNull()

}
