/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 模板服务 - 提供台账模板管理功能
 * Template Service - Provides ledger template management functionality
 */

package top.zztech.ainote.service

import org.babyfish.jimmer.Page
import org.babyfish.jimmer.client.FetchBy
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import top.zztech.ainote.error.AccountException
import top.zztech.ainote.error.NoteException
import top.zztech.ainote.model.LedgerTemplate
import top.zztech.ainote.model.by
import top.zztech.ainote.repository.LedgerTemplateRepository
import top.zztech.ainote.runtime.annotation.LogOperation
import top.zztech.ainote.runtime.utility.getCurrentAccountId
import top.zztech.ainote.service.dto.ChangeTemplateStatus
import top.zztech.ainote.service.dto.CreateTemplate
import top.zztech.ainote.service.dto.CreateTemplateField
import top.zztech.ainote.service.dto.SearchTemplate
import top.zztech.ainote.service.dto.UpdateTemplate
import java.util.UUID

/**
 * 模版
 */
@RestController
@RequestMapping("/template")
@Transactional
class TemplateService(
    private val templateRepository: LedgerTemplateRepository,
    private val aiService: AiService
) {
    /**
     * 创建模板
     */
    @LogOperation(action = "CREATE_TEMPLATE", entityType = "LedgerTemplate", includeRequest = true)
    @PostMapping("add")
    @PreAuthorize("isAuthenticated()")
    fun add(@RequestBody input: CreateTemplate): UUID {
        if (input.name.isBlank()) {
            throw NoteException.noteTitleEmpty()
        }
        return templateRepository.saveCommand(input, SaveMode.INSERT_ONLY).execute().modifiedEntity.id
    }
    /**
     * ai识别文档生成字段
     */
    @PostMapping("/generateFields")
    @LogOperation(action = "AI_GENERATE_TEMPLATE_FIELDS", entityType = "LedgerTemplateField", includeRequest = true)
    @PreAuthorize("isAuthenticated()")
    fun generateFields(@RequestBody input: CreateTemplate): List<CreateTemplateField> {
        return aiService.generateTemplateFieldsByAi(input)
    }

    /**
    * 确认字段创建
    **/
    @PostMapping("/updateFields")
    @LogOperation(action = "AI_GENERATE_TEMPLATE_FIELDS", entityType = "LedgerTemplateField", includeRequest = true)
    @PreAuthorize("isAuthenticated()")
    fun createFields(@RequestBody input: UpdateTemplate) : UUID{
        return templateRepository.saveCommand(input,SaveMode.UPDATE_ONLY).execute().modifiedEntity.id
    }


    /**
     * 我的模板page
     */

    /**
     * 分页获取当前用户可见的模板列表（带搜索）
     */
    @GetMapping("/myTemplatePage")
    @LogOperation(action = "QUERY_MY_TEMPLATE_PAGE", entityType = "LedgerTemplate", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    fun myTemplatePage(
        @RequestParam(defaultValue = "0") pageIndex: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "createdTime desc") sort: String,
        search: SearchTemplate
    ): Page<@FetchBy("LIST_TEMPLATE") LedgerTemplate> {
        val currentUserId = getCurrentAccountId()
            ?: throw AccountException.usernameDoesNotExist()
        return templateRepository.findAllByAccountId(
            pageIndex = pageIndex,
            pageSize = pageSize,
            sort = sort,
            currentUserId = currentUserId,
            search = search,
            fetcher = LIST_TEMPLATE
        )
    }

    /**
     * 分页查询当前租户下的全部模板（带搜索）
     */
    @GetMapping("/tenantTemplatePage")
    @LogOperation(action = "QUERY_TENANT_TEMPLATE_PAGE", entityType = "LedgerTemplate", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    fun tenantTemplatePage(
        @RequestParam(defaultValue = "0") pageIndex: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "createdTime desc") sort: String,
         search: SearchTemplate
    ): Page<@FetchBy("LIST_TEMPLATE") LedgerTemplate> {
            
        return templateRepository.findAllPage(
            pageIndex = pageIndex,
            pageSize = pageSize,
            sort = sort,
            search = search,
            fetcher = LIST_TEMPLATE
        )
    }
    /**
     * 获取模板详情
     */

    @GetMapping("/{id}")
    @LogOperation(action = "GET_TEMPLATE_DETAIL", entityType = "LedgerTemplate", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    fun detail(
        @PathVariable id: UUID
    ): @FetchBy("SIMPLE_TEMPLATE") LedgerTemplate? =
        templateRepository.findById(id, SIMPLE_TEMPLATE) ?: throw NoteException.noteNotFound()

    /**
     * 修改模板状态
     */
    @PutMapping("/changeStatus")
    @LogOperation(action = "")
    @PreAuthorize("isAuthenticated()")
    fun changeStatus(
        @RequestBody input: ChangeTemplateStatus
    ) =templateRepository.saveCommand(input,SaveMode.UPDATE_ONLY).execute()


    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    @LogOperation(action = "DELETE_TEMPLATE", entityType = "LedgerTemplate")
    @PreAuthorize("isAuthenticated()")
    fun delete(
        @PathVariable id: UUID
    ) {
        // The delete operation will be automatically scoped to the current tenant
        // by the TenantAwareDraftInterceptor
        templateRepository.findById(id) ?: throw NoteException.noteNotFound()
        templateRepository.deleteById(id)
    }

    companion object {
        private val LIST_TEMPLATE = newFetcher(LedgerTemplate::class).by {
            name()
            category()
            description()
            enabled()
            icon {
                allScalarFields()
            }
            file()
            fields()
        }

        private val SIMPLE_TEMPLATE= newFetcher(LedgerTemplate::class).by {
            name()
            category()
            description()
            enabled()
            icon {
                filePath()
            }
            file {
                filePath()
            }
            fields {
                fieldName()
                fieldLabel()
                fieldOptions()
                fieldType()
                required()
            }
            color()

        }
    }
}
