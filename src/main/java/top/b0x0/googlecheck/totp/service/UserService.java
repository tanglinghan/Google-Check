package top.b0x0.googlecheck.totp.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.b0x0.googlecheck.totp.common.Constants;
import top.b0x0.googlecheck.totp.common.RedisCacheKey;
import top.b0x0.googlecheck.totp.common.enums.ApiResultEnum;
import top.b0x0.googlecheck.totp.common.enums.RedisCacheEnum;
import top.b0x0.googlecheck.totp.common.exception.ApiException;
import top.b0x0.googlecheck.totp.common.vo.GoogleDTO;
import top.b0x0.googlecheck.totp.common.vo.Result;
import top.b0x0.googlecheck.totp.common.vo.req.Login4GoogleReq;
import top.b0x0.googlecheck.totp.common.vo.req.LoginReq;
import top.b0x0.googlecheck.totp.common.vo.req.RegisterReq;
import top.b0x0.googlecheck.totp.entity.User;
import top.b0x0.googlecheck.totp.mapper.UserMapper;
import top.b0x0.googlecheck.totp.util.GoogleAuthenticatorUtils;
import top.b0x0.googlecheck.totp.util.TokenUtils;
import top.b0x0.googlecheck.totp.util.UUIDUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static top.b0x0.googlecheck.totp.util.TokenUtils.encryptByMd5;

/**
 * @author TANG
 */
@Service
public class UserService {

    private final static Logger log = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    UserMapper userMapper;

    /**
     * 获取缓存中的数据
     *
     * @return /
     */
    public Result getData() {
        Map<String, Object> data = new HashMap<>();
        setData(RedisCacheKey.REGISTER_USER_KEY, data);
        setData(RedisCacheKey.TOKEN_KEY_LOGIN_KEY, data);
        return Result.ok(data);
    }

    public void setData(String keyword, Map<String, Object> data) {
        Set<String> keys = redisTemplate.keys(keyword);
        assert keys != null;
        for (String key : keys) {
            data.put(key, redisTemplate.opsForValue().get(key));
        }
    }

    /**
     * 注册
     *
     * @param req /
     * @return /
     */
    public Result register(RegisterReq req) {
        log.info("注册用户信息: {}", JSON.toJSONString(req));

        User user = new User();
        BeanUtils.copyProperties(req, user);
        user.setId(UUIDUtil.getUuid());
        User selectOne = userMapper.selectOne(new QueryWrapper<User>().eq("phone", user.getPhone()));
        if (selectOne != null) {
            return Result.error(ApiResultEnum.USER_IS_EXIST);
        }
        int insert = userMapper.insert(user);
        if (insert > 0) {
            return Result.ok();
        }
        return Result.error();

    }

    /**
     * 从Redis中获取已注册用户
     *
     * @param phone 手机号
     * @return user
     */
    public User getCacheUser(String phone) {
        return (User) redisTemplate.opsForValue().get(String.format(RedisCacheKey.REGISTER_USER, phone));
    }


    /**
     * 更新token用户
     */
    public void updateCacheUser(User user, HttpServletRequest request) {
        if (user == null) {
            throw new ApiException(ApiResultEnum.ERROR_NULL);
        }
        redisTemplate.opsForValue().set(TokenUtils.getTokenKey(request, RedisCacheEnum.LOGIN), user, 1, TimeUnit.DAYS);
    }

    /**
     * 账号密码登录
     *
     * @param req /
     * @return /
     */
    public Result login(LoginReq req) {
        log.info("登录用户信息: {}", JSON.toJSONString(req));

        User user = selectUserByPhone(req.getPhone());
        if (!user.getPassword().equals(req.getPassword())) {
            return Result.error(ApiResultEnum.USERNAME_OR_PASSWORD_IS_WRONG);
        }
        String phone = req.getPhone();
        // 使用md5生成token
        String token = encryptByMd5(phone);
        String cacheKey = String.format(RedisCacheKey.TOKEN_KEY_LOGIN, token);
        redisTemplate.opsForValue().set(cacheKey, user, 30, TimeUnit.MINUTES);
        Map<String, Object> data = new HashMap<>(1);
        data.put(Constants.TOKEN, token);
        return Result.ok(data);
    }

