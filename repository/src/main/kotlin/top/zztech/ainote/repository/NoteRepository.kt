/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 */

package top.zztech.ainote.repository

import org.babyfish.jimmer.spring.repo.support.AbstractKotlinRepository
import top.zztech.ainote.model.Note
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class NoteRepository(
    sql: KSqlClient
) : AbstractKotlinRepository<Note, UUID>(sql) {

}
