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
import org.babyfish.jimmer.sql.kt.ast.expression.*
import org.babyfish.jimmer.sql.kt.ast.table.makeOrders
import org.springframework.stereotype.Repository
import top.zztech.ainote.model.Note
import top.zztech.ainote.model.accountId
import top.zztech.ainote.model.tenant
import java.util.*

@Repository
class NoteRepository(
    sql: KSqlClient
) : AbstractKotlinRepository<Note, UUID>(sql) {
    
    fun findPageByCurrentUser(
        pageIndex: Int,
        pageSize: Int,
        sort: String,
        currentUserId: UUID?,
        search: Specification<Note>?,
        fetcher: Fetcher<Note>?
    ): Page<Note> {
        return createQuery {
            where(search)
            where(table.accountId.eq(currentUserId))
            orderBy(table.makeOrders(sort))
            select(table.fetch(fetcher))
        }.fetchPage(pageIndex, pageSize)
    }
    
    fun findPageByTenant(
        pageIndex: Int,
        pageSize: Int,
        sort: String,
        search: Specification<Note>?,
        fetcher: Fetcher<Note>?
    ): Page<Note> {
        return createQuery {
            where(search)
            orderBy(table.makeOrders(sort))
            select(table.fetch(fetcher))
        }.fetchPage(pageIndex, pageSize)
    }
}
