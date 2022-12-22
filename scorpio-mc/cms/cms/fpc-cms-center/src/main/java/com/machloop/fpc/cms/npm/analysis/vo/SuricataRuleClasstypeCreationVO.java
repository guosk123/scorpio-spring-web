package com.machloop.fpc.cms.npm.analysis.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/10/13 11:47 AM,cms
 * @version 1.0
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
