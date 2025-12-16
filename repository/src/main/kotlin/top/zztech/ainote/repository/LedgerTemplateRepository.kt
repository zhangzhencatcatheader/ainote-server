/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 */
package top.zztech.ainote.repository

import org.babyfish.jimmer.Page
import org.babyfish.jimmer.Specification
import org.babyfish.jimmer.spring.repo.support.AbstractKotlinRepository
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.table.makeOrders
import org.springframework.stereotype.Repository
import top.zztech.ainote.model.Company
import top.zztech.ainote.model.LedgerTemplate
import top.zztech.ainote.model.accountCompanies
import top.zztech.ainote.model.accountId
import java.util.UUID

@Repository
class LedgerTemplateRepository(
    sql: KSqlClient
) : AbstractKotlinRepository<LedgerTemplate, UUID>(sql) {
    fun findAllPage(
        pageIndex: Int,
        pageSize: Int,
        sort: String,
        search: Specification<LedgerTemplate>?,
        fetcher: Fetcher<LedgerTemplate>?
    ): Page<LedgerTemplate> =
        createQuery {
            where(search)
            orderBy(table.makeOrders(sort))
            select(table.fetch(fetcher))
        }.fetchPage(pageIndex,pageSize)

    fun findAllByAccountId(
        pageIndex: Int,
        pageSize: Int,
        sort: String,
        currentUserId: UUID,
        search: Specification<LedgerTemplate>?,
        fetcher: Fetcher<LedgerTemplate>?): Page<LedgerTemplate> =
    createQuery {
        where (table.accountId.eq(currentUserId))
        orderBy(table.makeOrders(sort))
        where(search)
        select(table.fetch(fetcher))
    }.fetchPage(pageIndex,pageSize)
}
