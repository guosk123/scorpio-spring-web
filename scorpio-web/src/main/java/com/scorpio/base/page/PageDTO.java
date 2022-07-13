package com.scorpio.base.page;

import com.google.common.collect.Lists;
import com.scorpio.Constants;
import org.apache.commons.collections.CollectionUtils;

import java.util.Iterator;
import java.util.List;

public class PageDTO<T> {

  private List<T> content = Lists.newArrayListWithCapacity(0);
  private long total;
  private int pageNum;
  private int pageSize;
  private List<OrderDTO> pageOrders;

  public PageDTO() {
  }

  public PageDTO(Page<T> page) {
    this.total = page.getTotalElements();
    this.content = page.getContent();
    this.pageNum = page.getNumber();
    this.pageSize = page.getSize();

    this.pageOrders = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (page.getSort() != null) {
      Iterator<Sort.Order> orderIt = page.getSort().iterator();
      while (orderIt.hasNext()) {
        OrderDTO orderDto = new OrderDTO(orderIt.next());
        pageOrders.add(orderDto);
      }
    }
  }

  public Page<T> toPage() {

    List<Sort.Order> orderList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (OrderDTO orderDto : pageOrders) {
      orderList.add(new Sort.Order(orderDto.getDirection(), orderDto.property));
    }
    PageRequest pageReq = null;
    if (CollectionUtils.isNotEmpty(orderList)) {
      pageReq = new PageRequest(pageNum, pageSize, new Sort(orderList));
    } else {
      pageReq = new PageRequest(pageNum, pageSize);
    }
    return new PageImpl<>(this.content, pageReq, this.total);
  }

  public List<T> getContent() {
    return content;
  }

  public void setContent(List<T> content) {
    this.content = content;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public int getPageNum() {
    return pageNum;
  }

  public void setPageNum(int pageNum) {
    this.pageNum = pageNum;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public List<OrderDTO> getPageOrders() {
    return pageOrders;
  }

  public void setPageOrders(List<OrderDTO> pageOrders) {
    this.pageOrders = pageOrders;
  }

  private int getTotalPages() {
    return pageSize == 0 ? 0 : (int) Math.ceil((double) total / (double) pageSize);
  }

  @Override
  public String toString() {

    String contentType = "UNKNOWN";

    if (!content.isEmpty()) {
      contentType = content.get(0).getClass().getName();
    }

    return String.format("APIPage %s of %d containing %s instances", getPageNum(), getTotalPages(),
        contentType);
  }

  public static class OrderDTO {

    private Sort.Direction direction;
    private String property;

    public OrderDTO() {

    }

    public OrderDTO(Sort.Order order) {
      this.property = order.getProperty();
      this.direction = order.getDirection();
    }

    public Sort.Direction getDirection() {
      return direction;
    }

    public void setDirection(Sort.Direction direction) {
      this.direction = direction;
    }

    public String getProperty() {
      return property;
    }

    public void setProperty(String property) {
      this.property = property;
    }

    @Override
    public String toString() {
      return String.format("%s: %s", property, direction);
    }
  }

}
