package com.lynas.slashadmin.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lynas.slashadmin.auth.dto.LoginRequest;
import com.lynas.slashadmin.auth.dto.RegisterRequest;
import com.lynas.slashadmin.auth.entity.RefreshTokenEntity;
import com.lynas.slashadmin.auth.enums.AuthCodeEnum;
import com.lynas.slashadmin.auth.mapper.RefreshTokenMapper;
import com.lynas.slashadmin.auth.model.AuthenticatedUser;
import com.lynas.slashadmin.auth.service.AuthService;
import com.lynas.slashadmin.auth.service.RefreshTokenCookieService;
import com.lynas.slashadmin.auth.service.TokenService;
import com.lynas.slashadmin.auth.service.UserAuthorizationService;
import com.lynas.slashadmin.auth.vo.AccessTokenResponse;
import com.lynas.slashadmin.auth.vo.AuthenticatedUserVo;
import com.lynas.slashadmin.auth.vo.CsrfTokenResponse;
import com.lynas.slashadmin.auth.vo.LoginResponse;
import com.lynas.slashadmin.common.enums.ResponseCodeEnum;
import com.lynas.slashadmin.common.exception.BusinessException;
import com.lynas.slashadmin.config.JwtProperties;
import com.lynas.slashadmin.system.role.entity.SysRoleEntity;
import com.lynas.slashadmin.system.role.mapper.SysRoleMapper;
import com.lynas.slashadmin.system.user.entity.SysUserEntity;
import com.lynas.slashadmin.system.user.entity.SysUserRoleEntity;
import com.lynas.slashadmin.system.user.enums.SysUserEnums;
import com.lynas.slashadmin.system.user.mapper.SysUserMapper;
import com.lynas.slashadmin.system.user.mapper.SysUserRoleMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuthServiceImpl
 * 认证业务服务实现
 *
 * <p>该类负责组织登录、刷新、退出和 CSRF Token 相关业务逻辑
 * Token 生成、Cookie 读写、用户授权查询和数据库会话持久化分别交由对应组件完成
 */
@Service
public class AuthServiceImpl implements AuthService {

  /**
   * 新注册用户默认角色编码
   */
  private static final String DEFAULT_REGISTER_ROLE_CODE = "NORMAL_USER";

  /**
   * 用户授权查询服务
   *
   * <p>用于根据用户名或用户 ID 查询当前仍然有效的认证用户
   */
  private final UserAuthorizationService userAuthorizationService;

  /**
   * BCrypt 密码校验器
   *
   * <p>只负责比较登录请求中的明文密码与数据库中的密码散列
   */
  private final PasswordEncoder passwordEncoder;

  /**
   * Token 安全处理服务
   *
   * <p>统一负责 access JWT 签发、 refresh token 生成与摘要计算
   */
  private final TokenService tokenService;

  /**
   * Refresh Token 数据库 Mapper
   *
   * <p>用于保存、锁定、轮换和撤销 refresh token 会话记录
   */
  private final RefreshTokenMapper refreshTokenMapper;

  /**
   * Refresh Token Cookie 读写组件
   *
   * <p>统一处理 refresh token cookie 的写入、读取和清除
   */
  private final RefreshTokenCookieService refreshTokenCookieService;

  /**
   * JWT 配置属性
   *
   * <p>用于读取 access token 和 refresh token 的有效期配置
   */
  private final JwtProperties jwtProperties;

  /**
   * 统一时间来源
   *
   * <p>避免业务代码直接调用系统时间，保证 JWT 和 数据库会话时间可测试
   */
  private final Clock clock;

  /**
   * 编程式事务模板
   *
   * <p>用于确保数据库事务提交成功后，再向客户端写入新的 Cookie
   */
  private final TransactionTemplate transactionTemplate;

  /**
   * 用户数据库 Mapper。
   */
  private final SysUserMapper sysUserMapper;

  /**
   * 角色数据库 Mapper。
   */
  private final SysRoleMapper sysRoleMapper;

