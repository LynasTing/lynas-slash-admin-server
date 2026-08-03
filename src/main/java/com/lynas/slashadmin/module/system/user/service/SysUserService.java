package com.lynas.slashadmin.module.system.user.service;

import com.lynas.slashadmin.module.system.user.dto.SysUserResponse;
import java.util.List;

/** 系统用户基础查询服务。 */
public interface SysUserService {

  List<SysUserResponse> listUsers();
}
