package com.sky.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果。
 *
 * <p>所有接口都返回这个结构，前端只需要判断 {@code code}：
 * 1 表示成功，0 表示失败。</p>
 *
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码：1 成功，0 失败 */
    private Integer code;

    /** 提示信息，成功时一般为 null */
    private String msg;

    /** 响应数据 */
    private T data;

    /**
     * 成功响应，不携带数据。
     *
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return new Result<>(1, null, null);
    }

    /**
     * 成功响应，携带数据。
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(1, null, data);
    }

    /**
     * 失败响应。
     *
     * @param msg 失败原因
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(0, msg, null);
    }
}
