/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 笔记服务 - 提供笔记管理功能
 * Note Service - Provides note management functionality
 */

package top.zztech.ainote.service

import org.babyfish.jimmer.client.FetchBy
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.springframework.security.access.prepost.PreAuthorize
import top.zztech.ainote.model.Note
import top.zztech.ainote.repository.NoteRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import top.zztech.ainote.runtime.annotation.LogOperation
import top.zztech.ainote.service.dto.CreateNote
import java.util.UUID

/**
 * 笔记服务
 */
@RestController
@RequestMapping("/note")
@Transactional
class NoteService(
    private val noteRepository: NoteRepository
) {
    /**
     * 创建一条笔记
     */
    @LogOperation(action = "CREATE_NOTE", entityType = "Note", includeRequest = true)
    @PostMapping("add")
    @PreAuthorize("isAuthenticated()")
    fun add(@RequestBody input: CreateNote): UUID =
        noteRepository.saveCommand(input, SaveMode.INSERT_ONLY).execute().modifiedEntity.id

}
