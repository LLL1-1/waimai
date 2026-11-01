package com.sky.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果。
 *
 * <p>配合 {@link Result} 使用，前端通过 {@code total} 和 {@code records} 渲染表格与分页。</p>
 *
 * @param <T> 每页记录的数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页记录 */
    private List<T> records;
}
