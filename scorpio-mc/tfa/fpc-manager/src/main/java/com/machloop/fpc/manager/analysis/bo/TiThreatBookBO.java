package com.machloop.fpc.manager.analysis.bo;

/**
 * @author ChenXiao
 * create at 2022/9/6
 */
public class TiThreatBookBO {

  private String id;

  private String iocRaw;

  private String basicTag;

  private String tag;

  private String intelType;

  private String source;

  private String time;

  private String iocType;


  @Override
  public String toString() {
    return "TiThreatBookBO{" + "id='" + id + '\'' + ", iocRaw='" + iocRaw + '\'' + ", basicTag='"
        + basicTag + '\'' + ", tag='" + tag + '\'' + ", intelType='" + intelType + '\''
        + ", source='" + source + '\'' + ", time='" + time + '\'' + ", iocType='" + iocType + '\''
        + '}';
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIocRaw() {
    return iocRaw;
  }

  public void setIocRaw(String iocRaw) {
    this.iocRaw = iocRaw;
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

  public String getIntelType() {
    return intelType;
  }

  public void setIntelType(String intelType) {
    this.intelType = intelType;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getIocType() {
    return iocType;
  }

  public void setIocType(String iocType) {
    this.iocType = iocType;
  }
}
