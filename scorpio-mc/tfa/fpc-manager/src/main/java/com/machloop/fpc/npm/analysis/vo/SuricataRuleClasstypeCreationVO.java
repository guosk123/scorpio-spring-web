package com.machloop.fpc.npm.analysis.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author guosk
 *
 * create at 2022年4月2日, fpc-manager
 */
public class SuricataRuleClasstypeCreationVO {

  @NotEmpty(message = "分类名称不能为空")
  private String name;

  @Override
  public String toString() {
    return "SuricataRuleClasstypeCreationVO [name=" + name + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
