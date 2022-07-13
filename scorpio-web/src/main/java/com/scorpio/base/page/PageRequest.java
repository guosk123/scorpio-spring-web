package com.scorpio.base.page;

import java.util.Objects;

public class PageRequest implements Pageable {
  
  private static final int MAX_PAGE_SIZE = 1000; 
  
  private final int page;
  private final int size;
  private final Sort sort;

  /**
   * Creates a new {@link PageRequest}. Pages are zero indexed, thus providing
   * 0 for {@code page} will return the first page.
   *
   * @param size
   * @param page
   */
  public PageRequest(int page, int size) {

    this(page, size, null);
  }

  /**
   * Creates a new {@link PageRequest} with sort parameters applied.
   *
   * @param page
   * @param size
   * @param direction
   * @param properties
   */
  public PageRequest(int page, int size, Sort.Direction direction, String... properties) {

    this(page, size, new Sort(direction, properties));
  }

  /**
   * Creates a new {@link PageRequest} with sort parameters applied.
   *
   * @param page
   * @param size
   * @param sort
   */
  public PageRequest(int page, int size, Sort sort) {

    if (0 > page) {
      throw new IllegalArgumentException("Page index must not be less than zero!");
    }

    if (0 >= size) {
      throw new IllegalArgumentException("Page size must not be less than or equal to zero!");
    }

    this.page = page;
    this.size = size > MAX_PAGE_SIZE ? MAX_PAGE_SIZE : size;
    this.sort = sort;
  }

  public int getPageSize() {

    return size;
  }

  public int getPageNumber() {

    return page;
  }

  public int getOffset() {

    return page * size;
  }

  public Sort getSort() {

    return sort;
  }

  @Override
  public int hashCode() {
    return Objects.hash(page, size, sort);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PageRequest other = (PageRequest) obj;
    return page == other.page && size == other.size && Objects.equals(sort, other.sort);
  }

}
