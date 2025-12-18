package top.zztech.ainote.integration.sms

import com.aliyun.dypnsapi20170525.Client
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeRequest
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest
import org.redisson.api.RedissonClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service
import top.zztech.ainote.integration.sms.PnvsSmsProperties
import top.zztech.ainote.error.AccountException
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
@ConditionalOnBean(Client::class)
class SmsVerifyCodeService(
    private val pnvsSmsClient: Client,
    private val pnvsSmsProperties: PnvsSmsProperties,
    private val redissonClient: RedissonClient
) {

    fun send(scene: String, phoneNumber: String): SmsSendResult {
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
        request.smsUpExtendCode = scene
        request.outId = outId
        request.interval = pnvsSmsProperties.minSendIntervalSeconds.toLong()
        request.codeType = 1L

        val resp = pnvsSmsClient.sendSmsVerifyCode(request)

        if (resp?.body?.success != true || resp.body?.code != "OK") {
            throw AccountException.smsCodeIsError(resp?.body?.message ?: "短信验证码发送失败")
        }

        val outIdBucket = redissonClient.getBucket<String>(smsOutIdKey(scene, phoneNumber))
        outIdBucket.set(outId, pnvsSmsProperties.validTimeSeconds.toLong(), TimeUnit.SECONDS)

        freqBucket.set(
            "1",
            pnvsSmsProperties.minSendIntervalSeconds.toLong(),
            TimeUnit.SECONDS
        )

        return SmsSendResult(outId)
    }

    fun verify(scene: String, phoneNumber: String, code: String) {
        val outIdBucket = redissonClient.getBucket<String>(smsOutIdKey(scene, phoneNumber))
        val outId = outIdBucket.get() ?: throw AccountException.smsCodeExpired()

        val request = CheckSmsVerifyCodeRequest()
        request.phoneNumber = phoneNumber
        request.outId = outId
        request.verifyCode = code

        val resp = pnvsSmsClient.checkSmsVerifyCode(request)

        if (resp?.body?.success != true || resp.body?.code != "OK") {
            throw AccountException.smsCodeIsError(resp?.body?.message ?: "短信验证码核验失败")
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
