/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 认证服务 - 提供用户登录和注册功能
 * Authentication Service - Provides user login and registration functionality
 */

package top.zztech.ainote.service

import org.babyfish.jimmer.Page
import org.babyfish.jimmer.client.FetchBy
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.ast.mutation.KSimpleSaveResult
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.beans.factory.ObjectProvider
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import top.zztech.ainote.error.AccountException
import top.zztech.ainote.integration.sms.SmsVerifyCodeService
import top.zztech.ainote.model.Account
import top.zztech.ainote.model.AccountCompanyEntity
import top.zztech.ainote.model.by
import top.zztech.ainote.model.password
import top.zztech.ainote.repository.AccountCompanyRepository
import top.zztech.ainote.repository.AccountRepository
import top.zztech.ainote.runtime.annotation.LogOperation
import top.zztech.ainote.runtime.dto.AuthResponse
import top.zztech.ainote.runtime.utility.JwtTokenProvider
import top.zztech.ainote.runtime.utility.getCurrentAccountId
import top.zztech.ainote.service.dto.AccountSearch
import top.zztech.ainote.service.dto.ChangeAccountStatusInput
import top.zztech.ainote.service.dto.JoinCompany
import top.zztech.ainote.service.dto.UpdateInput
import java.util.UUID

/**
 * 用户服务
 * 提供用户认证相关的REST API接口
 */

@RestController
@RequestMapping("/account")
class AccountService(
    val accountRepository: AccountRepository,
    val accountCompanyRepository: AccountCompanyRepository,
    val jwtTokenProvider: JwtTokenProvider,
    val passwordEncoder: PasswordEncoder,
    val smsVerifyCodeServiceProvider: ObjectProvider<SmsVerifyCodeService>
) {

    data class ChangePasswordInput(
        val oldPassword: String,
        val newPassword: String
    )

    data class ResetPasswordInput(
        val phone: String,
        val code: String,
        val scene: String,
        val newPassword: String
    )
    data class SwitchCompanyInput(
        val companyId: UUID
    )
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

    @LogOperation(action = "GET_ALL_ACCOUNT", entityType = "Account", includeRequest = false)
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    fun page(
        @RequestParam(defaultValue = "0") pageIndex: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "username asc, createdTime desc") sortCode: String,
        search: AccountSearch
    ): Page<@FetchBy("SIMPLE_ACCOUNT") Account> =
        accountRepository.findAllPage(pageIndex, pageSize, sortCode, search, SIMPLE_ACCOUNT)



    /**
     * 修改个人信息
     */
    @LogOperation(action = "UPDATE_MY_INFO", entityType = "Account", includeRequest = true)
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update")
    @Transactional
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
    @Transactional
    fun joinCompany(@RequestBody input: JoinCompany) : UUID {
        val currentUserId = getCurrentAccountId()
            ?: throw AccountException.usernameDoesNotExist()
        val modifiedInput = input.copy(id = currentUserId)
        return accountRepository.saveCommand(modifiedInput, SaveMode.UPDATE_ONLY, AssociatedSaveMode.APPEND).execute().modifiedEntity.id
    }

    /**
     * 修改用户状态
     */
    @LogOperation(action = "changeStatus", entityType = "Account",includeRequest = true)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/changeStatus")
    @Transactional
    fun changeStatus(@RequestBody input: ChangeAccountStatusInput): KSimpleSaveResult<Account> {
        return accountRepository.saveCommand(input, SaveMode.UPDATE_ONLY).execute();
    }

    @LogOperation(action = "CHANGE_PASSWORD", entityType = "Account", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/change-password")
    @Transactional
    fun changePassword(@RequestBody input: ChangePasswordInput): Map<String, String> {
        val currentUserId = getCurrentAccountId()
            ?: throw AccountException.usernameDoesNotExist()
        
        val account = accountRepository.findById(currentUserId, newFetcher(Account::class).by {
            password()
        }) ?: throw AccountException.usernameDoesNotExist()
        
        if (!passwordEncoder.matches(input.oldPassword, account.password)) {
            throw AccountException.passwordIsError("旧密码错误")
        }
        
        accountRepository.saveCommand(Account {
            id = currentUserId
            password = passwordEncoder.encode(input.newPassword)
        }).execute()
        
        return mapOf("message" to "密码修改成功")
    }

    @LogOperation(action = "RESET_PASSWORD", entityType = "Account", includeRequest = false)
    @PostMapping("/reset-password")
    @Transactional
    fun resetPassword(@RequestBody input: ResetPasswordInput): Map<String, String> {
        val svc = smsVerifyCodeServiceProvider.ifAvailable
            ?: throw AccountException.smsCodeIsError("短信服务未配置")
        svc.verify(input.scene, input.phone, input.code)

        val account = accountRepository.findByPhone(input.phone, null)
            ?: throw AccountException.phoneDoesNotExist()
        
        accountRepository.saveCommand(Account {
            id = account.id
            password = passwordEncoder.encode(input.newPassword)
        }).execute()
        
        return mapOf("message" to "密码重置成功")
    }

    /**
     * 切换企业
     */

    @LogOperation(action = "SWITCH_COMPANY", entityType = "Account", includeRequest = true)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/switch-company")
    @Transactional
    fun switchCompany(@RequestBody input: SwitchCompanyInput): AuthResponse {
        val currentUserId = getCurrentAccountId()
            ?: throw AccountException.usernameDoesNotExist()
        if (!accountCompanyRepository.hasCompany(currentUserId, input.companyId)) {
            throw AccountException.notInCompany()
        }

        accountCompanyRepository.switchChoiceCompany(currentUserId, input.companyId)

        val account = accountRepository.findById(currentUserId, newFetcher(Account::class).by {
            username()
            role()
        }) ?: throw AccountException.usernameDoesNotExist()

        val selectedCompany = accountCompanyRepository.getChoiceCompanyByAccount(currentUserId, SIMPLE_ACCOUNT_COMPANY)
        val tenant = selectedCompany?.company?.tenant ?: "default"

        return AuthResponse(
            currentUserId,
            jwtTokenProvider.generateToken(account.username),
            account.role.name,
            tenant
        )
    }


    companion object {
      private  val SIMPLE_ACCOUNT = newFetcher(Account::class).by {
           username()
           phone()
           role()
           status()
           avatar{
                   filePath()
                   fileName()
                   fileType()
           }
           accountCompanies {
                   choiceFlag()
                   role()
                   company {
                       tenant()
                       name()
                   }
           }
      }

      private val SIMPLE_ACCOUNT_COMPANY = newFetcher(top.zztech.ainote.model.AccountCompanyEntity::class).by {
          choiceFlag()
          role()
          company {
              tenant()
              name()
          }
      }
    }
} 