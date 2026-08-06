package com.lynas.slashadmin.common.response;

import com.lynas.slashadmin.common.enums.ResponseCodeEnum;
import lombok.Data;

/**
 * HTTP 接口返回给客户端的统一响应结构。
 *
 * <p>控制器应使用本类包装业务结果，客户端据 {@link #code} 判断请求是否成功，再读取 {@link #data} 或展示 {@link #message}。{@link Data}
 * 由 Lombok 在编译时生成 getter、setter 等方法，避免手写重复代码。
 *
 * @param <T> 成功响应中 data 字段承载的数据类型；失败响应通常为 {@code Void}
 */
@Data
public class ApiResponse<T> {

  /** 业务响应码；它描述接口处理结果，不等同于 HTTP 状态码。 */
  private Integer code;

  /** 供客户端展示或排查问题的响应说明。 */
  private String message;

  /** 业务数据；发生错误时为 {@code null}。 */
  private T data;

  /**
   * 创建一个完整的响应对象。
   *
   * <p>通常优先调用 {@link #success(Object)} 或 {@link #error(ResponseCodeEnum)}，使常用响应码集中维护；
   * 只有错误信息需按请求动态生成时才调用 {@link #error(Integer, String)}。
   *
   * @param code 业务响应码
   * @param message 响应说明
   * @param data 返回给客户端的业务数据
   */
  public ApiResponse(Integer code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  /**
   * 创建携带业务数据的成功响应。
   *
   * @param data 返回给客户端的业务数据
   * @param <T> 业务数据类型
   * @return 使用 {@link ResponseCodeEnum#SUCCESS} 的成功响应
   */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(
        ResponseCodeEnum.SUCCESS.getCode(), ResponseCodeEnum.SUCCESS.getMessage(), data);
  }

  /**
   * 根据预定义错误码创建失败响应。
   *
   * @param code 已在 {@link ResponseCodeEnum} 中定义的错误码和文案
   * @param <T> 响应数据类型
   * @return data 为 {@code null} 的失败响应
   */
  public static <T> ApiResponse<T> error(ResponseCodeEnum code) {
    return new ApiResponse<>(code.getCode(), code.getMessage(), null);
  }

  /**
   * 根据调用方提供的错误码和文案创建失败响应。
   *
   * <p>适用于校验失败等需要返回具体错误信息的场景；固定的业务错误应优先使用 {@link #error(ResponseCodeEnum)}，避免错误码和文案散落在各个调用处。
   *
   * @param code 业务响应码
   * @param message 本次请求对应的错误说明
   * @param <T> 响应数据类型
   * @return data 为 {@code null} 的失败响应
   */
  public static <T> ApiResponse<T> error(Integer code, String message) {
    return new ApiResponse<>(code, message, null);
  }
}
