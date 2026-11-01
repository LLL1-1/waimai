package com.sky.exception;

/**
 * 业务异常。
 *
 * <p>用于表达“参数不合法、名称重复、数据不存在”等可预期错误，
 * 由全局异常处理器转成统一的失败响应，不打印完整堆栈。</p>
 */
public class BusinessException extends RuntimeException {

    /**
     * @param message 给前端看的错误提示
     */
    public BusinessException(String message) {
        super(message);
    }
}
