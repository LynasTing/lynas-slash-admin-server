package com.lynas.slashadmin.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * 将 JWT authorities claim 转换为 Spring Security 对象
 *
 * <p>JWT 内的 authority 只能来自服务端签发。转换时仍执行类型与字符白名单校验，
 * 避免异常 claim 值进入 SecurityContext
 */
@Component
public final class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String AUTHORITIES_CLAIM = "authorities";
  private static final Pattern AUTHORITY_PATTERN = Pattern.compile("^[A-Za-z0-9:_-]{1,128}$");

  /**
   * 提取并校验 authorities claim
   *
   * <p>claim 必须是字符串列表。无效元素被丢弃，而非抛出异常，
   * 防止单个脏权限值使本应有效的 JWT 转换流程异常
   *
   * @param jwt 已验签的 JWT
   * @return 去重、稳定排序后的 Spring Security 权限集合
   */
  private List<GrantedAuthority> extractAuthorities(
    Jwt jwt) {
    Object claim = jwt.getClaim(AUTHORITIES_CLAIM);
    if (!(claim instanceof List<?> authorityValues)) {
      return List.of();
    }

    /**
     * TreeSet 自动去重、自动按照字符串排序
     */
    Set<String> validAuthorities = new TreeSet<>();

    for (Object item : authorityValues) {
      // 不是字符串直接跳过，例如 Map、List、Integer 等
      if (!(item instanceof String authority)) {
        continue;
      }
      // 2.不符合 AUTHORITY_PATTERN
      if (!AUTHORITY_PATTERN.matcher(authority).matches()) {
        continue;
      }
      // 3.保存合法权限
      validAuthorities.add(authority);
    }

    // 创建最终返回的 Spring Security 权限列表
    List<GrantedAuthority> authorities = new ArrayList<>();

    /**
     * 遍历已经去重、排序后的权限字符串
     */
    for (String authority : validAuthorities) {
      // 4.将字符串转换成 Spring Security 的权限对象
      SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority);
      // 5.加到 authorities
      authorities.add(grantedAuthority);
    }

    return authorities;
  }

  /**
   * 将 JWT 转换为携带当前用户权限的认证对象
   *
   * @param jwt 已完成 JWT validator 校验的 Token
   * @return principal 为 JWT subject、authorities 来自有效 claim 的认证对象
   */
  @Override
  public AbstractAuthenticationToken convert(
    Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
  }

}
