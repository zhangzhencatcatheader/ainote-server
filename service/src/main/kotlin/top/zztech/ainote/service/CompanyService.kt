package top.zztech.ainote.service

import org.babyfish.jimmer.Page
import org.babyfish.jimmer.client.FetchBy
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import top.zztech.ainote.error.AccountException
import top.zztech.ainote.model.Company
import top.zztech.ainote.model.by
import top.zztech.ainote.repository.CompanyRepository
import top.zztech.ainote.runtime.annotation.LogOperation
import top.zztech.ainote.runtime.utility.getCurrentAccountId
import top.zztech.ainote.service.dto.*
import java.util.UUID
import kotlin.random.Random

@RestController
@RequestMapping("/company")
class CompanyService(
    private val companyRepository: CompanyRepository
) {
    /**
     * 获取全部企业
     */

    @GetMapping("/page")
    @LogOperation(action = "QUERY_COMPANY_PAGE", entityType = "Company", includeRequest = true)
    @PreAuthorize("hasRole('ADMIN')")
    fun pageCompany(
        @RequestParam(defaultValue = "0") pageIndex: Int,
        @RequestParam(defaultValue = "5") pageSize: Int,
        @RequestParam(defaultValue = "name asc, createdTime desc") sortCode: String,
        search: CompanySearch
    ): Page<@FetchBy("LIST_COMPANY") Company> =
        companyRepository.findAllPage(pageIndex, pageSize, sortCode, search, LIST_COMPANY)
    /**
     * 管理员添加企业
     */
    @PostMapping("/add")
    @LogOperation(action = "ADD_COMPANY", entityType = "Company", includeRequest = true)
    @PreAuthorize("hasRole('ADMIN')")
    fun add(
        input: CompanyAddInput
    ): UUID {
        // 生成随机的 tenant，6位数字
        val randomTenant = Random.nextInt(100000, 999999).toString()
        // 直接保存并设置 tenant
        return companyRepository.save( input.copy(tenant = randomTenant)) {
            setMode(SaveMode.INSERT_ONLY)
        }.modifiedEntity.id
    }

    /**
     * 管理员删除企业
     */
    @PostMapping("/delete")
    @LogOperation(action = "DELETE_COMPANY", entityType = "Company", includeRequest = true)
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(
        id: UUID
    ) {
        companyRepository.deleteById(id)
    }

    /**
     *
     * 查询我加入的企业
     *
     */
    @GetMapping("/my")
    @LogOperation(action = "QUERY_MY_COMPANY", entityType = "Company", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    fun myCompany(): List<@FetchBy("COMPANY_NAME") Company> {
    val currentUserId = getCurrentAccountId()
        ?: throw AccountException.usernameDoesNotExist()
       return companyRepository.findAllByAccountId(currentUserId, COMPANY_NAME)
    }

    @GetMapping("/names")
    @LogOperation(action = "QUERY_COMPANY_NAMES", entityType = "Company", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    fun allCompanyNames(): List<@FetchBy("COMPANY_NAME") Company> =
        companyRepository.findAll(COMPANY_NAME)

    /**
     * 切换企业
     */



    companion object {
        private  val LIST_COMPANY = newFetcher(Company::class).by {
            name()
            code()
            address()
            tenant()
            contact()
        }

        private val COMPANY_NAME = newFetcher(Company::class).by {
            name()
        }
    }
}