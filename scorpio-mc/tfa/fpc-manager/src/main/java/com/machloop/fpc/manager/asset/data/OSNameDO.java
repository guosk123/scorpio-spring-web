package com.machloop.fpc.manager.asset.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年10月27日, fpc-manager
 */
public class OSNameDO extends BaseOperateDO {

  private String id;
  private String os;

  @Override
  public String toString() {
    return "OsNameDO [id=" + id + ", os=" + os + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getOs() {
    return os;
  }

  public void setOs(String os) {
    this.os = os;
  }

}
