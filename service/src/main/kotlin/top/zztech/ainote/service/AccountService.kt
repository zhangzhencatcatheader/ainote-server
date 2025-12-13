/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 认证服务 - 提供用户登录和注册功能
 * Authentication Service - Provides user login and registration functionality
 */

package top.zztech.ainote.service

import org.babyfish.jimmer.client.FetchBy
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.exception.SaveException
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.mutation.KSimpleSaveResult
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.zztech.ainote.error.AccountException
import top.zztech.ainote.model.Account
import top.zztech.ainote.model.AccountCompanyEntity
import top.zztech.ainote.model.Company
import top.zztech.ainote.model.StaticFile
import top.zztech.ainote.model.by
import top.zztech.ainote.model.username
import top.zztech.ainote.repository.AccountRepository
import top.zztech.ainote.runtime.dto.AuthResponse
import top.zztech.ainote.runtime.utility.JwtTokenProvider
import top.zztech.ainote.runtime.annotation.LogOperation
import top.zztech.ainote.runtime.utility.getCurrentAccountId
import top.zztech.ainote.service.dto.JoinCompany
import top.zztech.ainote.service.dto.LoginInput
import top.zztech.ainote.service.dto.RegisterInput
import top.zztech.ainote.service.dto.UpdateInput
import java.util.UUID

/**
 * 用户服务
 * 提供用户认证相关的REST API接口
 */

@RestController
@RequestMapping("/account")
class AccountService(
    val sql: KSqlClient,
    val accountRepository: AccountRepository
) {

    /**
     * 获取我的个人信息
     */
    @LogOperation(action = "GET_MY_INFO", entityType = "Account", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    fun me(): @FetchBy("SIMPLE_ACCOUNT") Account? {
        val currentUserId = getCurrentAccountId() 
            ?: throw AccountException.usernameDoesNotExist()
        return accountRepository.findById(currentUserId,SIMPLE_ACCOUNT)
    }


    /**
     * 修改个人信息
     */
    @LogOperation(action = "UPDATE_MY_INFO", entityType = "Account", includeRequest = true)
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update")
    fun update(@RequestBody updateInput: UpdateInput): KSimpleSaveResult<Account> {
        val currentUserId = getCurrentAccountId()
            ?: throw AccountException.usernameDoesNotExist()
        val modifiedInput = updateInput.copy(id = currentUserId)
        return accountRepository.saveCommand(modifiedInput, SaveMode.UPDATE_ONLY).execute();
    }
    /**
     * 加入企业
     */
    @LogOperation(action = "JOIN_COMPANY", entityType = "Account", includeRequest = true)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/joinCompany")
    fun joinCompany(@RequestBody input: JoinCompany) : UUID {
        val currentUserId = getCurrentAccountId()
            ?: throw AccountException.usernameDoesNotExist()
        val modifiedInput = input.copy(id = currentUserId)
        return sql.saveCommand(modifiedInput, SaveMode.UPDATE_ONLY, AssociatedSaveMode.APPEND).execute().modifiedEntity.id
    }


    companion object {
      private  val SIMPLE_ACCOUNT = newFetcher(Account::class).by {
           username()
           phone()
           role()
           avatar{
                   filePath()
                   fileName()
                   fileType()
           }

           companies(
               newFetcher(Company::class).by {
                   name()
               }
           )
           accountCompanies(
               newFetcher(AccountCompanyEntity::class).by {
                   company(
                       newFetcher(Company::class).by {
                           name()
                       }
                   )
                   role()
               }
           )
      }
    }
} 