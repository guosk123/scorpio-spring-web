package com.machloop.fpc.manager.restapi.vo;

import java.util.List;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * @author guosk
 *
 * create at 2021年9月9日, fpc-manager
 */
public class SaCustomApplicationVO {

  @NotEmpty(message = "名称不能为空")
  private String name;
  @Digits(integer = Integer.MAX_VALUE, fraction = 0, message = "类型格式不正确")
  @NotEmpty(message = "类型不能为空")
  private String categoryId;
  @Digits(integer = Integer.MAX_VALUE, fraction = 0, message = "子类型格式不正确")
  @NotEmpty(message = "子类型不能为空")
  private String subCategoryId;
  @Digits(integer = Integer.MAX_VALUE, fraction = 0, message = "应用层协议不正确")
  @NotEmpty(message = "应用层协议不能为空")
  private String l7ProtocolId;
  private List<AppRule> rule;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "SaCustomApplicationVO [name=" + name + ", categoryId=" + categoryId + ", subCategoryId="
        + subCategoryId + ", l7ProtocolId=" + l7ProtocolId + ", rule=" + rule + ", description="
        + description + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getSubCategoryId() {
    return subCategoryId;
  }

  public void setSubCategoryId(String subCategoryId) {
    this.subCategoryId = subCategoryId;
  }

  public String getL7ProtocolId() {
    return l7ProtocolId;
  }

  public void setL7ProtocolId(String l7ProtocolId) {
    this.l7ProtocolId = l7ProtocolId;
  }

  public List<AppRule> getRule() {
    return rule;
  }

  public void setRule(List<AppRule> rule) {
    this.rule = rule;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public static class AppRule {

    private String name;
    private String ipAddress;
    private String protocol;
    private String port;
    private String signatureType;
    private String signatureOffset;
    private String signatureContent;

    @Override
    public String toString() {
      return "AppRule [name=" + name + ", ipAddress=" + ipAddress + ", protocol=" + protocol
          + ", port=" + port + ", signatureType=" + signatureType + ", signatureOffset="
          + signatureOffset + ", signatureContent=" + signatureContent + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((ipAddress == null) ? 0 : ipAddress.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((port == null) ? 0 : port.hashCode());
      result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
      result = prime * result + ((signatureContent == null) ? 0 : signatureContent.hashCode());
      result = prime * result + ((signatureOffset == null) ? 0 : signatureOffset.hashCode());
      result = prime * result + ((signatureType == null) ? 0 : signatureType.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      AppRule other = (AppRule) obj;
      if (ipAddress == null) {
        if (other.ipAddress != null) return false;
      } else if (!ipAddress.equals(other.ipAddress)) return false;
      if (name == null) {
        if (other.name != null) return false;
      } else if (!name.equals(other.name)) return false;
      if (port == null) {
        if (other.port != null) return false;
      } else if (!port.equals(other.port)) return false;
      if (protocol == null) {
        if (other.protocol != null) return false;
      } else if (!protocol.equals(other.protocol)) return false;
      if (signatureContent == null) {
        if (other.signatureContent != null) return false;
      } else if (!signatureContent.equals(other.signatureContent)) return false;
      if (signatureOffset == null) {
        if (other.signatureOffset != null) return false;
      } else if (!signatureOffset.equals(other.signatureOffset)) return false;
      if (signatureType == null) {
        if (other.signatureType != null) return false;
      } else if (!signatureType.equals(other.signatureType)) return false;
      return true;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getIpAddress() {
      return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
      this.ipAddress = ipAddress;
    }

    public String getProtocol() {
      return protocol;
    }

    public void setProtocol(String protocol) {
      this.protocol = protocol;
    }

    public String getPort() {
      return port;
    }

    public void setPort(String port) {
      this.port = port;
    }

    public String getSignatureType() {
      return signatureType;
    }

    public void setSignatureType(String signatureType) {
      this.signatureType = signatureType;
    }

    public String getSignatureOffset() {
      return signatureOffset;
    }

    public void setSignatureOffset(String signatureOffset) {
      this.signatureOffset = signatureOffset;
    }

    public String getSignatureContent() {
      return signatureContent;
    }

    public void setSignatureContent(String signatureContent) {
      this.signatureContent = signatureContent;
    }

  }

}
