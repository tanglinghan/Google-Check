package top.b0x0.googlecheck.totp.interceptor;

import top.b0x0.googlecheck.totp.common.enums.ApiResultEnum;
import top.b0x0.googlecheck.totp.common.Constants;
import top.b0x0.googlecheck.totp.entity.User;
import top.b0x0.googlecheck.totp.util.TokenUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import top.b0x0.googlecheck.totp.annotation.NeedLogin;
import top.b0x0.googlecheck.totp.common.enums.RedisCacheEnum;
import top.b0x0.googlecheck.totp.common.vo.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 登录拦截
 * @author TANG
 */
@Slf4j
@Component
public class LoginIntercept extends HandlerInterceptorAdapter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*
         * isAssignableFrom() 判定此 Class 对象所表示的类或接口与指定的 Class 参数所表示的类或接口是否相同，或是否是其超类或超接口
         * isAssignableFrom 与instanceof 区别
         * isAssignableFrom()方法是判断是否为某个类的父类
         * instanceof 关键字是判断是否某个类的子类
         */
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            //HandlerMethod 封装方法定义相关的信息,如类,方法,参数等
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            NeedLogin needLogin = getTagAnnotation(method, NeedLogin.class);
            if (needLogin != null) {
                //登录校验
                if (needLogin.login() && !isLogin(request, RedisCacheEnum.LOGIN)) {
                    responseOut(response, Result.error(ApiResultEnum.AUTH_LOGIN_NOT_VALID));
                    return false;
                }
                //google校验
                if (needLogin.google() && !isGoogle(request, RedisCacheEnum.GOOGLE)) {
                    responseOut(response, Result.error(ApiResultEnum.AUTH_GOOGLE_NOT_FOUND));
                    return false;
                }
            }

        }
        return super.preHandle(request, response, handler);
    }

    /**
     * 检查是否 登录或者google验证
     *
     * @param request /
     * @param redisCacheEnum /
     * @return /
     */
    public boolean isGoogle(HttpServletRequest request, RedisCacheEnum redisCacheEnum) {
        String tokenKey = TokenUtils.getTokenKey(request, redisCacheEnum);
        String googleStatus = (String) redisTemplate.opsForValue().get(tokenKey);
        return googleStatus != null && googleStatus.equalsIgnoreCase(Constants.SUCCESS);
    }

    /**
     * 是否登录
     *
     * @param request /
     * @param redisCacheEnum /
     * @return /
     */
    public boolean isLogin(HttpServletRequest request, RedisCacheEnum redisCacheEnum) {
        String tokenKey = TokenUtils.getTokenKey(request, redisCacheEnum);
        User user = (User) redisTemplate.opsForValue().get(tokenKey);
        return user != null;
    }


    /**
     * 获取目标注解
     * 如果方法上有注解就返回方法上的注解配置，否则类上的
     *
     * @param method   /
     * @param annotationClass /
     * @param <A> /
     * @return /
     */
    public <A extends Annotation> A getTagAnnotation(Method method, Class<A> annotationClass) {
        // 获取方法中是否包含注解
        Annotation methodAnnotate = method.getAnnotation(annotationClass);
        //获取 类中是否包含注解，也就是controller 是否有注解
        Annotation classAnnotate = method.getDeclaringClass().getAnnotation(annotationClass);
        return (A) (methodAnnotate != null ? methodAnnotate : classAnnotate);
    }

    /**
     * 回写给客户端
     *
     * @param response /
     * @param result /
     * @throws IOException /
     */
    private void responseOut(HttpServletResponse response, Result result) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        String json = JSONObject.toJSON(result).toString();
        out = response.getWriter();
        out.append(json);
    }

}
