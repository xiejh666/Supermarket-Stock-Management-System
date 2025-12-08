package com.supermarket.exception;

import com.supermarket.common.Result;
import com.supermarket.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数校验异常：{}", e.getMessage());
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        log.error("参数绑定异常：{}", e.getMessage());
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("参数校验异常：{}", e.getMessage());
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder message = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            message.append(violation.getMessage()).append("; ");
        }
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message.toString());
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("文件上传大小超限：{}", e.getMessage());
        long maxSize = e.getMaxUploadSize();
        String message;
        if (maxSize > 0) {
            long maxSizeMB = maxSize / (1024 * 1024);
            message = String.format("上传文件大小超过限制，最大允许 %dMB", maxSizeMB);
        } else {
            message = "上传文件大小超过限制，请上传较小的文件";
        }
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：", e);
        // 检查是否是常见的异常类型，提供更友好的提示
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("Access denied") || message.contains("权限")) {
                return Result.error(ResultCode.ERROR.getCode(), "权限不足，请联系管理员");
            } else if (message.contains("Connection") || message.contains("连接")) {
                return Result.error(ResultCode.ERROR.getCode(), "网络连接异常，请稍后重试");
            } else if (message.contains("Timeout") || message.contains("超时")) {
                return Result.error(ResultCode.ERROR.getCode(), "操作超时，请稍后重试");
            }
        }
        return Result.error(ResultCode.ERROR.getCode(), "系统异常，请联系管理员");
    }
}

