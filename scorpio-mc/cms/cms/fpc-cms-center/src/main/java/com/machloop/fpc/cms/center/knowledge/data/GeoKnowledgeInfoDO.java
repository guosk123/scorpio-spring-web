package com.machloop.fpc.cms.center.knowledge.data;

import java.util.Date;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月18日, fpc-cms-center
 */
public class GeoKnowledgeInfoDO {

  private String version;
  private Date releaseDate;
  private Date importDate;

  @Override
  public String toString() {
    return "GeoKnowledgeInfoDO [version=" + version + ", releaseDate=" + releaseDate
        + ", importDate=" + importDate + "]";
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Date getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(Date releaseDate) {
    this.releaseDate = releaseDate;
  }

  public Date getImportDate() {
    return importDate;
  }

  public void setImportDate(Date importDate) {
    this.importDate = importDate;
  }

}

