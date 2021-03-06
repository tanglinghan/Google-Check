package top.b0x0.googlecheck.totp.controller.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.b0x0.googlecheck.totp.common.enums.ApiResultEnum;
import top.b0x0.googlecheck.totp.common.enums.RedisCacheEnum;
import top.b0x0.googlecheck.totp.common.exception.ApiException;
import top.b0x0.googlecheck.totp.entity.User;
import top.b0x0.googlecheck.totp.service.UserService;
import top.b0x0.googlecheck.totp.util.TokenUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 基类
 *
 * @author TANG
 */
public class BaseController {
    @Resource
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    /**
     * 从token 中获取用户信息
     *
     * @return /
     */
    protected User getUser() {
        String tokenKey = TokenUtils.getTokenKey(this.getRequest(), RedisCacheEnum.LOGIN);
        User user = (User) redisTemplate.opsForValue().get(tokenKey);
        if (user == null) {
            throw new ApiException(ApiResultEnum.AUTH_LOGIN_NOT_VALID);
        }
        return user;
    }
}
