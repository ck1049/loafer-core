package com.loafer.core.config;

import com.loafer.core.interceptor.LoginInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final String[] excludePatterns = {
            "/doc.html", // swagger文档
            "/swagger-resources/**",
            "/webjars/**",
            "/v2/**",
            "/user/**", // 用户相关
            "/error/**"
    };
    @Resource(name = "loginInterceptor")
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludePatterns);

        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void postHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, ModelAndView modelAndView) throws Exception {
                // 全局response header跨域处理
                response.setHeader("Access-Control-Expose-Headers", "*");
            }
        }).addPathPatterns("/**").excludePathPatterns(excludePatterns);
    }


}
