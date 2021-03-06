package top.b0x0.googlecheck.totp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户类
 *
 * @author TANG
 */
@Data
@TableName("t_user")
public class User {
    /**
     * 用户ID ，唯一标识
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 手机号
     */
    private String phone;
    /**
     * 登录密码
     */
    private String password;

    /**
     * google 验证的 密钥
     */
    private String googleSecret;

}
