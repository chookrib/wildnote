package cool.done.wildnote.server.adapter.driven;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import cool.done.wildnote.server.domain.SmsHandler;
import cool.done.wildnote.server.utility.JacksonUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 阿里云短信处理器
 */
@Component
public class AliyunSmsHandler implements SmsHandler {
    private static final Logger logger = LoggerFactory.getLogger(AliyunSmsHandler.class);

    //@Value("${wildnote.sms-url:}")
    //private String smsUrl;

    private final String smsLogPath;

    @Value("${wildnote.sms-aliyun-key:}")
    private String smsAliyunKey;

    @Value("${wildnote.sms-aliyun-secret:}")
    private String smsAliyunSecret;

    @Value("${wildnote.sms-aliyun-sign:}")
    private String smsAliyunSign;

    @Value("${wildnote.sms-aliyun-template:}")
    private String smsAliyunTemplate;

    public AliyunSmsHandler(@Value("${wildnote.sms-log-path:}") String smsLogPath) {
        this.smsLogPath = smsLogPath;
        if (!smsLogPath.isEmpty()) {
            File smsLogDir = new File(smsLogPath).getParentFile();
            if (smsLogDir != null && !smsLogDir.exists()) {
                smsLogDir.mkdirs();
            }
        }
    }

    @Override
    public void sendCode(String mobile, String code) {

        //String log = "";
        //try {
        //    String url = smsUrl.replace("<mobile>", mobile).replace("<message>", message);
        //    new RestTemplate().getForObject(url, String.class);
        //    log = String.format("短信发送成功: %s %s", mobile, message);
        //}
        //catch (Exception e) {
        //    log = String.format("短信发送失败: %s %s", mobile, e.getMessage());
        //}
        //
        //try {
        //    FileWriter fileWriter = new FileWriter(smsLogPath, StandardCharsets.UTF_8, true);
        //    fileWriter.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + log + "\n");
        //    fileWriter.close();
        //}
        //catch (Exception e) {
        //    logger.error("短信发送写日志失败: {} {}", log, e.getMessage());
        //}

        //使用dysmsapi20180501会报PRODUCT_NOT_SUBSCRIBED错误

        String log = "";
        Config config = new Config()
                .setAccessKeyId(smsAliyunKey)
                .setAccessKeySecret(smsAliyunSecret)
                ;
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
            log = String.format("验证码短信发送结果: %s %s %s", mobile, code, JacksonUtility.writeValueAsString(response.body));
        }
        catch (Exception e) {
            log = String.format("验证码短信发送失败: %s %s", mobile, e.getMessage());
        }

        try {
            FileWriter fileWriter = new FileWriter(smsLogPath, StandardCharsets.UTF_8, true);
            fileWriter.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + log + "\n");
            fileWriter.close();
        }
        catch (Exception e) {
            logger.error("验证码短信发送写日志失败: {} {}", log, e.getMessage());
        }
    }
}
