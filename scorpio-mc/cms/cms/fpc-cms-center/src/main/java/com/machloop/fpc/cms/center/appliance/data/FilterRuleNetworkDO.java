package com.machloop.fpc.cms.center.appliance.data;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/9/29 2:00 PM,cms
 * @version 1.0
 */
public class FilterRuleNetworkDO {

  private String id;

  private String filterRuleId;

  private String networkId;

  private String networkGroupId;

  @Override
  public String toString() {
    return "FilterRuleIPOrIPHostGroupDO[ id=" + id + ", filterRuleId=" + filterRuleId
        + ", networkId=" + networkId + ", networkGroupId=" + networkGroupId + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFilterRuleId() {
    return filterRuleId;
  }

  public void setFilterRuleId(String filterRuleId) {
    this.filterRuleId = filterRuleId;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getNetworkGroupId() {
    return networkGroupId;
  }

  public void setNetworkGroupId(String networkGroupId) {
    this.networkGroupId = networkGroupId;
  }
}
