package com.lynas.slashadmin.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynas.slashadmin.auth.entity.RefreshTokenEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * refresh token 会话表的数据访问接口
 *
 * <p>轮换 refresh token 时必须在事务中加锁查询，防止同一旧 token 被并发刷新两次
 */
@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshTokenEntity> {
  /**
   * 按摘要查询并锁定 Refresh Token 会话。
   *
   * <p>该方法用于 Refresh Token Rotation（刷新令牌轮换）场景。
   * 调用方必须处于数据库事务中，查询通过 {@code FOR UPDATE} 对匹配记录加排他锁，
   * 锁会持续到当前事务提交或回滚。
   *
   * <p>加锁的主要目的是防止并发刷新：当多个请求同时使用同一个旧 Refresh Token
   * 请求刷新时，第一个请求获得记录锁后，可以完成旧 Token 校验、撤销以及新 Token
   * 创建和替换关系更新；其他请求必须等待当前事务结束。之后再次读取该记录时，
   * 即可发现旧 Token 已被撤销，从而避免同一个 Refresh Token 被重复消费并生成多个
   * 有效的新 Token。
   *
   * <p>因此，{@code FOR UPDATE} 保证了旧 Token 的“查询 → 校验 → 撤销 → 替换”
   * 在并发场景下能够作为一个受事务保护的状态变更流程执行。
   *
   * @param tokenHash Refresh Token 的 SHA-256 十六进制摘要
   * @return 匹配的 Refresh Token 会话；不存在时返回 null
   */
  @Select("""
    SELECT
      id,
      user_id,
      token_hash,
      family_id,
      expires_at,
      revoked_at,
      replaced_by_id,
      created_at
    FROM auth_refresh_token
    WHERE token_hash = #{tokenHash}
    FOR UPDATE
    """)
  RefreshTokenEntity selectByTokenHashForUpdate(
    @Param("tokenHash") String tokenHash);

}
