package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Range;

public class StoragePolicyModificationVO {

  @Digits(integer = 1, fraction = 0, message = "压缩策略不合法")
  @Range(min = 0, max = 1, message = "压缩策略不合法")
  @NotEmpty(message = "压缩策略不能为空")
  private String compressAction;
  @Digits(integer = 1, fraction = 0, message = "加密策略不合法")
  @Range(min = 0, max = 1, message = "加密策略不合法")
  @NotEmpty(message = "加密策略不能为空")
  private String encryptAction;
  private String encryptAlgorithm;

  @Override
  public String toString() {
    return "StoragePolicyModificationVO [compressAction=" + compressAction + ", encryptAction="
        + encryptAction + ", encryptAlgorithm=" + encryptAlgorithm + "]";
  }

  public String getCompressAction() {
    return compressAction;
  }

  public void setCompressAction(String compressAction) {
    this.compressAction = compressAction;
  }

  public String getEncryptAction() {
    return encryptAction;
  }

  public void setEncryptAction(String encryptAction) {
    this.encryptAction = encryptAction;
  }

  public String getEncryptAlgorithm() {
    return encryptAlgorithm;
  }

  public void setEncryptAlgorithm(String encryptAlgorithm) {
    this.encryptAlgorithm = encryptAlgorithm;
  }

}
