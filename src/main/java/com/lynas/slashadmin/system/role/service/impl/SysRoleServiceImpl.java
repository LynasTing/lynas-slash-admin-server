package com.lynas.slashadmin.system.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.lynas.slashadmin.common.enums.ResponseCodeEnum;
import com.lynas.slashadmin.common.exception.BusinessException;
import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.common.response.PageResponse;
import com.lynas.slashadmin.common.utils.BeanCopyUtils;
import com.lynas.slashadmin.system.menu.entity.SysMenuEntity;
import com.lynas.slashadmin.system.menu.enums.SysMenuEnum;
import com.lynas.slashadmin.system.menu.service.SysMenuService;
import com.lynas.slashadmin.system.role.dto.SysRoleSaveDto;
import com.lynas.slashadmin.system.role.entity.SysRoleEntity;
import com.lynas.slashadmin.system.role.entity.SysRoleMenuEntity;
import com.lynas.slashadmin.system.role.entity.SysRolePageQuery;
import com.lynas.slashadmin.system.role.enums.SysRoleEnum;
import com.lynas.slashadmin.system.role.mapper.SysRoleMapper;
import com.lynas.slashadmin.system.role.mapper.SysRoleMenuMapper;
import com.lynas.slashadmin.system.role.service.SysRoleService;
import com.lynas.slashadmin.system.role.vo.SysRoleDetailVo;
import com.lynas.slashadmin.system.role.vo.SysRoleListVo;
import com.lynas.slashadmin.system.role.vo.SysRoleOptionVo;
import com.lynas.slashadmin.system.user.entity.SysUserRoleEntity;
import com.lynas.slashadmin.system.user.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 角色业务实现。
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

  private final SysMenuService sysMenuService;

  private final SysRoleMapper sysRoleMapper;

  private final SysRoleMenuMapper sysRoleMenuMapper;

  private final SysUserRoleMapper sysUserRoleMapper;

  public SysRoleServiceImpl(SysMenuService sysMenuService, SysRoleMapper sysRoleMapper,
    SysRoleMenuMapper sysRoleMenuMapper, SysUserRoleMapper sysUserRoleMapper) {
    this.sysMenuService = sysMenuService;
    this.sysRoleMapper = sysRoleMapper;
    this.sysRoleMenuMapper = sysRoleMenuMapper;
    this.sysUserRoleMapper = sysUserRoleMapper;
  }

  /**
   * 分页获取角色列表。
   *
   * <p>按排序值和主键稳定分页，只返回列表所需字段。
   *
   * @param query 分页查询参数
   */
  @Override
  public ApiResponse<PageResponse<SysRoleListVo>> getSysRoleList(
    SysRolePageQuery query) {
    LambdaQueryWrapper<SysRoleEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper
      .select(SysRoleEntity::getId, SysRoleEntity::getName, SysRoleEntity::getCode, SysRoleEntity::getSort,
        SysRoleEntity::getStatus, SysRoleEntity::getDescription)
      .like(StringUtils.hasText(query.getName()), SysRoleEntity::getName, query.getName())
      .like(StringUtils.hasText(query.getCode()), SysRoleEntity::getCode, query.getCode())
      .eq(query.getStatus() != null, SysRoleEntity::getStatus, query.getStatus()).orderByAsc(SysRoleEntity::getSort)
      .orderByAsc(SysRoleEntity::getId);
    Page<SysRoleEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
    page(page, wrapper);
    List<SysRoleListVo> sysRoleListVos = BeanCopyUtils.beanListCopy(page.getRecords(), SysRoleListVo.class);
    return ApiResponse.success(new PageResponse<>(sysRoleListVos, page.getTotal()));
  }

  /**
   * 获取角色详情及已授权菜单 ID。
   */
  @Override
  public ApiResponse<SysRoleDetailVo> getSysRoleDetail(
    Long id) {
    validateId(id);
    SysRoleEntity role = getOne(new LambdaQueryWrapper<SysRoleEntity>()
      .select(SysRoleEntity::getId, SysRoleEntity::getName, SysRoleEntity::getCode, SysRoleEntity::getSort,
        SysRoleEntity::getStatus, SysRoleEntity::getDescription)
      .eq(SysRoleEntity::getId, id));
    if (Objects.isNull(role)) {
      throw new BusinessException(SysRoleEnum.ROLE_NOT_EXIST);
    }

    SysRoleDetailVo detail = BeanCopyUtils.beanCopy(role, SysRoleDetailVo.class);
    List<Long> menuIds = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenuEntity>()
      .select(SysRoleMenuEntity::getMenuId).eq(SysRoleMenuEntity::getRoleId, id)).stream()
      .map(SysRoleMenuEntity::getMenuId).toList();
    detail.setMenuIds(menuIds);
    return ApiResponse.success(detail);
  }

  /**
   * 新增/修改 角色字段重复校验
   */
  private void validateUnique(
    Long id,
    SysRoleSaveDto args) {
    // 角色名是否已存在
    boolean nameExists = sysRoleMapper.exists(new LambdaQueryWrapper<SysRoleEntity>()
      .eq(SysRoleEntity::getName, args.getName()).ne(id != null, SysRoleEntity::getId, id));
    if (nameExists) {
      throw new BusinessException(SysRoleEnum.NAME_ALREADY_EXISTS);
    }

    // 角色编码是否已重复
    boolean codeExists = sysRoleMapper.exists(new LambdaQueryWrapper<SysRoleEntity>()
      .eq(SysRoleEntity::getCode, args.getCode()).ne(id != null, SysRoleEntity::getId, id));
    if (codeExists) {
      throw new BusinessException(SysRoleEnum.CODE_ALREADY_EXISTS);
    }
  }

  /**
   * 新增/修改 角色关联菜单 ID 校验
   */
  private List<Long> validateMenuIds(
    List<Long> ids) {
    // 缺省或空菜单列表均表示无授权；去重后再核对存在性和写入关联
    List<Long> menuIds = ids == null ? List.of() : ids.stream().distinct().toList();

    if (!menuIds.isEmpty()) {
      long existingCount = sysMenuService
        .count(new LambdaQueryWrapper<SysMenuEntity>().in(SysMenuEntity::getId, menuIds));
      if (existingCount != menuIds.size()) {
        throw new BusinessException(SysMenuEnum.ID_NOT_FOUND);
      }
    }

    return menuIds;
  }

  /**
   * 校验路径中的角色 ID，避免无意义的数据库查询和不明确的错误。
   *
   * @param id 角色主键
   */
  private void validateId(
    Long id) {
    if (id == null || id <= 0) {
      throw new BusinessException(SysRoleEnum.ROLE_NOT_EXIST);
    }
  }

  /**
   * 批量保存 角色与菜单的关系
   */
  private void insertRoleMenu(
    Long id,
    List<Long> menuIds) {
    for (Long menuId : menuIds) {
      if (sysRoleMenuMapper.insert(new SysRoleMenuEntity(id, menuId)) != 1) {
        throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
      }
    }
  }

  /**
   * 新增系统角色
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Void> addSysRole(
    SysRoleSaveDto args) {
    validateUnique(null, args);

    // 取出菜单 ids
    List<Long> menuIds = validateMenuIds(args.getMenuIds());
    SysRoleEntity role = BeanCopyUtils.beanCopy(args, SysRoleEntity.class);

    // 保存角色
    if (!save(role)) {
      throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
    }

    insertRoleMenu(role.getId(), menuIds);
    return ApiResponse.success();
  }

  /**
   * 修改系统角色
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Void> updateSysRoleById(
    Long id,
    SysRoleSaveDto args) {
    validateId(id);

    // 先确定角色 ID 是否存在
    if (!sysRoleMapper.exists(new LambdaQueryWrapper<SysRoleEntity>().eq(SysRoleEntity::getId, id))) {
      throw new BusinessException(SysRoleEnum.ROLE_NOT_EXIST);
    }

    // 校验
    validateUnique(id, args);
    List<Long> menuIds = validateMenuIds(args.getMenuIds());
    // args 转换成实体类
    SysRoleEntity role = BeanCopyUtils.beanCopy(args, SysRoleEntity.class);
    role.setId(id);
    // 更新角色信息
    if (!updateById(role)) {
      throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
    }
    // 删除旧的菜单关联关系
    sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenuEntity>().eq(SysRoleMenuEntity::getRoleId, id));

    insertRoleMenu(role.getId(), menuIds);
    return ApiResponse.success();
  }

  /**
   * 删除系统角色
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Void> deleteSysRoleById(
    Long id) {
    validateId(id);
    // 角色 ID 是否存在
    if (!sysRoleMapper.exists(new LambdaQueryWrapper<SysRoleEntity>().eq(SysRoleEntity::getId, id))) {
      throw new BusinessException(SysRoleEnum.ROLE_NOT_EXIST);
    }

    boolean roleInUse = sysUserRoleMapper
      .exists(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getRoleId, id));
    if (roleInUse) {
      throw new BusinessException(SysRoleEnum.ROLE_IN_USE);
    }

    // 先删除角色关联的菜单
    LambdaQueryWrapper<SysRoleMenuEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SysRoleMenuEntity::getRoleId, id);
    sysRoleMenuMapper.delete(wrapper);

    // 删除角色
    if (!removeById(id)) {
      throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
    }
    return ApiResponse.success();
  }

  /**
   * 查询所有角色（非分页模式）
   */
  @Override
  public ApiResponse<List<SysRoleOptionVo>> getSysRoleOptions() {
    LambdaQueryWrapper<SysRoleEntity> wrapper = new LambdaQueryWrapper<>();
    // 选项仅查询展示所需列，并在排序值相同时用主键稳定排序
    wrapper.select(SysRoleEntity::getId, SysRoleEntity::getName, SysRoleEntity::getCode, SysRoleEntity::getStatus,
      SysRoleEntity::getSort).orderByAsc(SysRoleEntity::getSort).orderByAsc(SysRoleEntity::getId);
    // mapper 查询数据库
    List<SysRoleEntity> roleList = sysRoleMapper.selectList(wrapper);
    // SysRoleEntity 转 SysRoleOptionVo
    List<SysRoleOptionVo> optionVos = BeanCopyUtils.beanListCopy(roleList, SysRoleOptionVo.class);
    // 响应
    return ApiResponse.success(optionVos);
  }
}
