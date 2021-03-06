package top.b0x0.googlecheck.totp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import top.b0x0.googlecheck.totp.common.Constants;
import top.b0x0.googlecheck.totp.common.RedisCacheKey;
import top.b0x0.googlecheck.totp.common.enums.RedisCacheEnum;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 工具类
 *
 * @author TANG
 */
public class TokenUtils {
    private static final Logger log = LoggerFactory.getLogger(TokenUtils.class);

    /**
     * 获取 tokenKey
     *
     * @param request /
     * @return /
     */
    public static String getTokenKey(HttpServletRequest request, RedisCacheEnum redisCacheEnum) {
        // 从请求头中获取以token为键的值
        String tokenKey = getTokenValue(request);
        log.info("tokenKey=" + tokenKey);
        if (redisCacheEnum.equals(RedisCacheEnum.GOOGLE)) {
            tokenKey = String.format(RedisCacheKey.TOKEN_KEY_GOOGLE, tokenKey);
        } else if (redisCacheEnum.equals(RedisCacheEnum.LOGIN)) {
            tokenKey = String.format(RedisCacheKey.TOKEN_KEY_LOGIN, tokenKey);
        }
        return tokenKey;
    }

    /**
     * 获取 token
     *
     * @param request /
     * @return /
     */
    public static String getTokenValue(HttpServletRequest request) {
        String token = request.getHeader(Constants.TOKEN);
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter(Constants.TOKEN);
        }
        return token;
    }


    private static final char[] HEX_CODE = "0123456789abcdef".toCharArray();

    /**
     * 登录账号以及当前时间戳 通过MD5摘要算法生成token
     *
     * @param param 登录账号
     * @return /
     */
    public static String encryptByMd5(String param) {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            //生成实现MD5摘要算法的 MessageDigest 对象
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            //重置摘要以供再次使用。
            algorithm.reset();
            //使用指定的字节更新摘要。
            algorithm.update((param + "-" + currentTimeMillis).getBytes());
            //通过运行诸如填充之类的终于操作完毕哈希计算
            byte[] messageDigest = algorithm.digest();
            //重新进行补码。
            return toHexString(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            log.error("生成Token失败: {}", e.getMessage());
        }
        return "";
    }

    public static String toHexString(byte[] data) {
        if (data == null) {
            return null;
        }
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(HEX_CODE[(b >> 4) & 0xF]);
            r.append(HEX_CODE[(b & 0xF)]);
        }
        return r.toString();
    }
}
