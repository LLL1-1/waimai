package com.sky.constant;

/**
 * 通用状态常量。
 *
 * <p>与数据库里的 status 字段保持一致：0 禁用，1 启用。</p>
 */
public final class StatusConstant {

    private StatusConstant() {
    }

    /** 启用 */
    public static final int ENABLE = 1;

    /** 禁用 */
    public static final int DISABLE = 0;
}
