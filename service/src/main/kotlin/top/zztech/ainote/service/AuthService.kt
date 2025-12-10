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
import org.babyfish.jimmer.sql.kt.ast.expression.and
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
import top.zztech.ainote.model.by
import top.zztech.ainote.model.username
import top.zztech.ainote.repository.AccountCompanyRepository
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
 * 认证服务
 * 提供用户认证相关的REST API接口
 */

@RestController
@RequestMapping("/auth")
class AuthService(
    val sql: KSqlClient,
    val authenticationManager: AuthenticationManager,
    val jwtTokenProvider: JwtTokenProvider,
    val passwordEncoder: PasswordEncoder,
    val accountCompanyRepository: AccountCompanyRepository
) {
    /**
     * 用户登录
     * @param input 登录信息（用户名和密码）
     * @return 认证响应（包含token和用户信息）
     */
    @LogOperation(action = "LOGIN", entityType = "Account", includeRequest = true)
    @PostMapping("/login")
    @Transactional
    fun login(input: LoginInput): AuthResponse {
        val user = sql.createQuery(Account::class) {
            where(table.username eq input.username)
            select(table)
        }.fetchOneOrNull() ?: throw AccountException.usernameDoesNotExist()

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                input.username,
                input.password
            )
        )

        // 查找用户是否有 choiceFlag = true 的 AccountCompany 记录
        val selectedCompany =accountCompanyRepository.getChoiceCompanyByAccount(user.id,SIMPLE_ACCOUNT_COMPANY)

        // 如果找到选择的公司，返回其 tenant；否则返回默认 "default"
        val tenant = selectedCompany?.company?.tenant ?: "default"

        return AuthResponse(
            user.id,
            jwtTokenProvider.generateToken(input.username),
            user.role.name,
            tenant
        )
    }

    /**
     * 用户注册
     * @param input 注册信息（用户名、密码等）
     * @return 认证响应（包含token和用户信息）
     */
    @LogOperation(action = "REGISTER", entityType = "Account", includeRequest = true)
    @Transactional
    @PostMapping("/register")
    fun register(input: RegisterInput): AuthResponse {
        try {
            val account = sql.save(input.copy(password = passwordEncoder.encode(input.password))) {
                setMode(SaveMode.INSERT_ONLY)
            }.modifiedEntity
            // 注册成功后自动生成token
            return AuthResponse(
                account.id,
                jwtTokenProvider.generateToken(input.username),
                account.role.name,
                "default"
            )
        } catch (e: SaveException.NotUnique) {
            throw AccountException.usernameAlreadyExists()
        }
    }


    companion object {
        private  val SIMPLE_ACCOUNT_COMPANY = newFetcher(AccountCompanyEntity::class).by {
            choiceFlag()
            role()
            company {
                name()
                tenant()
            }

        }
    }
} 