  /**
   * 用户角色关联 Mapper。
   */
  private final SysUserRoleMapper sysUserRoleMapper;

  public AuthServiceImpl(UserAuthorizationService userAuthorizationService, PasswordEncoder passwordEncoder,
    TokenService tokenService, RefreshTokenMapper refreshTokenMapper,
    RefreshTokenCookieService refreshTokenCookieService, JwtProperties jwtProperties, Clock clock,
    PlatformTransactionManager transactionManager, SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper,
    SysUserRoleMapper sysUserRoleMapper) {
    this.userAuthorizationService = userAuthorizationService;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
    this.refreshTokenMapper = refreshTokenMapper;
    this.refreshTokenCookieService = refreshTokenCookieService;
    this.jwtProperties = jwtProperties;
    this.clock = clock;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.sysUserMapper = sysUserMapper;
    this.sysRoleMapper = sysRoleMapper;
    this.sysUserRoleMapper = sysUserRoleMapper;
  }

  /**
   * 注册普通用户
   *
   * <p>注册用户固定使用 DEFAULT_REGISTER_ROLE_CODE 角色，客户端不能指定角色
   *
   * @param request 注册请求参数
   */
  @Override
  public void register(
    RegisterRequest request) {
    // 1.校验请求体
    if (request == null) {
      throw new BusinessException(AuthCodeEnum.PARAMETER_MISSING);
    }

    // 2.用户名是否已存在
    boolean usernameExists = sysUserMapper
      .exists(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getUsername, request.getUsername()));

    if (usernameExists) {
      throw new BusinessException(SysUserEnums.NAME_ALREADY_EXISTS);
    }

    // 3.查询注册用户固定使用的普通角色
    SysRoleEntity defaultRole = sysRoleMapper
      .selectOne(new LambdaQueryWrapper<SysRoleEntity>().eq(SysRoleEntity::getCode, DEFAULT_REGISTER_ROLE_CODE)
        .eq(SysRoleEntity::getStatus, 1).eq(SysRoleEntity::getDeleted, 0));
    if (defaultRole == null) {
      throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
    }

    // 4.创建用户实体
    SysUserEntity user = new SysUserEntity();
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setStatus(1);
    user.setDeleted(0);
    user.setAuthVersion(1);
    user.setEmail(null);

