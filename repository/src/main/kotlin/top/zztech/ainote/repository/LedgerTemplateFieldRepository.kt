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
import top.zztech.ainote.model.LedgerTemplateField
import top.zztech.ainote.model.accountCompanies
import top.zztech.ainote.model.accountId
import java.util.UUID

@Repository
class LedgerTemplateFieldRepository(
    sql: KSqlClient
) : AbstractKotlinRepository<LedgerTemplateField, UUID>(sql) {
}
