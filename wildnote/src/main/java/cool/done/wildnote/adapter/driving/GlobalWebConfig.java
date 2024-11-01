package cool.done.wildnote.adapter.driving;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 */
@Configuration
public class GlobalWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //拦截器
        InterceptorRegistration globalInterceptor = registry.addInterceptor(getGlobalHandlerInterceptor());

        //排除
        //globalInterceptor.excludePathPatterns("/img/**");
        //globalInterceptor.excludePathPatterns("/js/**");
        //globalInterceptor.excludePathPatterns("/css/**");
        //globalInterceptor.excludePathPatterns("/**/*.html");

        //排除所有地址
        //globalInterceptor.excludePathPatterns("/**");

        //排除地址
        globalInterceptor.excludePathPatterns("/api/login");

        //拦截地址
        globalInterceptor.addPathPatterns("/api/**");
    }

    @Bean
    public GlobalWebInterceptor getGlobalHandlerInterceptor() {
        return new GlobalWebInterceptor();
    }

    /**
     * 跨域配置
     * 此方式预检请求OPTIONS会先被拦截器拦截，allowedMethods改为*也有同样问题
     * 使用@CrossOrigin注解也有同样问题
     * 改为使用CorsFilter实现
     */
    /*@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                //.allowedMethods("*")
                .allowedMethods("GET", "POST")
                ;
    }*/
}
