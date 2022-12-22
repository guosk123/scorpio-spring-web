package com.machloop.fpc.cms.center.analysis.vo;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/11/16 5:33 PM,cms
 * @version 1.0
 */
public class TiThreatBookQueryVO {

  private String basicTag;

  private String tag;

  private String iocType;

  @Override
  public String toString() {
    return "TiThreatBookQueryVO{" + "basicTag='" + basicTag + '\'' + ", tag='" + tag + '\''
        + ", iocType='" + iocType + '\'' + '}';
  }

  public String getBasicTag() {
    return basicTag;
  }

  public void setBasicTag(String basicTag) {
    this.basicTag = basicTag;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getIocType() {
    return iocType;
  }

  public void setIocType(String iocType) {
    this.iocType = iocType;
  }
}
