package com.scorpio.base.page;

import java.util.Iterator;
import java.util.List;

public interface Page<T> extends Iterable<T> {

  /**
   * Returns the number of the current page. 
   * Is always non-negative and less that {@code Page#getTotalPages()}.
   *
   * @return the number of the current page
   */
  int getNumber();

  /**
   * Returns the size of the page.
   *
   * @return the size of the page
   */
  int getSize();

  /**
   * Returns the number of total pages.
   *
   * @return the number of toral pages
   */
  int getTotalPages();

  /**
   * Returns the total amount of elements.
   *
   * @return the total amount of elements
   */
  long getTotalElements();

  /**
   *
   * @see Iterable#iterator()
   */
  Iterator<T> iterator();

  /**
   * Returns the page content as {@link List}.
   *
   * @return
   */
  List<T> getContent();

  /**
   * Returns the sorting parameters for the page.
   *
   * @return
   */
  Sort getSort();
}
