/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 对象存储服务 - 提供文件上传下载功能
 * OSS Service - Provides file upload and download functionality
 */

package top.zztech.ainote.service

import com.aliyun.oss.OSS
import com.aliyun.oss.model.PutObjectRequest
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import top.zztech.ainote.cfg.OssProperties
import top.zztech.ainote.model.StaticFile
import top.zztech.ainote.model.enums.FileType
import top.zztech.ainote.repository.StaticFileRepository
import top.zztech.ainote.runtime.annotation.LogOperation
import top.zztech.ainote.runtime.utility.getCurrentAccountId
import java.io.InputStream
import java.util.*

/**
 * 对象存储服务
 * 提供基于阿里云OSS的文件上传、删除和URL获取功能
 */

@RestController
@RequestMapping("/file")
@Transactional
@ConditionalOnBean(OSS::class)
class OssService(
    @Autowired(required = false) private val ossClient: OSS?,
    private val ossProperties: OssProperties,
    private val staticFileRepository: StaticFileRepository
) {
    /**
     * 上传文件（从 MultipartFile）
     * @param file 多部分文件对象
     * @param folder 存储文件夹名称，默认为空
     * @return 文件信息对象
     */
    @LogOperation(action = "UPLOAD_FILE", entityType = "StaticFile", includeRequest = true)
    @PostMapping("/upload")
    fun uploadFile(file: MultipartFile, @RequestParam(defaultValue = "") folder: String): StaticFile {
        if (ossClient == null) {
            throw IllegalStateException("OSS服务未配置，无法上传文件")
        }
        
        val originalFileName = file.originalFilename ?: "file"
        val fileName = generateFileName(originalFileName, folder)
        val filePath = if (folder.isNotEmpty()) "$folder/$fileName" else fileName
        
        // 上传到OSS
        ossClient.putObject(
            PutObjectRequest(
                ossProperties.bucketName,
                filePath,
                file.inputStream
            )
        )
        
        // 保存到数据库
        val uploaderId = try { getCurrentAccountId() } catch (e: Exception) { null }
        val fileUrl = getFileUrl(filePath)
        val mimeType = file.contentType
        val fileType = detectFileType(mimeType, originalFileName)
        
        val staticFile = StaticFile {
            this.fileName = fileName
            this.originalName = originalFileName
            this.filePath = filePath
            this.fileSize = file.size
            this.mimeType = mimeType
            this.fileType = fileType
            this.uploaderId = uploaderId
        }
        
        return staticFileRepository.saveCommand(staticFile, SaveMode.INSERT_ONLY).execute().modifiedEntity
    }

    /**
     * 根据ID获取文件信息
     * @param id 文件ID
     * @return 文件信息
     */
    @LogOperation(action = "GET_FILE", entityType = "StaticFile", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    fun getFileById(@PathVariable id: UUID): StaticFile? =
        staticFileRepository.findById(id)

    /**
     * 获取所有文件
     * @return 文件列表
     */
    @LogOperation(action = "LIST_FILES", entityType = "StaticFile", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun getAllFiles(): List<StaticFile> =
        staticFileRepository.findAll()

    /**
     * 根据上传者ID获取文件
     * @param uploaderId 上传者ID
     * @return 文件列表
     */
    @LogOperation(action = "LIST_FILES_BY_UPLOADER", entityType = "StaticFile", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/uploader/{uploaderId}")
    fun getFilesByUploaderId(@PathVariable uploaderId: UUID): List<StaticFile> =
        staticFileRepository.findByUploaderId(uploaderId)

    /**
     * 根据文件类型获取文件
     * @param fileType 文件类型
     * @return 文件列表
     */
    @LogOperation(action = "LIST_FILES_BY_TYPE", entityType = "StaticFile", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/type/{fileType}")
    fun getFilesByType(@PathVariable fileType: FileType): List<StaticFile> =
        staticFileRepository.findByFileType(fileType)

    /**
     * 搜索文件
     * @param keyword 关键词
     * @return 文件列表
     */
    @LogOperation(action = "SEARCH_FILES", entityType = "StaticFile", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    fun searchFiles(@RequestParam keyword: String): List<StaticFile> =
        staticFileRepository.searchByFileName("%$keyword%")

    /**
     * 获取最新文件
     * @param limit 限制数量
     * @return 文件列表
     */
    @LogOperation(action = "LIST_LATEST_FILES", entityType = "StaticFile", includeRequest = false)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/latest")
    fun getLatestFiles(@RequestParam(defaultValue = "10") limit: Int): List<StaticFile> =
        staticFileRepository.findLatestFiles(limit)

    /**
     * 上传文件（从InputStream）
     * @param inputStream 输入流
     * @param originalFileName 原始文件名
     * @param folder 存储文件夹名称，默认为空
     * @return 文件的公网访问URL
     */
    fun uploadFile(inputStream: InputStream, originalFileName: String, folder: String = ""): String {
        if (ossClient == null) {
            throw IllegalStateException("OSS服务未配置，无法上传文件")
        }
        
        val fileName = generateFileName(originalFileName, folder)
        
        ossClient.putObject(
            PutObjectRequest(
                ossProperties.bucketName,
                fileName,
                inputStream
            )
        )
        
        return getFileUrl(fileName)
    }

    /**
     * 删除文件
     * @param id 文件ID
     */
    @LogOperation(action = "DELETE_FILE", entityType = "StaticFile", includeRequest = true)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteFile(@PathVariable id: UUID) {
        if (ossClient == null) {
            throw IllegalStateException("OSS服务未配置，无法删除文件")
        }
        
        val file = staticFileRepository.findById(id)
        if (file != null) {
            // 从 OSS 删除
            ossClient.deleteObject(ossProperties.bucketName, file.filePath)
            // 从数据库删除
            staticFileRepository.deleteById(id)
        }
    }

    /**
     * 获取文件的公网访问URL
     * @param fileName 文件名（包含路径）
     * @return 完整的文件访问URL
     */
    fun getFileUrl(fileName: String): String {
        return "https://${ossProperties.bucketName}.${ossProperties.endpoint.removePrefix("https://").removePrefix("http://")}/$fileName"
    }

    /**
     * 生成唯一的文件名
     * 格式：timestamp-uuid.extension
     * @param originalFileName 原始文件名
     * @param folder 文件夹名称
     * @return 生成的文件名
     */
    private fun generateFileName(originalFileName: String, folder: String): String {
        val extension = originalFileName.substringAfterLast(".", "")
        val uuid = UUID.randomUUID().toString().replace("-", "")
        val timestamp = System.currentTimeMillis()
        val fileName = "$timestamp-$uuid.$extension"
        return fileName
    }

    /**
     * 根据MIME类型和文件名检测文件类型
     * @param mimeType MIME类型
     * @param fileName 文件名
     * @return 文件类型枚举
     */
    private fun detectFileType(mimeType: String?, fileName: String): FileType {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        
        return when {
            mimeType?.startsWith("image/") == true -> FileType.IMAGE
            mimeType?.startsWith("video/") == true -> FileType.VIDEO
            mimeType?.startsWith("audio/") == true -> FileType.AUDIO
            extension in listOf("pdf", "doc", "docx", "txt", "md") -> FileType.DOCUMENT
            extension in listOf("xls", "xlsx", "csv") -> FileType.SPREADSHEET
            extension in listOf("ppt", "pptx", "key") -> FileType.PRESENTATION
            extension in listOf("zip", "rar", "7z", "tar", "gz") -> FileType.ARCHIVE
            extension in listOf("java", "kt", "js", "html", "css", "json", "xml", "py", "cpp", "c", "h") -> FileType.CODE
            else -> FileType.OTHER
        }
    }
}
