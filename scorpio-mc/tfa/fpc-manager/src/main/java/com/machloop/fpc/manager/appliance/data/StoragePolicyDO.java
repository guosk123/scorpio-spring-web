package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

public class StoragePolicyDO extends BaseOperateDO {
  private String compressAction;
  private String encryptAction;
  private String encryptAlgorithm;

  @Override
  public String toString() {
    return "StoragePolicyDO [compressAction=" + compressAction + ", encryptAction=" + encryptAction
        + ", encryptAlgorithm=" + encryptAlgorithm + "]";
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
