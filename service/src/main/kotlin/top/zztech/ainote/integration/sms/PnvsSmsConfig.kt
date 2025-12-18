package top.zztech.ainote.integration.sms

import com.aliyun.dypnsapi20170525.Client
import com.aliyun.teaopenapi.models.Config
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "aliyun.pnvs.sms")
class PnvsSmsProperties {
    var region: String = "cn-shanghai"
    var endpoint: String = "dypnsapi.aliyuncs.com"

    var accessKeyId: String = ""
    var accessKeySecret: String = ""

    var signName: String = ""
    var templateCode: String = ""

    var codeLength: Int = 4
    var validTimeSeconds: Int = 300
    var minSendIntervalSeconds: Int = 60
}

@Configuration
class PnvsSmsConfig {

    @Bean
    @ConditionalOnProperty(prefix = "aliyun.pnvs.sms", name = ["access-key-id", "access-key-secret"])
    fun pnvsSmsClient(properties: PnvsSmsProperties): Client {
        if (properties.accessKeyId.isBlank() || properties.accessKeySecret.isBlank()) {
            throw IllegalStateException("PNVS短信配置不完整，请检查 access-key-id 和 access-key-secret")
        }

        val config = Config()
        config.accessKeyId = properties.accessKeyId
        config.accessKeySecret = properties.accessKeySecret
        config.endpoint = properties.endpoint
        return Client(config)
    }
}
