package com.scorpio.base.page;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

public class PageImpl<T> implements Page<T> {

  private final List<T> content = Lists.newArrayListWithCapacity(0);
  private final Pageable pageable;
  private final long total;

  /**
   * Constructor of {@code PageImpl}.
   *
   * @param content the content of this page, must not be {@literal null}.
   * @param pageable the paging information, can be {@literal null}.
   * @param total the total amount of items available
   */
  public PageImpl(List<T> content, Pageable pageable, long total) {

    if (null == content) {
      throw new IllegalArgumentException("Content must not be null!");
    }

    this.content.addAll(content);
    this.total = total;
    this.pageable = pageable;
  }

  /**
   * Creates a new {@link PageImpl} with the given content. This will result in the created {@link Page} being identical
   * to the entire {@link List}.
   *
   * @param content must not be {@literal null}.
   */
  public PageImpl(List<T> content) {
    this(content, null, null == content ? 0 : content.size());
  }

  /**
   * @see com.machloop.alpha.common.base.page.Page#getNumber()
   */
  public int getNumber() {
    return pageable == null ? 0 : pageable.getPageNumber();
  }

  /**
   * @see com.machloop.alpha.common.base.page.Page#getSize()
   */
  public int getSize() {
    return pageable == null ? 0 : pageable.getPageSize();
  }

  /**
   * @see com.machloop.alpha.common.base.page.Page#getTotalPages()
   */
  public int getTotalPages() {
    return getSize() == 0 ? 0 : (int) Math.ceil((double) total / (double) getSize());
  }

  /**
   * @see com.machloop.alpha.common.base.page.Page#getTotalElements()
   */
  public long getTotalElements() {
    return total;
  }

  /**
   * @see com.machloop.alpha.common.base.page.Page#iterator()
   */
  public Iterator<T> iterator() {
    return content.iterator();
  }

  /**
   * @see com.machloop.alpha.common.base.page.Page#getContent()
   */
  public List<T> getContent() {
    return Collections.unmodifiableList(content);
  }

  /**
   * @see com.machloop.alpha.common.base.page.Page#getSort()
   */
  public Sort getSort() {
    return pageable == null ? null : pageable.getSort();
  }

  @Override
  public String toString() {

    String contentType = "UNKNOWN";

    if (!content.isEmpty()) {
      contentType = content.get(0).getClass().getName();
    }

    return String.format("Page %s of %d containing %s instances", getNumber(), getTotalPages(),
        contentType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, pageable, total);
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
    PageImpl<?> other = (PageImpl<?>) obj;
    return Objects.equals(content, other.content) && Objects.equals(pageable, other.pageable)
        && total == other.total;
  }

}
