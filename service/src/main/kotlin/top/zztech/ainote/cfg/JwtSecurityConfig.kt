
package top.zztech.ainote.cfg

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import top.zztech.ainote.runtime.utility.JwtAuthenticationFilter


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class JwtSecurityConfig(val jwtAuthenticationFilter: JwtAuthenticationFilter) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf: CsrfConfigurer<HttpSecurity> -> csrf.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            .cors {
                it.configurationSource {
                    CorsConfiguration().apply {
                        addAllowedOrigin("*")
                        addAllowedMethod("*")
                        addAllowedHeader("*")
                        allowCredentials = false
                    }
                }
            }
            .sessionManagement { session: SessionManagementConfigurer<HttpSecurity> ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            }
            .authorizeHttpRequests { auth: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry ->
                auth
                    // 公开访问：登录和注册接口
                    .requestMatchers("/auth/login", "/auth/register").permitAll()
                    // 公开访问：OpenAPI 文档和客户端
                    .requestMatchers("/openapi.html", "/openapi.yml", "/ts.zip").permitAll()
                    // 可选认证：OSS上传接口（有无token都可以）
                    .requestMatchers("/file/upload").permitAll()
                    // 其他所有接口需要 ADMIN 角色
                    .anyRequest().hasRole("ADMIN")
            }
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.getAuthenticationManager()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}