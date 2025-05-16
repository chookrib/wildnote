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

    private static final Logger log = LoggerFactory.getLogger(WildnoteSvcApplication.class);

    public static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(WildnoteSvcApplication.class, args);
        log.info("Started File-Name: {} Build-Time: {}", getFileName(), getBuildTime());
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
                return "META-INF/MANIFEST.MF 不存在";
            }

            Properties props = new Properties();
            props.load(inputStream);

            for (String key : props.stringPropertyNames()) {
                if(key.equals("Build-Time")) {
                    return props.getProperty(key);
                }
            }

            return "META-INF/MANIFEST.MF 未包含 Build-Time";
        } catch (Exception e) {
            return String.format("META-INF/MANIFEST.MF 获取 Build-Time 失败 %s", e.getMessage());
        }
    }
}
