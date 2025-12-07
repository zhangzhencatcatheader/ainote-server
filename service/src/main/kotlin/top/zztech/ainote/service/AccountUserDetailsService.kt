/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 账户用户详情服务 - 提供Spring Security所需的用户认证信息
 * Account UserDetailsService - Provides user authentication information for Spring Security
 */

package top.zztech.ainote.service

import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import top.zztech.ainote.model.Account
import top.zztech.ainote.model.username
import top.zztech.ainote.runtime.dto.IdUserDetails

/**
 * 账户用户详情服务
 * 用于Spring Security认证，从数据库加载用户信息
 */
@Component
class AccountUserDetailsService(
    val sql: KSqlClient
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val account = sql.createQuery(Account::class) {
            where(table.username eq username)
            select(table)
        }.fetchOneOrNull() ?: throw UsernameNotFoundException("用户不存在: $username")
        
        return object : IdUserDetails(account.id) {
            override fun getAuthorities(): Collection<GrantedAuthority> =
                listOf(SimpleGrantedAuthority("ROLE_${account.role}"))

            override fun getPassword(): String = account.password

            override fun getUsername(): String = account.username
            
            override fun isAccountNonExpired() = true
            
            override fun isAccountNonLocked() = true
            
            override fun isCredentialsNonExpired() = true
            
            override fun isEnabled() = true
        }
    }
}