    /**
     * 使用Google动态码登录
     *
     * @param req /
     * @return /
     * @ /
     */
    public Result login4Google(Login4GoogleReq req) {
        log.info("登录用户信息: {}", JSON.toJSONString(req));

        User user = selectUserByPhone(req.getPhone());
        if (!user.getPassword().equals(req.getPassword())) {
            return Result.error(ApiResultEnum.USERNAME_OR_PASSWORD_IS_WRONG);
        }
        if (StringUtils.isEmpty(user.getGoogleSecret())) {
            throw new ApiException(ApiResultEnum.GOOGLE_NOT_BIND);
        }
        boolean isTrue = GoogleAuthenticatorUtils.checkCode(user.getGoogleSecret(), req.getSecCode(), System.currentTimeMillis());
        if (!isTrue) {
            throw new ApiException(ApiResultEnum.GOOGLE_CODE_NOT_MATCH);
        }
        String phone = req.getPhone();
        // 使用md5生成token
        String token = encryptByMd5(phone);
        String cacheKey = String.format(RedisCacheKey.TOKEN_KEY_GOOGLE, token);
        redisTemplate.opsForValue().set(cacheKey, user, 120, TimeUnit.SECONDS);
        Map<String, Object> data = new HashMap<>(1);
        data.put(Constants.TOKEN, token);
        return Result.ok(data);
    }

    private User selectUserByPhone(String phone) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));
        if (user == null) {
            throw new ApiException(ApiResultEnum.USER_NOT_EXIST);
        }
        return user;
    }

    /**
     * 生成Google 密钥
     * secret：密钥
     * secretQrCode：Google Authenticator 扫描条形码的内容
     *
     * @param user /
     * @return <p>
     * {
     * "data": {
     * "secret": "TGQGCULWSTTHOHG77LN7BAGGDWYGG6TJ",
     * "secretQrCode": "otpauth://totp/%E5%B1%B1%E6%B2%B3%E5%9C%A8%3A17688888888?secret=TGQGCULWSTTHOHG77LN7BAGGDWYGG6TJ&issuer=%E5%B1%B1%E6%B2%B3%E5%9C%A8"
     * },
     * "message": "ok",
     * "status": 200
     * }
     * </p>
     */
    public Result generateGoogleSecret(User user) {
        //Google密钥
        String randomSecretKey = GoogleAuthenticatorUtils.createSecret();
        String googleAuthenticatorBarCode = GoogleAuthenticatorUtils.createQrUri(randomSecretKey, user.getPhone(), Constants.COMPANY_NAME);
        Map<String, Object> data = new HashMap<>();
        //Google密钥
        data.put("secret", randomSecretKey);
        //用户二维码内容
        data.put("secretQrCode", googleAuthenticatorBarCode);
        return Result.ok(data);
    }


    /**
     * 绑定Google验证器
     *
     * @param dto  /
     * @param user /
     * @return /
     */
    public Result bindGoogle(GoogleDTO dto, User user, HttpServletRequest request) {
        log.info("当前登录用户: {}", JSON.toJSONString(user));
        if (!StringUtils.isEmpty(user.getGoogleSecret())) {
            throw new ApiException(ApiResultEnum.GOOGLE_IS_BIND);
        }
        boolean isTrue = GoogleAuthenticatorUtils.checkCode(dto.getSecret(), dto.getCode(), System.currentTimeMillis());
        if (!isTrue) {
            throw new ApiException(ApiResultEnum.GOOGLE_CODE_NOT_MATCH);
        }
        User cacheUser = selectUserByPhone(user.getPhone());
        cacheUser.setGoogleSecret(dto.getSecret());
        userMapper.updateById(cacheUser);
        // 刷新缓存
        updateCacheUser(cacheUser, request);
        return Result.ok();
    }

    /**
     * Google登录
     *
     * @param opptCode 谷歌验证器生成的验证码
     * @param user     当前登录用户
     * @return /
     */
    public Result googleLogin(String opptCode, User user, HttpServletRequest request) {
        if (StringUtils.isEmpty(user.getGoogleSecret())) {
            throw new ApiException(ApiResultEnum.GOOGLE_NOT_BIND);
        }
        boolean isTrue = GoogleAuthenticatorUtils.checkCode(user.getGoogleSecret(), opptCode, System.currentTimeMillis());
        if (!isTrue) {
            throw new ApiException(ApiResultEnum.GOOGLE_CODE_NOT_MATCH);
        }
        redisTemplate.opsForValue().set(TokenUtils.getTokenKey(request, RedisCacheEnum.GOOGLE), Constants.SUCCESS, 1, TimeUnit.DAYS);
        return Result.ok();
    }


}
