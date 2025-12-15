package top.zztech.ainote.service

import org.springframework.ai.chat.model.ChatModel
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.zztech.ainote.runtime.annotation.LogOperation

@RestController
@RequestMapping("/ai")
class AiService(
    private val chatModel: ChatModel
) {

    @PostMapping("/chat")
    @LogOperation(action = "AI_CHAT", entityType = "Ai", includeRequest = true)
    @PreAuthorize("isAuthenticated()")
    fun chat(@RequestBody req: AiChatRequest): AiChatResponse {
        val result = chatModel.call(req.prompt)
        return AiChatResponse(result)
    }
}

data class AiChatRequest(
    val prompt: String
)

data class AiChatResponse(
    val result: String
)
