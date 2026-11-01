package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类。
 *
 * <p>基于 jjwt 0.12.x：生成 token 时写入 claims 和过期时间，
 * 解析 token 时校验签名与有效期。</p>
 */
public class JwtUtil {

    private JwtUtil() {
    }

    /**
     * 生成 JWT。
     *
     * @param secretKey 签名密钥，长度至少 32 字节
     * @param ttlMillis 有效期，单位毫秒
     * @param claims    要写入 token 的业务数据，例如 empId
     * @return JWT 字符串
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMillis))
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验 JWT。
     *
     * @param secretKey 签名密钥
     * @param token     JWT 字符串
     * @return token 中的 claims
     * @throws io.jsonwebtoken.JwtException 签名不合法、token 过期或格式错误时抛出
     */
    public static Claims parseJWT(String secretKey, String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
