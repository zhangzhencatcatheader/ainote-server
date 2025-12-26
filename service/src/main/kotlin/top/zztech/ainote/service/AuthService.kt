/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 认证服务 - 提供用户登录和注册功能
 * Authentication Service - Provides user login and registration functionality
 */

package top.zztech.ainote.service

import cn.hutool.captcha.CaptchaUtil
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.exception.SaveException
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.redisson.api.RedissonClient
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.beans.factory.ObjectProvider
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.zztech.ainote.error.AccountException
import top.zztech.ainote.model.Account
import top.zztech.ainote.model.AccountCompanyEntity
import top.zztech.ainote.model.by
import top.zztech.ainote.model.phone
import top.zztech.ainote.model.username
import top.zztech.ainote.repository.AccountCompanyRepository
import top.zztech.ainote.runtime.annotation.LogOperation
import top.zztech.ainote.runtime.dto.AuthResponse
import top.zztech.ainote.runtime.utility.JwtTokenProvider
import top.zztech.ainote.integration.sms.SmsVerifyCodeService
import top.zztech.ainote.service.dto.LoginInput
import top.zztech.ainote.service.dto.VerifyCaptchaInput
import top.zztech.ainote.service.dto.SendSmsCodeInput
import top.zztech.ainote.service.dto.SmsLoginInput
import top.zztech.ainote.service.dto.RegisterInput
import java.util.*
import java.util.concurrent.TimeUnit

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
    val accountCompanyRepository: AccountCompanyRepository,
    val redissonClient: RedissonClient,
    val smsVerifyCodeServiceProvider: ObjectProvider<SmsVerifyCodeService>
) {
    /**
     * 用户登录
     * @param input 登录信息（用户名和密码）
     * @return 认证响应（包含token和用户信息）
     */
    @LogOperation(action = "LOGIN", entityType = "Account", includeRequest = true)
    @PostMapping("/login")
    @Transactional
    @Throws(AccountException::class)
    fun login(@RequestBody input: LoginInput): AuthResponse {
        requireCaptchaOk(input.verCode, input.verKey)
        val user = sql.createQuery(Account::class) {
            where(table.username eq input.username)
            select(table)
        }.fetchOneOrNull() ?: throw AccountException.usernameDoesNotExist()
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    input.username,
                    input.password
                )
            )
        } catch (e: BadCredentialsException) {
            throw AccountException.passwordIsError("账号或密码错误") // 使用你定义的 PASSWORD_IS_ERROR
        }

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
    fun register(@RequestBody input: RegisterInput): AuthResponse {
        try {
            val svc = smsVerifyCodeServiceProvider.ifAvailable
                ?: throw AccountException.smsCodeIsError("短信服务未配置")
            svc.verify(input.scene, input.phone, input.code)

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

    @LogOperation(action = "SEND_SMS_CODE", entityType = "Account", includeRequest = true)
    @PostMapping("/sms/send")
    fun sendSms(@RequestBody input: SendSmsCodeInput): SmsVerifyCodeService.SmsSendResult {
        val svc = smsVerifyCodeServiceProvider.ifAvailable
            ?: throw AccountException.smsCodeIsError("短信服务未配置")
        return svc.send(input.scene, input.phone)
    }

    @LogOperation(action = "SMS_LOGIN", entityType = "Account", includeRequest = true)
    @PostMapping("/sms/login")
    @Transactional
    fun smsLogin(@RequestBody input: SmsLoginInput): AuthResponse {
        val svc = smsVerifyCodeServiceProvider.ifAvailable
            ?: throw AccountException.smsCodeIsError("短信服务未配置")
        svc.verify(input.scene, input.phone, input.code)

        val user = sql.createQuery(Account::class) {
            where(table.phone eq input.phone)
            select(table)
        }.fetchOneOrNull() ?: throw AccountException.phoneDoesNotExist()

        val selectedCompany = accountCompanyRepository.getChoiceCompanyByAccount(user.id, SIMPLE_ACCOUNT_COMPANY)
        val tenant = selectedCompany?.company?.tenant ?: "default"

        return AuthResponse(
            user.id,
            jwtTokenProvider.generateToken(user.username),
            user.role.name,
            tenant
        )
    }

    @GetMapping("/captcha")
    fun captcha(): CaptchaResponse {
        val lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100)
        val key = UUID.randomUUID().toString()
        redissonClient.getBucket<String>(CAPTCHA_REDIS_KEY_PREFIX + key)
            .set(lineCaptcha.getCode().trim().lowercase(), CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES)
        return CaptchaResponse(
            key = key,
            image = lineCaptcha.imageBase64Data
        )
    }

    @PostMapping("/captcha/verify")
    fun verifyCaptcha(@RequestBody input: VerifyCaptchaInput): VerifyCaptchaResponse {
        return VerifyCaptchaResponse(ok = captchaIsTrue(input.verCode, input.verKey))
    }

    private fun requireCaptchaOk(verCode: String?, verKey: String?) {
        if (!captchaIsTrue(verCode, verKey)) {
            throw AccountException.captchaIsError()
        }
    }

    private fun captchaIsTrue(verCode: String?, verKey: String?): Boolean {
        if (verCode.isNullOrBlank() || verKey.isNullOrBlank()) {
            return false
        }
        val bucket = redissonClient.getBucket<String>(CAPTCHA_REDIS_KEY_PREFIX + verKey)
        val redisCode = bucket.get()
        val ok = !redisCode.isNullOrBlank() && redisCode.trim().equals(verCode.trim(), ignoreCase = true)
        if (ok) {
            bucket.delete()
        }
        return ok
    }

    data class CaptchaResponse(
        val key: String,
        val image: String
    )

    data class VerifyCaptchaResponse(
        val ok: Boolean
    )

    companion object {
        private const val CAPTCHA_REDIS_KEY_PREFIX = "captcha:"
        private const val CAPTCHA_TTL_MINUTES: Long = 30

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