package com.scorpio.dict;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.scorpio.Constants;

public class Dict implements Serializable {

  private static final long serialVersionUID = 1L;

  private String key;
  private String label;
  private List<Item> items = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

  private Map<String,
      Item> itemMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  public Dict() {
    // do nothing
  }

  @XmlAttribute
  public String getKey() {
    return key;
  }

  public void setKey(String dictKey) {
    this.key = dictKey;
  }

  @XmlAttribute
  public String getLabel() {
    return label;
  }

  public void setLabel(String dictLabel) {
    this.label = dictLabel;
  }

  @XmlElement(name = "item")
  public List<Item> getItems() {
    return items;
  }

  public List<Item> getItems(String... exclude) {
    if (items != null) {
      List<Item> result = Lists.newArrayListWithCapacity(items.size());
      List<String> excludes = Arrays.asList(exclude);
      for (Item item : items) {
        if (excludes.contains(item.getKey())) {
          continue;
        }
        result.add(item);
      }
      return result;
    }

    return items;
  }

  public void setItems(List<Item> items) {
    for (Item item : items) {
      itemMap.put(item.getKey(), item);
    }
    this.items = items;
  }

  public Map<String, Item> getItemMap() {
    return itemMap;
  }

  public void setItemMap(Map<String, Item> itemMap) {
    this.itemMap = itemMap;
  }

  public static class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;
    private String label;
    private Item parent;
    private List<Item> children = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    private Map<String,
        Item> childrenMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    @XmlAttribute
    public String getKey() {
      return key;
    }

    public void setKey(String itemKey) {
      this.key = itemKey;
    }

    @XmlAttribute
    public String getLabel() {
      return label;
    }

    public void setLabel(String itemLabel) {
      this.label = itemLabel;
    }

    public Item getParent() {
      return parent;
    }

    public void setParent(Item parent) {
      this.parent = parent;
    }

    @XmlElement(name = "item")
    public List<Item> getChildren() {
      return children;
    }

    public void setChildren(List<Item> childrenItems) {
      for (Item childItem : childrenItems) {
        childItem.setParent(this);
        childrenMap.put(childItem.getKey(), childItem);
      }
      this.children = childrenItems;
    }
  }

}
