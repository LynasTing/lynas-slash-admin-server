package com.lynas.slashadmin.common.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.lynas.slashadmin.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Web 层的全局异常处理器：将控制器执行期间抛出的指定异常转换成统一的 {@link ApiResponse}。
 *
 * <p>
 * {@link RestControllerAdvice} 是 {@code @ControllerAdvice} 与
 * {@code @ResponseBody} 的组合注解。Spring Boot 启动时会扫描并注册此类；任意
 * {@code @RestController} 的请求处理方法抛出异常后，Spring 会按异常类型寻找 {@link ExceptionHandler}
 * 方法，而不是直接把异常堆栈返回给客户端。
 *
 * <p>
 * 本类同时定义两层结果：{@link ResponseEntity} 的 HTTP
 * 状态码供网关、浏览器和客户端判断请求状态；{@link ApiResponse#code} 与 {@link ApiResponse#message}
 * 提供 JSON 响应体中的业务信息。两者当前使用相同数值，避免客户端 必须理解两套状态含义。
 *
 * <p>
 * 异常处理器应只返回可安全公开的信息，不能把 SQL、堆栈、连接字符串或内部类名交给客户端。本类尚未记录异常日志； 生产环境应配合日志记录或监控告警，否则
 * 500 错误的根因难以排查。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * 处理无法解析的请求体。
   *
   * <p>Jackson 严格模式下，DTO 中未声明的字段会被视为请求格式错误，而不是服务端故障。
   *
   * @param exception Spring 封装的请求体解析异常
   * @param request 当前 HTTP 请求
   * @return HTTP 400，响应体说明请求体格式错误或未识别字段
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
    HttpMessageNotReadableException exception,
    HttpServletRequest request) {
    String message = resolveRequestBodyMessage(exception);
    LOGGER.warn("请求体解析失败: method={}, uri={}, message={}", request.getMethod(), request.getRequestURI(), message);
    return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message));
  }

  /**
   * 处理控制器参数校验失败。
   *
   * <p>
   * 当请求对象的字段使用 {@code @NotBlank}、{@code @Size} 等 Bean Validation 注解，并且控制器参数使用
   * {@code @Valid} 触发校验时，Spring 会在调用控制器方法前抛出
   * {@link MethodArgumentNotValidException}。因此业务方法 本身通常不会开始执行。
   *
   * <p>
   * 一个请求可能有多个字段错误。这里有意只取第一个错误，给客户端一个简洁提示；若产品需要一次展示全部错误，需改为 收集所有
   * {@code FieldError} 后返回列表。字段未配置校验文案时，使用默认文案，避免响应出现 {@code null}。
   *
   * @param exception Spring 封装的参数绑定与校验结果
   * @return HTTP 400，响应体的 data 为 {@code null}，message 为第一个字段错误的安全提示
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(
    MethodArgumentNotValidException exception) {
    // BindingResult 保存本次参数绑定和校验产生的全部错误；本接口约定只向客户端返回第一条。
    String message = exception.getBindingResult().getFieldErrors().stream().findFirst()
      .map(error -> error.getDefaultMessage() == null ? "请求参数不合法" : error.getDefaultMessage()).orElse("请求参数不合法");
    // ResponseEntity 设置 HTTP 状态；ApiResponse 设置 JSON 响应体，两者均使用 400 表示客户端请求有误。
    return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message));
  }

  /**
   * 处理数据库完整性约束冲突。
   *
   * <p>
   * {@link DataIntegrityViolationException} 常见于唯一索引重复、外键引用不存在或删除仍被引用的数据。底层数据库的
   * 原始报错通常包含表名、字段名和 SQL 片段，不应直接暴露给客户端。
   *
   * @return HTTP 409，表示请求格式合法，但当前资源状态与该操作冲突
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
    HttpServletRequest request) {
    LOGGER.warn("数据约束冲突: method={}, uri={}", request.getMethod(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(HttpStatus.CONFLICT.value(), "数据约束冲突"));
  }

  /**
   * 处理数据库访问失败。
   *
   * <p>数据库连接、SQL 语句或表结构异常属于服务端运行环境问题，详细堆栈仅写入服务端日志。
   *
   * @param exception 数据库访问异常
   * @param request 当前 HTTP 请求
   * @return HTTP 500，避免向客户端暴露数据库内部信息
   */
  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataAccessException(
    DataAccessException exception,
    HttpServletRequest request) {
    LOGGER.error("数据库访问失败: method={}, uri={}", request.getMethod(), request.getRequestURI(), exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误"));
  }

  /**
   * 业务报错
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(
    BusinessException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(exception.getResponseCode()));
  }

  /**
   * 处理未被更具体处理器捕获的异常。
   *
   * <p>
   * {@code Exception.class} 的匹配范围很宽，因此它相当于本类的最后一道防线。Spring 会优先选择更具体的异常类型，
   * 所以参数校验和数据完整性异常仍会由前两个方法处理。
   *
   * @return HTTP 500，客户端只收到通用提示；详细异常应留在服务端日志中
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
    Exception exception,
    HttpServletRequest request) {
    LOGGER.error("未处理的服务端异常: method={}, uri={}", request.getMethod(), request.getRequestURI(), exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误"));
  }

  /**
   * 从 Jackson 根因中提取可安全返回给客户端的请求体错误。
   *
   * @param exception Spring 请求体解析异常
   * @return 面向客户端的安全错误说明
   */
  private String resolveRequestBodyMessage(
    HttpMessageNotReadableException exception) {
    Throwable cause = exception.getCause();
    while (cause != null) {
      if (cause instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
        return "包含未识别字段：" + unrecognizedPropertyException.getPropertyName();
      }
      cause = cause.getCause();
    }
    return "请求体格式不正确";
  }
}
