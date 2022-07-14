package com.scorpio.dict;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.scorpio.Constants;

@XmlRootElement(name = "dict-definition")
public class DictDefinition implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<Dict> dicts = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

  private Map<String,
      Dict> dictMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);


  public List<Dict.Item> getItem(String key) {
    return dictMap.get(key) == null ? null : dictMap.get(key).getItems();
  }

  public Map<String, String> getItemMap(String key) {
    List<Dict.Item> items = getItem(key);

    // 防止空指针
    if (items == null) {
      return Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    }

    Map<String, String> result = Maps.newLinkedHashMapWithExpectedSize(items.size());
    for (Dict.Item item : items) {
      result.put(item.getKey(), item.getLabel());
    }
    return result;
  }

  @XmlElement(name = "dict")
  public List<Dict> getDicts() {
    return dicts;
  }

  public void setDicts(List<Dict> dicts) {
    this.dicts = dicts;
  }

  public Map<String, Dict> getDictMap() {
    return dictMap;
  }

  public void setDictMap(Map<String, Dict> dictMap) {
    this.dictMap = dictMap;
  }


}
