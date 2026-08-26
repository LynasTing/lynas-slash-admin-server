package com.lynas.slashadmin.system.menu.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增系统菜单的请求参数。
 *
 * <p>菜单主键、创建时间和更新时间由数据库维护，客户端不得传入。
 */
@Data
@Schema(description = "新增菜单入参")
public class SysMenuSaveDto {
  @NotNull(message = "父级菜单 ID 不能为空")
  @Min(value = 0, message = "父级菜单 ID 不能小于 0")
  @Schema(description = "父级菜单 ID，0 表示根节点", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long parentId;

  @NotBlank(message = "菜单名称不能为空")
  @Size(max = 64, message = "菜单名称不能超过 64 个字符")
  @Schema(description = "菜单名称", example = "系统管理", requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @NotBlank(message = "菜单编码不能为空")
  @Size(max = 128, message = "菜单编码不能超过 128 个字符")
  @Schema(description = "唯一编码", example = "system:role:list", requiredMode = Schema.RequiredMode.REQUIRED)
  private String code;

  @NotNull(message = "菜单类型不能为空")
  @Min(value = 1, message = "菜单类型必须在 1 到 4 之间")
  @Max(value = 4, message = "菜单类型必须在 1 到 4 之间")
  @Schema(description = "节点类型：1 分组，2 目录，3 菜单，4 操作按钮", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer category;

  @NotNull(message = "排序值不能为空")
  @Min(value = 0, message = "排序值不能小于 0")
  @Schema(description = "显示顺序", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer sort;

  @NotNull(message = "菜单状态不能为空")
  @Min(value = 0, message = "菜单状态只能为 0 或 1")
  @Max(value = 1, message = "菜单状态只能为 0 或 1")
  @Schema(description = "启用状态：0 禁用，1 启用", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer status;

  @Size(max = 255, message = "路由路径不能超过 255 个字符")
  @Schema(description = "前端路由路径", example = "/system/role")
  private String path;

  @Size(max = 255, message = "组件标识不能超过 255 个字符")
  @Schema(description = "前端组件标识或组件路径", example = "system/role/index")
  private String component;

  @Size(max = 128, message = "菜单图标不能超过 128 个字符")
  @Schema(description = "菜单图标", example = "UserFilled")
  private String icon;

  @NotNull(message = "隐藏状态不能为空")
  @Min(value = 0, message = "隐藏状态只能为 0 或 1")
  @Max(value = 1, message = "隐藏状态只能为 0 或 1")
  @Schema(description = "是否隐藏：0 否，1 是", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer hidden;

  @Size(max = 500, message = "菜单描述不能超过 500 个字符")
  @Schema(description = "菜单描述", example = "系统角色管理页面")
  private String description;

  @Size(max = 500, message = "外链地址不能超过 500 个字符")
  @Schema(description = "外链地址", example = "https:/" + "/example.com/docs")
  private String externalLink;
}
