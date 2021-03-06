package top.b0x0.googlecheck.totp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import top.b0x0.googlecheck.totp.common.vo.Result;
import top.b0x0.googlecheck.totp.common.vo.req.Login4GoogleReq;
import top.b0x0.googlecheck.totp.common.vo.req.LoginReq;
import top.b0x0.googlecheck.totp.service.UserService;

import javax.annotation.Resource;

/**
 * @author TANG
 * @date 2021-0305
 */
@Api(tags = "登录模块")
@RestController
@Validated
public class LoginController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    @ApiOperation("普通登录")
    public Result login(@Validated LoginReq req) {
        return userService.login(req);
    }

    @PostMapping("/login4Google")
    @ApiOperation("谷歌验证器登录")
    public Result login4Google(@Validated Login4GoogleReq req) {
        return userService.login4Google(req);
    }
}
