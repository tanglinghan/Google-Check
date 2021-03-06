package top.b0x0.googlecheck.totp.common.vo;

import lombok.Data;

/**
 * @author TANG
 */
@Data
public class GoogleDTO {
    /**
     * google密钥
     */
    private String secret;
    /**
     * 手机上显示的验证码
     */
    private String code;
}
