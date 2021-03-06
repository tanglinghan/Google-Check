package top.b0x0.googlecheck.totp.common.vo.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author TANG
 */
@Data
@ApiModel("账号注册对象")
public class RegisterReq {

    @ApiModelProperty(value = "手机号")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @ApiModelProperty(value = "登录密码")
    @NotBlank(message = "密码不能为空")
    private String password;

}
