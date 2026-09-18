package com.lynas.slashadmin.config;

import com.lynas.slashadmin.common.security.UserAuthenticationValidator;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

/**
 * JWT 签发所需的密码学组件配置
 *
 * <p>HS256 密钥只从 JwtProperties 读取并在内存中构造编码器，
 * 不输出到日志、接口响应或任何持久化位置
 */
@Configuration
public class JwtCryptoConfig {

  /**
   * 提供应用共享的加密安全随机数来源
   *
   * <p>Refresh Token 必须使用 SecureRandom 生成，不能使用 UUID、Math.random 或者普通的伪随机数生成器
   *
   * @return 加密安全随机数生成器
   */
  @Bean
  public SecureRandom secureRandom() {
    return new SecureRandom();
  }

  /**
   * 基于配置密钥创建 HS256 JWT 编码器
   *
   * <p>JwtProperties 已在应用启动阶段校验 Base64 格式和最小密钥长度，
   * 此处只负责解码并交给 NimbusJwtEncoder 签发 Token
   *
   * @param jwtProperties 已校验的 JWT 配置
   * @return 使用 HS256 对称密钥的 JWT 编码器
   */
  @Bean
  public JwtEncoder jwtEncoder(
    JwtProperties jwtProperties) {
    byte[] secret = Base64.getDecoder().decode(jwtProperties.secretBase64());
    return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secret));
  }

  /**
   * 创建同时验证签名、issuer、audience、时间和用户当前状态的 JWT decoder
   *
   * @param jwtProperties               JWT 配置属性
   * @param userAuthenticationValidator 用户当前状态校验器
   * @return 配置完成的 JWT decoder
   */
  @Bean
  public JwtDecoder jwtDecoder(
    JwtProperties jwtProperties,
    UserAuthenticationValidator userAuthenticationValidator) {

    // 1. 将配置文件中的 Base64 密钥字符串解码成 byte[]
    byte[] secret = Base64.getDecoder().decode(jwtProperties.secretBase64());
    // 2. 将 byte[] 包装成 HMAC-SHA256 可以使用的密钥对象
    SecretKey secretKey = new SecretKeySpec(secret, "HmacSHA256");
    // 3. 创建 JWT 解码器。指定使用上面的密钥，并使用 HS256 验证 JWT 签名
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
    // 4. 创建签发者校验器。检查 JWT 的 iss 是否等于配置中的 issuer
    JwtIssuerValidator issuerValidator = new JwtIssuerValidator(jwtProperties.issuer());
    // 5. 创建时间校验器。检查 exp、nbf 等时间，并允许 30 秒的时钟误差
    JwtTimestampValidator timestampValidator = new JwtTimestampValidator(Duration.ofSeconds(30));
    // 6. 创建受众校验器。检查 JWT 的 aud 是否包含系统要求的 audience
    JwtClaimValidator<List<String>> audienceValidator = new JwtClaimValidator<>("aud",
      audience -> audience != null && audience.contains(jwtProperties.audience()));
    // 7. 将所有 JWT 的校验规则组合起来
    OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithValidators(issuerValidator, timestampValidator,
      audienceValidator, userAuthenticationValidator);
    // 8. 将组合后的校验规则交给 JWT 解码器
    decoder.setJwtValidator(validator);
    // 9. 返回配置完成的 JWT 解码器
    return decoder;
  }
}
