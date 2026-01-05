/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 账户数据仓库 - 提供账户实体的数据访问功能
 * Account Repository - Provides data access functionality for Account entity
 */

package top.zztech.ainote.repository

import org.babyfish.jimmer.kt.set
import org.babyfish.jimmer.spring.repo.support.AbstractKotlinRepository
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.stereotype.Repository
import top.zztech.ainote.model.Account
import top.zztech.ainote.model.AccountCompanyEntity
import top.zztech.ainote.model.accountId
import top.zztech.ainote.model.choiceFlag
import top.zztech.ainote.model.companyId
import top.zztech.ainote.model.enums.RoleEnum
import top.zztech.ainote.model.fetchBy
import top.zztech.ainote.model.role
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

    private val sqlClient: KSqlClient = sql

    fun getChoiceCompanyByAccount(id: UUID, fetcher: Fetcher<AccountCompanyEntity>) =
        sql.createQuery(AccountCompanyEntity::class) {
            where(table.accountId eq id)
            where(table.choiceFlag.eq(true))
            select(table.fetch(fetcher))
        }.fetchOneOrNull()

    fun changeRole(companyId: UUID, account: UUID, admin: RoleEnum) {
        sql.save(AccountCompanyEntity {
            this.companyId = companyId
            this.accountId = account
            this.role = admin
        })
    }

    fun hasCompany(accountId: UUID, companyId: UUID): Boolean =
        sqlClient.createQuery(AccountCompanyEntity::class) {
            where(table.accountId eq accountId)
            where(table.companyId eq companyId)
            select(table)
        }.fetchOneOrNull() != null

    fun switchChoiceCompany(accountId: UUID, companyId: UUID) {
        // 先将该用户的所有公司的 choiceFlag 设为 false
        sqlClient.createUpdate(AccountCompanyEntity::class) {
            where(table.accountId eq accountId)
            set(table.choiceFlag, false)
        }.execute()

        // 再将指定公司的 choiceFlag 设为 true
        sqlClient.createUpdate(AccountCompanyEntity::class) {
            where(table.accountId eq accountId)
            where(table.companyId eq companyId)
            set(table.choiceFlag, true)
        }.execute()
    }

    fun findAllByCompanyId(
        companyId: UUID,
        fetcher: Fetcher<AccountCompanyEntity>?
    ): List<AccountCompanyEntity> =
        createQuery {
            where(table.companyId eq companyId)
            where(table.choiceFlag.eq(true))
            select(table.fetch(fetcher))
        }.execute()
}
