package com.lynas.slashadmin.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.lynas.slashadmin.common.enums.ResponseCodeEnum;
import com.lynas.slashadmin.common.exception.BusinessException;
import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.common.response.PageResponse;
import com.lynas.slashadmin.common.utils.BeanCopyUtils;
import com.lynas.slashadmin.system.menu.dto.SysMenuPageQuery;
import com.lynas.slashadmin.system.menu.dto.SysMenuSaveDto;
import com.lynas.slashadmin.system.menu.entity.SysMenuEntity;
import com.lynas.slashadmin.system.menu.enums.SysMenuEnum;
import com.lynas.slashadmin.system.menu.mapper.SysMenuMapper;
import com.lynas.slashadmin.system.menu.service.SysMenuService;
import com.lynas.slashadmin.system.menu.vo.SysMenuListVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {
  /**
   * 分页获取菜单列表
   */
  @Override
  public ApiResponse<PageResponse<SysMenuListVo>> getSysMenuList(
    SysMenuPageQuery query) {
    LambdaQueryWrapper<SysMenuEntity> wrapper = new LambdaQueryWrapper<>();
    Page<SysMenuEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
    page(page, wrapper);
    List<SysMenuListVo> sysMenuListVos = BeanCopyUtils.beanListCopy(page.getRecords(), SysMenuListVo.class);
    return ApiResponse.success(new PageResponse<>(sysMenuListVos, page.getTotal()));
  }

  /**
   * 新增/修改时校验重复信息
   */
  private void validateUnique(
    Long id,
    SysMenuSaveDto args) {
    boolean nameExists = lambdaQuery().eq(SysMenuEntity::getName, args.getName())
      .ne(id != null, SysMenuEntity::getId, id).exists();
    if (nameExists) {
      throw new BusinessException(SysMenuEnum.NAME_ALREADY_EXISTS);
    }

    boolean codeExists = lambdaQuery().eq(SysMenuEntity::getCode, args.getCode())
      .ne(id != null, SysMenuEntity::getId, id).exists();
    if (codeExists) {
      throw new BusinessException(SysMenuEnum.CODE_ALREADY_EXISTS);
    }
  }

  /**
   * 新增菜单
   */
  @Override
  public ApiResponse<Void> addSysMenu(
    SysMenuSaveDto args) {
    validateUnique(null, args);
    SysMenuEntity menu = BeanCopyUtils.beanCopy(args, SysMenuEntity.class);
    if (!save(menu)) {
      throw new IllegalStateException(ResponseCodeEnum.SYSTEM_ERROR.getMessage());
    }
    return ApiResponse.success();
  }

  /**
   * 更新菜单
   */
  @Override
  public ApiResponse<Void> updateSysMenuById(
    Long id,
    SysMenuSaveDto args) {
    validateUnique(id, args);
    SysMenuEntity menu = BeanCopyUtils.beanCopy(args, SysMenuEntity.class);
    menu.setId(id);
    if (!updateById(menu)) {
      throw new IllegalStateException(ResponseCodeEnum.SYSTEM_ERROR.getMessage());
    }
    return ApiResponse.success();
  }

  /**
   * 删除菜单
   */
  @Override
  public ApiResponse<Void> deleteSysMenuById(
    Long id) {
    if (Objects.isNull(id)) {
      throw new BusinessException(ResponseCodeEnum.ID_IS_NULL);
    }
    if (!removeById(id)) {
      throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
    }
    return ApiResponse.success();
  }
}
