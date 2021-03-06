package top.b0x0.googlecheck.totp.common.enums;

/**
 * @author TANG
 */
public enum ApiResultEnum {
    /**
     * result 枚举
     */
    SUCCESS("200", "ok"),
    FAILED("400", "请求失败"),
    ERROR("500", "不知名错误"),
    ERROR_NULL("501", "空指针异常"),
    ERROR_CLASS_CAST("502", "类型转换异常"),
    ERROR_RUNTIME_EXCEPTION("503", "系统开小差了"),
    ERROR_IO("504", "上传文件异常"),
    ERROR_METHOD_NOT_SUPPORT("505", "请求方法错误"),
    AUTH_LOGIN_NOT_VALID("10000", "用户未登录，或token过期"),
    AUTH_GOOGLE_NOT_FOUND("10001", "未Goole校验，无法访问"),
    USER_IS_EXIST("10003", "用户已存在"),
    USER_NOT_EXIST("10004", "用户不存在"),
    USERNAME_OR_PASSWORD_IS_WRONG("10005", "用户名密码错误"),
    GOOGLE_CODE_NOT_MATCH("10006", "Google验证码不匹配"),
    GOOGLE_IS_BIND("10007", "Google验证器已绑定，不能重复绑定"),
    GOOGLE_NOT_BIND("10008", "Google验证器未绑定，请先进行绑定");

    private final String message;
    private final String status;

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    ApiResultEnum(String status, String message) {
        this.message = message;
        this.status = status;
    }


}
