package com.lynas.slashadmin.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用分页查询参数。
 */
@Data
@NoArgsConstructor
@Schema(description = "分页查询参数")
public class PageQuery {
  @Min(value = 1, message = "页码必须大于或等于 1")
  @Schema(description = "当前页码，从 1 开始", example = "1", defaultValue = "1", minimum = "1")
  private Integer pageNum = 1;

  @Min(value = 1, message = "每页记录数必须大于或等于 1")
  @Max(value = 999, message = "每页记录数不能超过 999")
  @Schema(description = "每页记录数，取值范围为 1 到 999", example = "10", defaultValue = "10", minimum = "1", maximum = "999")
  private Integer pageSize = 10;
}
