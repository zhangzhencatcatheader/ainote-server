/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 笔记服务 - 提供笔记管理功能
 * Note Service - Provides note management functionality
 */

package top.zztech.ainote.service

import org.babyfish.jimmer.Page
import org.babyfish.jimmer.client.FetchBy
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.security.access.prepost.PreAuthorize
import top.zztech.ainote.repository.NoteRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import top.zztech.ainote.error.AccountException
import top.zztech.ainote.error.NoteException
import top.zztech.ainote.model.Note
import top.zztech.ainote.model.by
import top.zztech.ainote.runtime.annotation.LogOperation
import top.zztech.ainote.runtime.utility.getCurrentAccountId
import top.zztech.ainote.service.dto.CreateNote
import top.zztech.ainote.service.dto.NoteSearch
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
    fun add(@RequestBody input: CreateNote): UUID {
        if (input.title.isBlank()) {
            throw NoteException.noteTitleEmpty()
        }
        return noteRepository.saveCommand(input, SaveMode.INSERT_ONLY).execute().modifiedEntity.id
    }

    /**
     * 我的笔记page
     */

    /**
     * 分页获取当前用户的笔记列表（带搜索）
     */
    @GetMapping("/myNotePage")
    @LogOperation(action = "QUERY_NOTE_PAGE", entityType = "Note", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    fun myNotePage(
        @RequestParam(defaultValue = "0") pageIndex: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "createdTime desc") sort: String,
        @RequestParam search: NoteSearch?
    ): Page<@FetchBy("LIST_NOTE") Note> {
        val currentUserId = getCurrentAccountId()
            ?: throw AccountException.usernameDoesNotExist()
        return noteRepository.findPageByCurrentUser(
            pageIndex = pageIndex,
            pageSize = pageSize,
            sort = sort,
            currentUserId = currentUserId,
            search = search,
            fetcher = LIST_NOTE
        )
    }

    /**
     * 分页查询当前租户下的全部笔记（带搜索）
     */
    @GetMapping("/tenantNotePage")
    @LogOperation(action = "QUERY_TENANT_NOTE_PAGE", entityType = "Note", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    fun tenantNotePage(
        @RequestParam(defaultValue = "0") pageIndex: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "createdTime desc") sort: String,
        @RequestParam search: NoteSearch?
    ): Page<@FetchBy("LIST_NOTE") Note> {
            
        return noteRepository.findPageByTenant(
            pageIndex = pageIndex,
            pageSize = pageSize,
            sort = sort,
            search = search,
            fetcher = LIST_NOTE
        )
    }
    /**
     * 获取笔记详情
     */

    @GetMapping("/{id}")
    @LogOperation(action = "GET_NOTE_DETAIL", entityType = "Note", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    fun detail(
        @PathVariable id: UUID
    ): @FetchBy("LIST_NOTE") Note? =
        noteRepository.findById(id, LIST_NOTE) ?: throw NoteException.noteNotFound()

    /**
     * 删除笔记
     */
    @DeleteMapping("/{id}")
    @LogOperation(action = "DELETE_NOTE", entityType = "Note")
    @PreAuthorize("isAuthenticated()")
    fun delete(
        @PathVariable id: UUID
    ) {
        // The delete operation will be automatically scoped to the current tenant
        // by the TenantAwareDraftInterceptor
        noteRepository.findById(id) ?: throw NoteException.noteNotFound()
        noteRepository.deleteById(id)
    }

    companion object {
        private val LIST_NOTE = newFetcher(Note::class).by {
            allScalarFields()
            files {
                allScalarFields()
            }
            aboutAccount {
                allScalarFields()
            }
        }
    }
}
