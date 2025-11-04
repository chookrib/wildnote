package cool.done.wildnote.server.adapter.driven;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import cool.done.wildnote.server.application.ExtraLogService;
import cool.done.wildnote.server.domain.SmsGateway;
import cool.done.wildnote.server.utility.JsonUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信 Gateway 接口 Adapter
 */
@Component
public class AliyunSmsGatewayAdapter implements SmsGateway {

    private static final Logger logger = LoggerFactory.getLogger(AliyunSmsGatewayAdapter.class);

    @Value("${wildnote.sms-aliyun-key:}")
    private String smsAliyunKey;

    @Value("${wildnote.sms-aliyun-secret:}")
    private String smsAliyunSecret;

    @Value("${wildnote.sms-aliyun-sign:}")
    private String smsAliyunSign;

    @Value("${wildnote.sms-aliyun-template:}")
    private String smsAliyunTemplate;

    private final ExtraLogService extraLogService;

    public AliyunSmsGatewayAdapter(ExtraLogService extraLogService) {
        this.extraLogService = extraLogService;
    }

    @Override
    public void sendCode(String mobile, String code) {
        //使用 dysmsapi20180501 会报 PRODUCT_NOT_SUBSCRIBED 错误

        Config config = new Config()
                .setAccessKeyId(smsAliyunKey)
                .setAccessKeySecret(smsAliyunSecret);
        //config.endpoint = "dysmsapi.aliyuncs.com";
        //logger.info(smsAliyunSign);
        try {
            Client client = new Client(config);
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(mobile)
                    .setSignName(smsAliyunSign)                         // 签名
                    .setTemplateCode(smsAliyunTemplate)                 // 模板CODE
                    .setTemplateParam("{\"code\":\"" + code + "\"}")    // 模板变量
                    //.setSmsUpExtendCode("")                           // 上行短信扩展码
                    ;
            SendSmsResponse response = client.sendSms(request);
            //String requestId = response.body.requestId;
            extraLogService.logSmsInfo(
                    String.format(
                            "阿里云短信验证码发送结果: %s %s %s",
                            mobile,
                            code,
                            JsonUtility.serialize(response.body)
                    ), logger);
        } catch (Exception ex) {
            extraLogService.logSmsError(String.format("阿里云短信验异常: %s %s %s", mobile, code, ex.getMessage()), logger);
        }
    }
}
