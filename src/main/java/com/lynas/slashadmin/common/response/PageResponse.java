package com.lynas.slashadmin.common.response;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询返回给客户端的数据结构。
 *
 * <p>该对象只描述当前页的数据和匹配记录总数；页码、页大小等查询条件由对应的请求参数承载，避免在响应中重复表达。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
  /** 当前页记录；无结果时使用空列表。 */
  private List<T> records;
  /** 满足查询条件的总记录数，不受当前页大小限制。 */
  private Long total;
}
