package com.machloop.fpc.cms.center.appliance.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/12/9 5:12 PM,cms
 * @version 1.0
 */
public class DomainWhiteListVO {

  @Length(min = 1, max = 30, message = "名称不能为空，最多可输入30个字符")
  private String name;
  @NotEmpty(message = "域名白名单不能为空")
  private String domain;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "DomainWhiteListVO{" + "name='" + name + '\'' + ", domain='" + domain + '\''
        + ", description='" + description + '\'' + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
