package com.machloop.fpc.cms.center.restapi.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author guosk
 *
 * create at 2021年6月26日, fpc-manager
 */
public class IngestPolicyVO {

  @Length(min = 1, max = 30, message = "策略名称不能为空，最多可输入30个字符")
  private String name;
  @Digits(integer = 1, fraction = 0, message = "策略选项格式不正确")
  @Range(min = 0, max = 1, message = "策略选项格式不正确")
  @NotEmpty(message = "策略不能为空")
  private String defaultAction;
  @Digits(integer = 1, fraction = 0, message = "报文去重选项格式不正确")
  @Range(min = 0, max = 1, message = "报文去重选项格式不正确")
  @NotEmpty(message = "报文去重选项不能为空")
  private String deduplication;
  @Length(max = 1024, message = "bpf长度不在可允许范围内")
  private String exceptBpf;
  private List<FilterTupleBO> exceptTuple;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "IngestPolicyVO [name=" + name + ", defaultAction=" + defaultAction + ", deduplication="
        + deduplication + ", exceptBpf=" + exceptBpf + ", exceptTuple=" + exceptTuple
        + ", description=" + description + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDefaultAction() {
    return defaultAction;
  }

  public void setDefaultAction(String defaultAction) {
    this.defaultAction = defaultAction;
  }

  public String getDeduplication() {
    return deduplication;
  }

  public void setDeduplication(String deduplication) {
    this.deduplication = deduplication;
  }

  public String getExceptBpf() {
    return exceptBpf;
  }

  public void setExceptBpf(String exceptBpf) {
    this.exceptBpf = exceptBpf;
  }

  public List<FilterTupleBO> getExceptTuple() {
    return exceptTuple;
  }

  public void setExceptTuple(List<FilterTupleBO> exceptTuple) {
    this.exceptTuple = exceptTuple;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public static class FilterTupleBO {

    private String sourceIp;
    private String sourcePort;
    private String destIp;
    private String destPort;
    private String protocol;
    private String vlanId;

    @Override
    public String toString() {
      return "FilterTupleBO [sourceIp=" + sourceIp + ", sourcePort=" + sourcePort + ", destIp="
          + destIp + ", destPort=" + destPort + ", protocol=" + protocol + ", vlanId=" + vlanId
          + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((sourceIp == null) ? 0 : sourceIp.hashCode());
      result = prime * result + ((sourcePort == null) ? 0 : sourcePort.hashCode());
      result = prime * result + ((destIp == null) ? 0 : destIp.hashCode());
      result = prime * result + ((destPort == null) ? 0 : destPort.hashCode());
      result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
      result = prime * result + ((vlanId == null) ? 0 : vlanId.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      FilterTupleBO other = (FilterTupleBO) obj;
      if (sourceIp == null) {
        if (other.sourceIp != null) return false;
      } else if (!sourceIp.equals(other.sourceIp)) return false;
      if (sourcePort == null) {
        if (other.sourcePort != null) return false;
      } else if (!sourcePort.equals(other.sourcePort)) return false;
      if (destIp == null) {
        if (other.destIp != null) return false;
      } else if (!destIp.equals(other.destIp)) return false;
      if (destPort == null) {
        if (other.destPort != null) return false;
      } else if (!destPort.equals(other.destPort)) return false;
      if (protocol == null) {
        if (other.protocol != null) return false;
      } else if (!protocol.equals(other.protocol)) return false;
      if (vlanId == null) {
        if (other.vlanId != null) return false;
      } else if (!vlanId.equals(other.vlanId)) return false;
      return true;
    }

    @JsonIgnore
    public boolean isEmpty() {
      if (StringUtils.isBlank(this.sourceIp) && StringUtils.isBlank(this.sourcePort)
          && StringUtils.isBlank(this.destIp) && StringUtils.isBlank(this.destPort)
          && StringUtils.isBlank(this.protocol) && StringUtils.isBlank(this.vlanId)) {
        return true;
      } else {
        return false;
      }
    }

    public String getSourceIp() {
      return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
      this.sourceIp = sourceIp;
    }

    public String getSourcePort() {
      return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
      this.sourcePort = sourcePort;
    }

    public String getDestIp() {
      return destIp;
    }

    public void setDestIp(String destIp) {
      this.destIp = destIp;
    }

    public String getDestPort() {
      return destPort;
    }

    public void setDestPort(String destPort) {
      this.destPort = destPort;
    }

    public String getProtocol() {
      return protocol;
    }

    public void setProtocol(String protocol) {
      this.protocol = protocol;
    }

    public String getVlanId() {
      return vlanId;
    }

    public void setVlanId(String vlanId) {
      this.vlanId = vlanId;
    }
  }

}
