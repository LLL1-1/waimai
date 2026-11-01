package com.sky.handler;

import com.sky.exception.BusinessException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>把业务异常、参数校验异常和未知异常统一转换成 {@link Result}，
 * 避免每个 Controller 都写 try-catch，也避免把堆栈直接暴露给前端。</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常，例如“分类名称已存在”“分类不存在”。
     *
     * @param ex 业务异常
     * @return 失败响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        log.warn("业务异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理 @Valid 参数校验失败，取第一条错误信息返回。
     *
     * @param ex 参数校验异常
     * @return 失败响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("参数校验失败");
        return Result.error(message);
    }

    /**
     * 处理 MyBatis 系统异常，最常见的原因是数据库连接失败。
     *
     * <p>例如 MYSQL_PASSWORD 配错、MySQL 没启动、库不存在等。
     * 完整堆栈仍然打到日志里，前端只看到明确的连接失败提示。</p>
     *
     * @param ex MyBatis 系统异常
     * @return 失败响应
     */
    @ExceptionHandler(MyBatisSystemException.class)
    public Result<Void> handleMyBatisSystemException(MyBatisSystemException ex) {
        log.error("数据库访问异常", ex);
        return Result.error("数据库连接失败，请检查数据库配置");
    }

    /**
     * 兜底异常处理，记录完整堆栈，但只给前端返回通用提示。
     *
     * @param ex 未知异常
     * @return 失败响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.error("系统繁忙，请稍后再试");
    }
}
