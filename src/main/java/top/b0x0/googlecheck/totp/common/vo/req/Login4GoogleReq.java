package top.b0x0.googlecheck.totp.common.vo.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author TANG
 */
@Data
@ApiModel("使用动态码登录请求对象")
public class Login4GoogleReq {

    @ApiModelProperty(value = "手机号")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @ApiModelProperty(value = "登录密码")
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty(value = "动态码")
    @NotBlank(message = "动态码不能为空")
    private String secCode;

}
