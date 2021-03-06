package top.b0x0.googlecheck.totp.common.exception;

import top.b0x0.googlecheck.totp.common.enums.ApiResultEnum;
import top.b0x0.googlecheck.totp.common.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常捕获
 *
 * @author TANG
 * @since 2020-12-23
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NullPointerException.class)
    public Result handleNullPointer(NullPointerException ex) {
        logger.error(ex.getMessage(), ex);
        return Result.error(ApiResultEnum.ERROR_NULL);
    }

    @ExceptionHandler(ClassCastException.class)
    public Result handleClassCastException(ClassCastException ex) {
        logger.error(ex.getMessage(), ex);
        return Result.error(ApiResultEnum.ERROR_CLASS_CAST);
    }

    @ExceptionHandler(IOException.class)
    public Result handleIoException(IOException ex) {
        logger.error(ex.getMessage(), ex);
        return Result.error(ApiResultEnum.ERROR_IO);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        logger.error(ex.getMessage(), ex);
        return Result.error(ApiResultEnum.ERROR_METHOD_NOT_SUPPORT);
    }

    /**
     * 处理自定义异常
     *
     * @param ex /
     * @return /
     */
    @ExceptionHandler(ApiException.class)
    public Result handleApiException(ApiException ex) {
        logger.error(ex.getMessage(), ex);
        return Result.error(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException ex) {
        logger.error(ex.getMessage(), ex);
        return Result.error(ApiResultEnum.ERROR_RUNTIME_EXCEPTION);
    }

    @ExceptionHandler(Exception.class)
    public Result exception(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return Result.error(ApiResultEnum.ERROR);
    }

    //  Validated 请求参数校验异常处理

    /**
     * <1> 处理 form data方式调用接口校验失败抛出的异常
     */
    @ExceptionHandler(BindException.class)
    public Result bindExceptionHandler(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> collect = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        return Result.error("400",collect.toString());
    }

    /**
     * <2> 处理 json 请求体调用接口校验失败抛出的异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> collect = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        return Result.error("400",collect.toString());
    }

    /**
     * <3> 处理单个参数校验失败抛出的异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result constraintViolationExceptionHandler(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        List<String> collect = constraintViolations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        return Result.error("400",collect.toString());
    }

}
