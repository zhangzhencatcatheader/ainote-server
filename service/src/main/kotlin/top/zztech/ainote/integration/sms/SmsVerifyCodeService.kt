package top.zztech.ainote.integration.sms

import com.aliyun.dypnsapi20170525.Client
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeRequest
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service
import top.zztech.ainote.integration.sms.PnvsSmsProperties
import top.zztech.ainote.error.AccountException
import java.util.UUID
import java.time.Duration

@Service
@ConditionalOnBean(Client::class)
class SmsVerifyCodeService(
    private val pnvsSmsClient: Client,
    private val pnvsSmsProperties: PnvsSmsProperties,
    private val redissonClient: RedissonClient
) {

    private val log = LoggerFactory.getLogger(SmsVerifyCodeService::class.java)

    private val smsUpExtendCodeRegex = Regex("^[0-9]{1,12}$")

    fun send(scene: String, phoneNumber: String): SmsSendResult {
        if (scene.isBlank()) {
            throw AccountException.smsCodeIsError("scene 不能为空")
        }
        if (phoneNumber.isBlank()) {
            throw AccountException.smsCodeIsError("手机号不能为空")
        }
        if (pnvsSmsProperties.signName.isBlank() || pnvsSmsProperties.templateCode.isBlank()) {
            throw AccountException.smsCodeIsError("短信配置不完整，请检查 sign-name / template-code")
        }

        val freqKey = smsFreqKey(scene, phoneNumber)
        val freqBucket = redissonClient.getBucket<String>(freqKey)
        if (freqBucket.isExists) {
            throw AccountException.smsSendTooFrequent()
        }

        val outId = UUID.randomUUID().toString()

        val request = SendSmsVerifyCodeRequest()
        request.phoneNumber = phoneNumber
        request.signName = pnvsSmsProperties.signName
        request.templateCode = pnvsSmsProperties.templateCode
        request.templateParam = "{\"code\":\"##code##\",\"min\":\"${pnvsSmsProperties.validTimeSeconds / 60}\"}"
        request.codeLength = pnvsSmsProperties.codeLength.toLong()
        request.validTime = pnvsSmsProperties.validTimeSeconds.toLong()
        if (smsUpExtendCodeRegex.matches(scene)) {
            request.smsUpExtendCode = scene
        } else {
            log.debug(
                "忽略 smsUpExtendCode(scene={}), 因不满足阿里云扩展码格式(纯数字且长度1~12)",
                scene
            )
        }
        request.outId = outId
        request.interval = pnvsSmsProperties.minSendIntervalSeconds.toLong()
        request.codeType = 1L

        val resp = pnvsSmsClient.sendSmsVerifyCode(request)

        if (resp?.body?.success != true || resp.body?.code != "OK") {
            val code = resp?.body?.code
            val message = resp?.body?.message
            log.warn(
                "PNVS发送短信验证码失败, code={}, message={}, scene={}, phoneNumber={}, outId={}",
                code,
                message,
                scene,
                phoneNumber,
                outId
            )
            throw AccountException.smsCodeIsError(
                buildString {
                    append("短信验证码发送失败")
                    if (!code.isNullOrBlank()) append("(code=").append(code).append(')')
                    if (!message.isNullOrBlank()) append(": ").append(message)
                }
            )
        }

        val outIdBucket = redissonClient.getBucket<String>(smsOutIdKey(scene, phoneNumber))
        outIdBucket.set(outId, Duration.ofSeconds(pnvsSmsProperties.validTimeSeconds.toLong()))

        freqBucket.set(
            "1",
            Duration.ofSeconds(pnvsSmsProperties.minSendIntervalSeconds.toLong())
        )

        return SmsSendResult(outId)
    }

    fun verify(scene: String, phoneNumber: String, code: String) {
        if (scene.isBlank()) {
            throw AccountException.smsCodeIsError("scene 不能为空")
        }
        if (phoneNumber.isBlank()) {
            throw AccountException.smsCodeIsError("手机号不能为空")
        }
        if (code.isBlank()) {
            throw AccountException.smsCodeIsError("验证码不能为空")
        }

        val outIdBucket = redissonClient.getBucket<String>(smsOutIdKey(scene, phoneNumber))
        val outId = outIdBucket.get() ?: throw AccountException.smsCodeExpired()

        val request = CheckSmsVerifyCodeRequest()
        request.phoneNumber = phoneNumber
        request.outId = outId
        request.verifyCode = code

        val resp = pnvsSmsClient.checkSmsVerifyCode(request)

        if (resp?.body?.success != true || resp.body?.code != "OK") {
            val respCode = resp?.body?.code
            val message = resp?.body?.message
            log.warn(
                "PNVS核验短信验证码失败, code={}, message={}, scene={}, phoneNumber={}, outId={}",
                respCode,
                message,
                scene,
                phoneNumber,
                outId
            )
            throw AccountException.smsCodeIsError(
                buildString {
                    append("短信验证码核验失败")
                    if (!respCode.isNullOrBlank()) append("(code=").append(respCode).append(')')
                    if (!message.isNullOrBlank()) append(": ").append(message)
                }
            )
        }

        val verifyResult = resp.body?.model?.verifyResult
        if (verifyResult != "PASS") {
            throw AccountException.smsCodeIsError("短信验证码错误")
        }

        outIdBucket.delete()
    }

    private fun smsOutIdKey(scene: String, phoneNumber: String): String = "sms:outId:$scene:$phoneNumber"

    private fun smsFreqKey(scene: String, phoneNumber: String): String = "sms:freq:$scene:$phoneNumber"

    data class SmsSendResult(
        val outId: String
    )
}
