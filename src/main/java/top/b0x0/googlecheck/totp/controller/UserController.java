package top.b0x0.googlecheck.totp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.b0x0.googlecheck.totp.annotation.NeedLogin;
import top.b0x0.googlecheck.totp.common.vo.GoogleDTO;
import top.b0x0.googlecheck.totp.common.vo.Result;
import top.b0x0.googlecheck.totp.common.vo.req.RegisterReq;
import top.b0x0.googlecheck.totp.controller.base.BaseController;
import top.b0x0.googlecheck.totp.service.UserService;
import top.b0x0.googlecheck.totp.util.QRCodeUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * 接口文档地址: http://localhost:8081/doc.html
 *
 * @author TANG
 */
@Api(tags = "用户模块")
@RestController
@RequestMapping("/user")
@Validated
public class UserController extends BaseController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    @ApiOperation("注册")
    public Result register(@Validated RegisterReq req) throws Exception {
        return userService.register(req);
    }

    @NeedLogin
    @GetMapping("/generateGoogleSecret")
    @ApiOperation("生成google密钥")
    public Result generateGoogleSecret() {
        return userService.generateGoogleSecret(this.getUser());
    }

    /**
     * 注意：这个需要地址栏请求,因为返回的是一个流
     * 显示一个二维码图片
     *
     * @param secretQrCode eg：otpauth://totp/%E7%9B%88%E4%BC%97%E5%95%86%E4%BF%9D%3A%2817680540104%29?secret=GCHSSODUOA7YH7SODZVZCYYFG6HT6PXU&issuer=%E7%9B%88%E4%BC%97%E5%95%86%E4%BF%9D
     * @param response     /
     */
    @GetMapping("/genQrCode")
    @ApiOperation("生成二维码")
    public void genQrCode(String secretQrCode, HttpServletResponse response) throws Exception {
        response.setContentType("image/png");
        OutputStream stream = response.getOutputStream();
        QRCodeUtils.encode(secretQrCode, stream);
    }

    @NeedLogin
    @PostMapping("/bindGoogleSecret")
    @ApiOperation("绑定密钥")
    public Result bindGoogle(GoogleDTO dto) {
        return userService.bindGoogle(dto, this.getUser(),this.getRequest());
    }

    @NeedLogin
    @PostMapping("/googleSecValidation")
    @ApiOperation("google二次验证")
    public Result googleSecValidation(String code) {
        return userService.googleLogin(code, this.getUser(), this.getRequest());
    }


    @GetMapping("/getData")
    @NeedLogin(google = true)
    @ApiOperation("获取数据")
    public Result getData() {
        return userService.getData();
    }

}

