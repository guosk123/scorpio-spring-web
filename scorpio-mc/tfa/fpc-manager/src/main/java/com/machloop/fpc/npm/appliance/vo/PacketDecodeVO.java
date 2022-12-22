package com.machloop.fpc.npm.appliance.vo;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

/**
 * @author minjiajun
 *
 * create at 2022年9月1日, fpc-manager
 */
public class PacketDecodeVO {

  @NotEmpty(message = "类型名称不能为空")
  private String type;
  @NotEmpty(message = "payloads不能为空")
  private List<Map<String, Object>> payloads;

  @Override
  public String toString() {
    return "PacketDecodeVO [type=" + type + ", payloads=" + payloads + "]";
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<Map<String, Object>> getPayloads() {
    return payloads;
  }

  public void setPayloads(List<Map<String, Object>> payloads) {
    this.payloads = payloads;
  }

}
