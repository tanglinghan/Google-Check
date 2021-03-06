package top.b0x0.googlecheck.totp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.b0x0.googlecheck.totp.interceptor.LoginIntercept;

@Slf4j
@Component
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginIntercept loginIntercept;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("添加拦截");
        registry.addInterceptor(loginIntercept);
    }
}
