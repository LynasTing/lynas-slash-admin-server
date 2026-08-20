package com.lynas.slashadmin.common.response;
import com.lynas.slashadmin.common.enums.ResponseCode;
import com.lynas.slashadmin.common.enums.ResponseCodeEnum;
import lombok.Data;

/**
 * HTTP 接口返回给客户端的统一响应结构。
 *
 * <p>控制器使用本类包装业务结果。HTTP 状态码描述协议层结果，{@code code} 描述本项目的业务结果，客户端据此决定后续处理。
 */
@Data
public class ApiResponse<T> {
  /**
   * 业务响应码；客户端据此判断请求处理结果。
   */
  private Integer code;
  /**
   * 可安全展示给客户端的处理说明。
   */
  private String message;
  /**
   * 成功响应承载的业务数据；失败响应通常为 {@code null}。
   */
  private T data;
  public ApiResponse(Integer code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  /**
   * 无数据返回的成功响应
   */
  public static ApiResponse<Void> success() {
    return new ApiResponse<>(ResponseCodeEnum.SUCCESS.getCode(), ResponseCodeEnum.SUCCESS.getMessage(), null);
  }

  /**
   * 创建携带业务数据的成功响应。
   *
   * <p>
   * 响应码与默认文案从 {@link ResponseCodeEnum} 读取，避免每个 Controller 重复书写魔法数字。
   *
   * @param data 返回给客户端的业务数据
   * @param <T>  业务数据类型
   * @return 使用 {@link ResponseCodeEnum#SUCCESS} 的成功响应
   */
  public static <T> ApiResponse<T> success(
    T data) {
    return new ApiResponse<>(ResponseCodeEnum.SUCCESS.getCode(), ResponseCodeEnum.SUCCESS.getMessage(), data);
  }

  /**
   * 根据预定义错误码创建失败响应。
   *
   * @param responseCode 任意实现 {@link ResponseCode} 的错误码和文案
   * @param <T>  响应数据类型
   * @return data 为 {@code null} 的失败响应
   */
  public static <T> ApiResponse<T> error(
    ResponseCode responseCode) {
    return new ApiResponse<>(responseCode.getCode(), responseCode.getMessage(), null);
  }

  /**
   * 根据调用方提供的错误码和文案创建失败响应。
   *
   * <p>
   * 适用于校验失败等需要返回具体错误信息的场景；固定的业务错误应优先使用
   * {@link #error(ResponseCode)}，避免错误码和文案散落在各个调用处。 调用方只能传入对客户端安全的文案，不能把
   * SQL、异常堆栈或敏感数据直接放入 {@code message}。
   *
   * @param code    业务响应码
   * @param message 本次请求对应的错误说明
   * @param <T>     响应数据类型
   * @return data 为 {@code null} 的失败响应
   */
  public static <T> ApiResponse<T> error(
    Integer code,
    String message) {
    return new ApiResponse<>(code, message, null);
  }
}
