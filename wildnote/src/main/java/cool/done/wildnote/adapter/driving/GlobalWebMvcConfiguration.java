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
public class GlobalWebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //拦截器
        InterceptorRegistration globalInterceptor = registry.addInterceptor(getGlobalHandlerInterceptor());

        //排除
        //globalInterceptor.excludePathPatterns("/error/**");
        //globalInterceptor.excludePathPatterns("/images/**");
        //globalInterceptor.excludePathPatterns("/scripts/**");
        //globalInterceptor.excludePathPatterns("/styles/**");
        //globalInterceptor.excludePathPatterns("/**/*.html");

        //排除所有地址
        //globalInterceptor.excludePathPatterns("/**");

        //拦截地址
        globalInterceptor.addPathPatterns("/api/**");
    }

    @Bean
    public GlobalHandlerInterceptor getGlobalHandlerInterceptor() {
        return new GlobalHandlerInterceptor();
    }
}
