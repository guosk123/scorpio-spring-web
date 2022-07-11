package com.scorpio.rest.vo;

import java.io.File;
import java.io.Serializable;

public class FileVO implements Serializable{

	private static final long serialVersionUID = -8109117239471899537L;

	private String create_time;
	private String zone;
	private File object;

  public String getCreate_time() {
    return create_time;
  }
  public void setCreate_time(String create_time) {
    this.create_time = create_time;
  }
  public String getZone() {
    return zone;
  }
  public void setZone(String zone) {
    this.zone = zone;
  }
  public File getObject() {
    return object;
  }
  public void setObject(File object) {
    this.object = object;
  }

  @Override
  public String toString() {
    return "FileVO [create_time=" + create_time + ", zone=" + zone + ", object=" + object + "]";
  }

}
