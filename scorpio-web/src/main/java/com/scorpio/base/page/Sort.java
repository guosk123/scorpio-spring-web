package com.scorpio.base.page;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

public class Sort implements Iterable<Sort.Order> {

  public static final Direction DEFAULT_DIRECTION = Direction.ASC;

  private final List<Order> orders;

  /**
   * Creates a new {@link Sort} instance using the given {@link Order}s.
   *
   * @param orders must not be {@literal null}.
   */
  public Sort(Order... orders) {
    this(Arrays.asList(orders));
  }

  /**
   * Creates a new {@link Sort} instance.
   *
   * @param orders must not be {@literal null} or contain {@literal null}.
   */
  public Sort(List<Order> orders) {

    if (null == orders || orders.isEmpty()) {
      throw new IllegalArgumentException(
          "You have to provide at least one sort property to sort by!");
    }

    this.orders = orders;
  }

  /**
   * Creates a new {@link Sort} instance. Order defaults to {@value Direction#ASC}.
   *
   * @param properties must not be {@literal null} or contain {@literal null} or empty strings
   */
  public Sort(String... properties) {
    this(DEFAULT_DIRECTION, properties);
  }

  /**
   * Creates a new {@link Sort} instance.
   *
   * @param direction defaults to {@linke Sort#DEFAULT_DIRECTION} (for {@literal null} cases, too)
   * @param properties must not be {@literal null} or contain {@literal null} or empty strings
   */
  public Sort(Direction direction, String... properties) {
    this(direction,
        properties == null ? Lists.newArrayListWithCapacity(0) : Arrays.asList(properties));
  }

  /**
   * Creates a new {@link Sort} instance.
   *
   * @param direction
   * @param properties
   */
  public Sort(Direction direction, List<String> properties) {

    if (properties == null || properties.isEmpty()) {
      throw new IllegalArgumentException("You have to provide at least one property to sort by!");
    }

    this.orders = Lists.newArrayListWithCapacity(properties.size());
    for (String property : properties) {
      this.orders.add(new Order(direction, property));
    }
  }

  /**
   * Returns a new {@link Sort} consisting of the {@link Order}s of the current {@link Sort} combined with the given
   * ones.
   *
   * @param sort can be {@literal null}.
   * @return
   */
  public Sort and(Iterable<Order> sort) {

    if (sort == null) {
      return this;
    }

    ArrayList<Order> these = Lists.newArrayList(this.orders);
    for (Order order : sort) {
      these.add(order);
    }
    return new Sort(these);
  }

  /**
   * Returns the order registered for the given property.
   *
   * @param property
   * @return
   */
  public Order getOrderFor(String property) {

    for (Order order : this) {
      if (order.getProperty().equals(property)) {
        return order;
      }
    }

    return null;
  }

  public Iterator<Order> iterator() {
    return this.orders.iterator();
  }

  @Override
  public String toString() {
    return StringUtils.collectionToCommaDelimitedString(orders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orders);
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
    Sort other = (Sort) obj;
    return Objects.equals(orders, other.orders);
  }

  /**
   * Enumeration for sort directions.
   *
   * @author Oliver Gierke
   */
  public enum Direction {

    ASC, DESC;

    /**
     * Returns the {@link Direction} enum for the given {@link String} value.
     *
     * @param value
     * @return
     */
    public static Direction fromString(String value) {
      return Direction.valueOf(value.toUpperCase(Locale.US));
    }
  }

  /**
   * PropertyPath implements the pairing of an {@link Direction} and a property. It is used to provide input for
   * {@link Sort}
   *
   * @author Oliver Gierke
   */
  public static class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Direction direction;
    private final String property;

    /**
     * Creates a new {@link Order} instance. if order is {@literal null} then order defaults to
     * {@link Sort#DEFAULT_DIRECTION}
     *
     * @param direction can be {@literal null}, will default to {@link Sort#DEFAULT_DIRECTION}
     * @param property must not be {@literal null} or empty.
     */
    public Order(Direction direction, String property) {

      if (!StringUtils.hasText(property)) {
        throw new IllegalArgumentException("Property must not null or empty!");
      }

      this.direction = direction == null ? DEFAULT_DIRECTION : direction;
      this.property = property;
    }

    /**
     * Creates a new {@link Order} instance. Takes a single property. Direction defaults to
     * {@link Sort#DEFAULT_DIRECTION}.
     *
     * @param property must not be {@literal null} or empty.
     */
    public Order(String property) {
      this(DEFAULT_DIRECTION, property);
    }

    /**
     * Returns the order the property shall be sorted for.
     *
     * @return
     */
    public Direction getDirection() {
      return direction;
    }

    /**
     * Returns the property to order for.
     *
     * @return
     */
    public String getProperty() {
      return property;
    }

    /**
     * Returns whether sorting for this property shall be ascending.
     *
     * @return
     */
    public boolean isAscending() {
      return this.direction == Direction.ASC;
    }

    /**
     * Returns a new {@link Order} with the given {@link Order}.
     *
     * @param order
     * @return
     */
    public Order with(Direction order) {
      return new Order(order, this.property);
    }

    /**
     * Returns a new {@link Sort} instance for the given properties.
     *
     * @param properties
     * @return
     */
    public Sort withProperties(String... properties) {
      return new Sort(this.direction, properties);
    }

    @Override
    public String toString() {
      return String.format("%s: %s", property, direction);
    }

    @Override
    public int hashCode() {
      return Objects.hash(direction, property);
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
      Order other = (Order) obj;
      return direction == other.direction && Objects.equals(property, other.property);
    }

  }
}
