package com.sky.context;

/**
 * 当前请求的登录人上下文。
 *
 * <p>拦截器解析 token 后把员工/用户 id 放进来，Service 层可以直接取，
 * 请求结束时必须清理，避免线程池复用导致数据串线。</p>
 */
public class BaseContext {

    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    private BaseContext() {
    }

    /**
     * 保存当前登录人 id。
     *
     * @param id 员工或用户 id
     */
    public static void setCurrentId(Long id) {
        THREAD_LOCAL.set(id);
    }

    /**
     * 获取当前登录人 id。
     *
     * @return 当前登录人 id，未登录时为 null
     */
    public static Long getCurrentId() {
        return THREAD_LOCAL.get();
    }

    /**
     * 清理当前登录人 id。
     */
    public static void removeCurrentId() {
        THREAD_LOCAL.remove();
    }
}
