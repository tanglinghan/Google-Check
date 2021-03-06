package top.b0x0.googlecheck.totp.common;

/**
 * @author TANG
 */
public class RedisCacheKey {

    public static final String KEY_PREFIX = "top:b0x0:";
    /**
     * 登录生成的token key
     */
    public static final String TOKEN_KEY_LOGIN = KEY_PREFIX + "token:login:%s";

    /**
     * google验证保存的状态 key
     */
    public static final String TOKEN_KEY_GOOGLE = KEY_PREFIX + "token:google:%s";


    /**
     * 注册的用户全部放redis缓存中
     */
    public static final String REGISTER_USER = KEY_PREFIX + "register:user:%s";

    /**
     * 通配符
     */
    public static final String REGISTER_USER_KEY = KEY_PREFIX + "register:user:*";

    public static final String TOKEN_KEY_LOGIN_KEY = KEY_PREFIX + "token:login:*";

}
