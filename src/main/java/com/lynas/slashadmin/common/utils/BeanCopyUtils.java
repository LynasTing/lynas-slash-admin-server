package com.lynas.slashadmin.common.utils;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用于 Bean 复制操作的实例创建工具。
 */
public class BeanCopyUtils {
  /**
   * 通过 {@code type} 的无参构造器创建目标对象实例。
   *
   * <p>当前方法仅创建目标对象，尚未复制 {@code source} 的属性。
   *
   * @param source 源对象，为后续属性复制预留
   * @param type   目标 Bean 类型，必须提供可访问的无参构造器
   * @param <T>    目标 Bean 类型
   * @return 新创建的目标对象；创建失败时返回 {@code null}
   */
  public static <T> T beanCopy(
    Object source,
    Class<T> type) {
    // 实例创建与属性复制分离，避免创建失败时返回部分填充的对象。
    T result = null;
    try {
      result = type.getDeclaredConstructor().newInstance();
      BeanUtils.copyProperties(source, result);
    } catch (Exception err) {
      err.printStackTrace();
    }
    return result;
  }

  public static <K, T> List<T> beanListCopy(
    List<K> sourceList,
    Class<T> type) {
    return sourceList.stream().map(item -> beanCopy(item, type)).collect(Collectors.toList());
  }
}
