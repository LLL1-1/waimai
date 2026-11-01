package com.sky.interceptor;

import com.sky.context.BaseContext;
import com.sky.exception.BusinessException;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理端 JWT 登录拦截器。
 *
 * <p>拦截 /admin/**，从请求头中取出 token，校验签名和有效期，
 * 解析出 empId 后放入 BaseContext，供 Service 层获取当前登录人。</p>
 */
@Component
@RequiredArgsConstructor
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;

    /**
     * 请求进入 Controller 前校验 token。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  当前处理器
     * @return true 表示放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        // 没有 token 直接按未登录处理
        if (!StringUtils.hasText(token)) {
            throw new BusinessException("未登录或登录已过期");
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get("empId").toString());
            BaseContext.setCurrentId(empId);
            return true;
        } catch (Exception ex) {
            // 签名错误、token 过期、格式错误都统一按未登录处理
            throw new BusinessException("未登录或登录已过期");
        }
    }

    /**
     * 请求结束后清理 ThreadLocal，避免线程复用导致登录人串线。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  当前处理器
     * @param ex       请求处理中的异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
