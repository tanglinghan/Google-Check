package top.b0x0.googlecheck.totp.util;

import java.util.UUID;

/**
 * @author TANG
 * @description 生成ID工具类
 * @date 2020-12-24
 */
public class UUIDUtil {

    public static String getUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
