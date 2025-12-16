package top.zztech.ainote.service

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.files.FileCreateParams
import com.openai.models.files.FilePurpose
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.zztech.ainote.runtime.annotation.LogOperation
import top.zztech.ainote.repository.StaticFileRepository
import top.zztech.ainote.service.dto.CreateTemplate
import top.zztech.ainote.service.dto.CreateTemplateField
import java.net.URI
import java.nio.file.Files
import kotlin.io.path.writeBytes

@RestController
@RequestMapping("/ai")
class AiService(
    private val chatModel: ChatModel,
    private val staticFileRepository: StaticFileRepository,
    private val environment: Environment
) {

    private val bailianBaseUrl: String = environment.getProperty("DASHSCOPE_BASE_URL")
        ?: System.getenv("DASHSCOPE_BASE_URL")
        ?: "https://dashscope.aliyuncs.com/compatible-mode/v1"

    private val qwenLongModel: String = environment.getProperty("DASHSCOPE_LONG_MODEL")
        ?: System.getenv("DASHSCOPE_LONG_MODEL")
        ?: "qwen-long"

    private val dashscopeApiKey: String = environment.getProperty("DASHSCOPE_API_KEY")
        ?: System.getenv("DASHSCOPE_API_KEY")
        ?: ""

    private val bailianClient: OpenAIClient by lazy {
        if (dashscopeApiKey.isBlank()) {
            throw IllegalStateException("DASHSCOPE_API_KEY 未配置")
        }
        OpenAIOkHttpClient.builder()
            .apiKey(dashscopeApiKey)
            .baseUrl(bailianBaseUrl)
            .build()
    }

    @PostMapping("/chat")
    @LogOperation(action = "AI_CHAT", entityType = "Ai", includeRequest = true)
    @PreAuthorize("isAuthenticated()")
    fun chat(@RequestBody req: AiChatRequest): AiChatResponse {
        val result = chatModel.call(req.prompt)
        return AiChatResponse(result)
    }

    @PostMapping("/template-fields")
    @LogOperation(action = "AI_GENERATE_TEMPLATE_FIELDS", entityType = "LedgerTemplateField", includeRequest = true)
    @PreAuthorize("isAuthenticated()")
    fun generateTemplateFields(@RequestBody input: CreateTemplate): List<CreateTemplateField> {
        return generateTemplateFieldsByAi(input)
    }

    fun generateTemplateFieldsByAi(input: CreateTemplate): List<CreateTemplateField> {
        val converter = BeanOutputConverter(object : ParameterizedTypeReference<List<CreateTemplateField>>() {})

        val systemPrompt = buildString {
            appendLine("你是一个业务建模助手。根据给定的台账模板信息与引用文档，生成一组字段定义。")
            appendLine("要求：")
            appendLine("1) 只输出最终结果，不要输出任何解释、Markdown 或代码块标记。")
            appendLine("2) 输出必须满足以下结构化格式要求：")
            appendLine(converter.format)
            appendLine("3) fieldType 只能使用以下枚举之一：TEXT,TEXTAREA,NUMBER,INTEGER,DECIMAL,DATE,DATETIME,TIME,BOOLEAN,SELECT,MULTISELECT,FILE,EMAIL,PHONE,URL")
            appendLine("4) fieldName 使用英文 snake_case；fieldLabel 使用中文；sortOrder 从 0 递增。")
            appendLine("5) 如果 fieldType 是 SELECT 或 MULTISELECT，则 fieldOptions 输出 JSON 字符串数组序列化后的字符串，例如: [\"选项1\",\"选项2\"]。")
            appendLine("补充模板信息：")
            appendLine("name: ${input.name}")
            appendLine("description: ${input.description ?: ""}")
            appendLine("category: ${input.category ?: ""}")
        }

        val userPrompt = "请基于模板信息与引用文档内容，生成一组台账模板字段定义。"

        val fileId = input.fileId
        if (fileId == null) {
            return callChatModelFallback(systemPrompt, userPrompt, converter)
        }

        // fileId 路径：优先使用百炼文件上传 + fileid:// 引用。
        // 如果任一步失败，则回退到纯文本（避免接口不可用）。
        return try {
            val staticFile = staticFileRepository.findById(fileId)
                ?: throw IllegalArgumentException("找不到对应的静态文件: $fileId")

            val fileBytes = URI(staticFile.filePath).toURL().openStream().use { it.readBytes() }

            val suffix = "." + staticFile.originalName.substringAfterLast('.', "bin")
            val tmpPath = Files.createTempFile("ainote-template-", suffix)
            tmpPath.writeBytes(fileBytes)

            val bailianFileObject = try {
                val fileParams = FileCreateParams.builder()
                    .file(tmpPath)
                    .purpose(FilePurpose.of("file-extract"))
                    .build()
                bailianClient.files().create(fileParams)
            } finally {
                try {
                    Files.deleteIfExists(tmpPath)
                } catch (_: Exception) {
                }
            }

            val bailianFileId = bailianFileObject.id()
            val chatParams = ChatCompletionCreateParams.builder()
                .addSystemMessage(systemPrompt)
                .addSystemMessage("fileid://$bailianFileId")
                .addUserMessage(userPrompt)
                .model(qwenLongModel)
                .build()

            val completion = bailianClient.chat().completions().create(chatParams)
            val content = completion.choices().firstOrNull()?.message()?.content()?.orElse("") ?: ""
            converter.convert(content) ?: emptyList()
        } catch (_: Exception) {
            callChatModelFallback(systemPrompt, userPrompt, converter)
        }
    }

    private fun callChatModelFallback(
        systemPrompt: String,
        userPrompt: String,
        converter: BeanOutputConverter<List<CreateTemplateField>>
    ): List<CreateTemplateField> {
        val result = chatModel.call(systemPrompt + "\n" + userPrompt)
        return try {
            converter.convert(result) ?: emptyList()
        } catch (ex: Exception) {
            throw IllegalArgumentException("AI 返回内容不是合法的 CreateTemplateField JSON 数组: ${ex.message}")
        }
    }
}

data class AiChatRequest(
    val prompt: String
)

data class AiChatResponse(
    val result: String
)
