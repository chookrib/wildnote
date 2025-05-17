package cool.done.wildnotesvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
public class WildnoteSvcApplication {

    private static final Logger logger = LoggerFactory.getLogger(WildnoteSvcApplication.class);

    public static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(WildnoteSvcApplication.class, args);
        logger.info("Started File-Name: {} Build-Time: {}", getFileName(), getBuildTime());
    }

    /**
     * 取应用文件名
     */
    public static String getFileName() {
        return new ApplicationHome(applicationContext.getClass()).getSource().getName();
    }

    /**
     * 从MANIFEST.MF取应用打包时间
     */
    public static String getBuildTime() {
        try {
            InputStream inputStream = applicationContext.getClass().getClassLoader()
                    .getResourceAsStream("META-INF/MANIFEST.MF");

            if(inputStream == null) {
                logger.warn("获取 Build-Time 失败: META-INF/MANIFEST.MF 文件不存在");
                return "";
            }

            Properties props = new Properties();
            props.load(inputStream);

            for (String key : props.stringPropertyNames()) {
                if(key.equals("Build-Time")) {
                    return props.getProperty(key);
                }
            }

            logger.warn("获取 Build-Time 失败: META-INF/MANIFEST.MF 文件未包含 Build-Time");
            return "";
        } catch (Exception e) {
            logger.warn("获取 Build-Time 失败: {}", e.getMessage());
            return "";
        }
    }
}
