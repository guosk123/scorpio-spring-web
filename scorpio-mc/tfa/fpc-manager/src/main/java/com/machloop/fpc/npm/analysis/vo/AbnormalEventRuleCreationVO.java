package com.machloop.fpc.npm.analysis.vo;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Range;

/**
 * @author guosk
 *
 * create at 2021年7月15日, fpc-manager
 */
public class AbnormalEventRuleCreationVO {

  @Min(value = 1, message = "异常事件类型不合法")
  private int type;
  @NotEmpty(message = "异常事件内容不能为空")
  private String content;
  @Digits(integer = 1, fraction = 0, message = "启用选项格式不正确")
  @Range(min = 0, max = 1, message = "启用选项格式不正确")
  @NotEmpty(message = "启用状态不能为空")
  private String status;

  @Override
  public String toString() {
    return "AbnormalEventRuleCreationVO [type=" + type + ", content=" + content + ", status="
        + status + "]";
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

}
