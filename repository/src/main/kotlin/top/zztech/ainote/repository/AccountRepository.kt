/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 账户数据仓库 - 提供账户实体的数据访问功能
 * Account Repository - Provides data access functionality for Account entity
 */

package top.zztech.ainote.repository

import org.babyfish.jimmer.Page
import org.babyfish.jimmer.Specification
import org.babyfish.jimmer.spring.repo.support.AbstractKotlinRepository
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.table.makeOrders
import org.springframework.stereotype.Repository
import top.zztech.ainote.model.Account
import top.zztech.ainote.model.Company
import java.util.UUID

/**
 * 账户数据仓库 - Account实体的Repository
 * 
 * 提供CRUD操作和用户账户的数据访问功能
 */
@Repository
class AccountRepository(
    sql: KSqlClient
) : AbstractKotlinRepository<Account, UUID>(sql) {
    fun findAllPage(
        pageIndex: Int,
        pageSize: Int,
        sort: String,
        search: Specification<Account>,
        fetcher: Fetcher<Account>?
    ): Page<Account> =
        createQuery {
            where(search)
            orderBy(table.makeOrders(sort))
            select(table.fetch(fetcher))
        }.fetchPage(pageIndex,pageSize)
}
