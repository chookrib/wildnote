package cool.done.wildnote.adapter.driving;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置
 */
@Configuration
public class GlobalWebConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //拦截器
        InterceptorRegistration globalInterceptor = registry.addInterceptor(getGlobalInterceptor());

        //排除和拦截规则无需添加contextPath前缀
        //排除
        //globalInterceptor.excludePathPatterns("/error/**");
        //globalInterceptor.excludePathPatterns("/images/**");
        //globalInterceptor.excludePathPatterns("/scripts/**");
        //globalInterceptor.excludePathPatterns("/styles/**");
        //globalInterceptor.excludePathPatterns("/**/*.html");

        //排除所有地址
        //globalInterceptor.excludePathPatterns("/**");
        //排除登录
        globalInterceptor.excludePathPatterns("/login");
        //拦截所有地址
        globalInterceptor.addPathPatterns("/**");       //拦截所有地址
    }

    @Bean
    public GlobalInterceptor getGlobalInterceptor() {
        return new GlobalInterceptor();
    }
}