    // 5.在同一个事务中保存用户和默认角色关联
    transactionTemplate.executeWithoutResult(status -> {
      // 插入表
      int userRows = sysUserMapper.insert(user);
      if (userRows != 1) {
        throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
      }

      // 插入用户与角色关联关系
      int roleRows = sysUserRoleMapper.insert(new SysUserRoleEntity(user.getId(), defaultRole.getId()));
      if (roleRows != 1) {
        throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
      }

    });
  }

  /**
   * 使用 用户名 + 密码登录
   *
   * <p>登录成功后签发 access token，将 refresh token 摘要保存到数据库，
   * 并在事物提交成功后将 refresh token 原文写入 HttpOnly Cookie
   *
   * @param request  用户名密码请求
   * @param response HTTP 响应对象
   * @return 登录成功响应
   */
  @Override
  public LoginResponse login(
    LoginRequest request,
    HttpServletResponse response) {
    // 1.校验请求参数
    if (request == null) {
      throw new BusinessException(ResponseCodeEnum.AUTHENTICATION_FAILED);
    }

    // 2.按用户名查询启用且未删除的认证用户
    AuthenticatedUser user = userAuthorizationService.findActiveUserByUsername(request.getUsername())
      .orElseThrow(() -> new BusinessException(ResponseCodeEnum.AUTHENTICATION_FAILED));

    // 3.比较请求中的明文密码和数据库中的 BCrypt 密码散列
    boolean passwordMatches = user.passwordHash() != null
      && passwordEncoder.matches(request.getPassword(), user.passwordHash());

    // 4.密码不匹配时返回统一错误，避免暴露账号是否存在
    if (!passwordMatches) {
      throw new BusinessException(ResponseCodeEnum.AUTHENTICATION_FAILED);
    }

    // 5.签发 access token
    String accessToken = tokenService.createAccessToken(user);

    // 6.生成 refresh token 原文和数据库摘要
    TokenService.GeneratedRefreshToken generatedRefreshToken = tokenService.createRefreshToken();

    // 7.计算 access token 和 refresh token 的过期时间
    Instant accessTokenExpiresAt = clock.instant().plus(jwtProperties.accessTokenTtl());
    LocalDateTime refreshTokenExpiresAt = LocalDateTime.now(clock).plus(jwtProperties.refreshTokenTtl());

    // 8.创建数据库会话实体，只保存 refresh token 摘要
    RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
    refreshTokenEntity.setUserId(user.userId());
    refreshTokenEntity.setTokenHash(generatedRefreshToken.sha256Hash());
    refreshTokenEntity.setFamilyId(UUID.randomUUID().toString());
    refreshTokenEntity.setExpiresAt(refreshTokenExpiresAt);
    refreshTokenEntity.setCreatedAt(LocalDateTime.now(clock));

    // 9.保存数据库会话，事务提交失败时不会写入 Cookie
    transactionTemplate.executeWithoutResult(status -> {
      int affectedRows = refreshTokenMapper.insert(refreshTokenEntity);
      if (affectedRows != 1) {
        throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
      }
    });

    // 10.数据库事物成功后，才写入 refresh token cookie
    refreshTokenCookieService.write(response, generatedRefreshToken.rawValue());

    // 11.构建值包含安全字段的登录响应
    return new LoginResponse(accessToken, AuthenticatedUserVo.from(user), accessTokenExpiresAt);
  }

  /**
   * refresh 成功后返回给事务外使用的数据
   *
   * @param accessToken          新的 access token
   * @param rawRefreshToken      新的 refresh token 原文
   * @param accessTokenExpiresAt access token 过期时间
   * @param failed               是否刷新失败
   */
  private record RefreshResult(String accessToken, String rawRefreshToken, Instant accessTokenExpiresAt,
    boolean failed) {
    /**
     * 刷新成功
     */
    private static RefreshResult success(
      String accessToken,
      String rawRefreshToken,
      Instant accessTokenExpiresAt) {
      return new RefreshResult(accessToken, rawRefreshToken, accessTokenExpiresAt, false);
    }

    /**
     * 刷新失败
     *
     * <p>该结果用于让事务正常提交撤销操作，之后由事务外统一抛出 NEED_LOGIN
     */
    private static RefreshResult failure() {
      return new RefreshResult(null, null, null, true);
    }
  }

  /**
   * 撤销指定 refresh token 所属的整个会话族
   *
   * <p>当已经撤销的 refresh token 再次被使用时，
   * 说明旧的 token 可能被重放，需要让同一次会话族中的其它 token 也立即失效
   *
   * @param token     已被判定为撤销的 refresh token
   * @param revokedAt 撤销时间
   */
  private void revokeTokenFamily(
    RefreshTokenEntity token,
    LocalDateTime revokedAt) {

    // 1.创建批量更新条件
    LambdaUpdateWrapper<RefreshTokenEntity> updateWrapper = new LambdaUpdateWrapper<>();

    // 2.优先按照 family_id 撤销整个会话族
    if (token.getFamilyId() != null && !token.getFamilyId().isBlank()) {
      updateWrapper.eq(RefreshTokenEntity::getFamilyId, token.getFamilyId());
    } else {
      // 3.历史数据没有 family_id 时，只撤销当前记录
      updateWrapper.eq(RefreshTokenEntity::getId, token.getId());
    }

    // 4.只更新仍未撤销的会话
    updateWrapper.isNull(RefreshTokenEntity::getRevokedAt);

    // 5.写入撤销时间
    updateWrapper.set(RefreshTokenEntity::getRevokedAt, revokedAt);

    // 6.执行批量撤销
    refreshTokenMapper.update(null, updateWrapper);
  }

  /**
   * 使用 refresh token 刷新 access token
   *
   * <p>刷新过程会锁定旧会话，校验通过后会创建新会话并撤销旧会话
   * 事务提交成功后才替换浏览器中的 refresh token cookie
   *
   * @param request  HTTP 请求对象
   * @param response HTTP 响应对象
   * @return 新的 access token
   */
  @Override
  public AccessTokenResponse refresh(
    HttpServletRequest request,
    HttpServletResponse response) {

    // 1.从请求 Cookie 中读取 refresh token 原文
    String rawRefreshToken = refreshTokenCookieService.read(request)
      .orElseThrow(() -> new BusinessException(ResponseCodeEnum.NEED_LOGIN));

    // 2.计算 refresh token 摘要，用于查询数据库
    String tokenHash = tokenService.hashRefreshToken(rawRefreshToken);

    // 3.在事务中完成锁定、校验、轮换和撤销旧会话
    RefreshResult refreshResult = transactionTemplate.execute(status -> {
      // 3.1 锁定当前 refresh token 记录，防止并发刷新
      RefreshTokenEntity oldToken = refreshTokenMapper.selectByTokenHashForUpdate(tokenHash);

      // 3.2 找不到记录（旧 token）时，判定 refresh token 无效
      if (oldToken == null) {
        throw new BusinessException(ResponseCodeEnum.NEED_LOGIN);
      }

      LocalDateTime now = LocalDateTime.now(clock);

      // 3.3 已撤销的 token 可能是重放攻击，需要撤销整个会话族
      if (oldToken.getRevokedAt() != null) {
        revokeTokenFamily(oldToken, now);
        return RefreshResult.failure();
      }

      // 3.4 已过期的 token 不能继续刷新
      if (oldToken.getExpiresAt() == null || !oldToken.getExpiresAt().isAfter(now)) {
        oldToken.setRevokedAt(now);

        int updatedRows = refreshTokenMapper.updateById(oldToken);
        if (updatedRows != 1) {
          throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
        }

        return RefreshResult.failure();
      }

      // 3.5重新读取当前用户，确保用户未删除且启用
      AuthenticatedUser user = userAuthorizationService.findActiveUserById(oldToken.getUserId())
        .orElseThrow(() -> new BusinessException(ResponseCodeEnum.NEED_LOGIN));

      // 3.6为当前用户签发新的 access token
      String accessToken = tokenService.createAccessToken(user);

      // 3.7生成新的 refresh token
      TokenService.GeneratedRefreshToken newRefreshToken = tokenService.createRefreshToken();

      // 3.8创建新的数据库会话，并继承原会话族 ID
      RefreshTokenEntity newToken = new RefreshTokenEntity();
      newToken.setUserId(user.userId());
      newToken.setTokenHash(newRefreshToken.sha256Hash());
      newToken.setFamilyId(oldToken.getFamilyId());
      newToken.setExpiresAt(now.plus(jwtProperties.refreshTokenTtl()));
      newToken.setCreatedAt(now);

      // 3.9保存新的 refresh token 会话
      int insertedRows = refreshTokenMapper.insert(newToken);
      if (insertedRows != 1) {
        throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
      }

      // 3.10撤销旧会话，并记录它被哪个新会话替换
      oldToken.setRevokedAt(now);
      oldToken.setReplacedById(newToken.getId());

      int updateRows = refreshTokenMapper.updateById(oldToken);
      if (updateRows != 1) {
        throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
      }

      // 3.11返回事务外需要使用的数据
      Instant accessTokenExpiresAt = clock.instant().plus(jwtProperties.accessTokenTtl());

      return RefreshResult.success(accessToken, newRefreshToken.rawValue(), accessTokenExpiresAt);
    });

    // 4.数据库事务提交成功后，替换浏览器中的 refresh token cookie
    if (refreshResult.failed()) {
      throw new BusinessException(ResponseCodeEnum.NEED_LOGIN);
    }

    // 5.只有刷新成功后，才替换浏览器中的 refresh token cookie
    refreshTokenCookieService.write(response, refreshResult.rawRefreshToken());

    // 6.只向响应 body 返回新的 access token
    return new AccessTokenResponse(refreshResult.accessToken(), refreshResult.accessTokenExpiresAt());
  }

  /**
   * 退出当前 refresh token 会话
   *
   * <p>退出操作具备幂等性：没有 Cookie、Cookie 无效或会话已经撤销时，
   * 都会清除浏览器 Cookie 并正常结束
   *
   * @param request  HTTP 请求对象
   * @param response HTTP 响应对象
   */
  @Override
  public void logout(
    HttpServletRequest request,
    HttpServletResponse response) {
    // 1.尝试读取 refresh token cookie
    refreshTokenCookieService.read(request).ifPresent(rawRefreshToken -> {
      // 2.计算 refresh token 摘要
      String tokenHash = tokenService.hashRefreshToken(rawRefreshToken);

      // 3.在事务中锁定并撤销当前会话
      transactionTemplate.executeWithoutResult(status -> {
        RefreshTokenEntity token = refreshTokenMapper.selectByTokenHashForUpdate(tokenHash);

        // 4.Cookie 无对应数据库记录时，保持幂等，不抛出异常
        if (token == null) {
          return;
        }

        // 5.已经撤销的会话不需要重复更新
        if (token.getRevokedAt() != null) {
          return;
        }

        // 6.记录当前撤销时间
        token.setRevokedAt(LocalDateTime.now(clock));

        // 7.确认数据库确实完成了更新
        int updatedRows = refreshTokenMapper.updateById(token);
        if (updatedRows != 1) {
          throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
        }
      });
    });

    // 8.无论数据库中是否存在会话，都清除浏览器 Cookie
    refreshTokenCookieService.clear(response);
  }

  /**
   * 返回当前请求对应的 CSRF Token 信息
   *
   * <p>CSRF Token 的生成和 Cookie 写入由 Spring Security 的 CookieCsrfTokenRepository 负责，
   * 本方法只负责转换为接口响应对象
   *
   * @param csrfToken Spring Security 提供的 CSRF Token
   * @return CSRF Token 响应
   */
  @Override
  public CsrfTokenResponse createCsrfToken(
    CsrfToken csrfToken) {
    // 1.校验 Spring Security 是否提供了 CSRF Token
    if (csrfToken == null) {
      throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
    }

    // 2.读取 token 的值、请求头名称和参数名称
    String token = csrfToken.getToken();
    String headerName = csrfToken.getHeaderName();
    String parameterName = csrfToken.getParameterName();

    // 3.构造返回
    return new CsrfTokenResponse(token, headerName, parameterName);
  }

  /**
   * 查询当前登录用户信息
   *
   * <p>Authentication#getName() 由 JwtAuthenticationConverter 设置为 JWT subject,
   * 此处将 subject 解析为用户 ID 后重新查询当前授权数据
   *
   * @param authentication 当前 Spring Security 认证对象
   * @return 当前用户公开资料
   */
  @Override
  public AuthenticatedUserVo me(
    Authentication authentication) {

    // 1.校验当前请求是否存在有效认证
    if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null
      || authentication.getName().isBlank()) {
      throw new BusinessException(ResponseCodeEnum.NEED_LOGIN);
    }

    // 2.将 JWT subject 解析为用户 ID
    Long userId;
    try {
      userId = Long.valueOf(authentication.getName());
    } catch (NumberFormatException exception) {
      throw new BusinessException(ResponseCodeEnum.NEED_LOGIN);
    }

    // 3.重新查询当前用户的最新角色、权限和菜单
    AuthenticatedUser user = userAuthorizationService.findActiveUserById(userId)
      .orElseThrow(() -> new BusinessException(ResponseCodeEnum.NEED_LOGIN));

    // 4.转换为不包含密码和认证版本的公开对象
    return AuthenticatedUserVo.from(user);
  }
}
