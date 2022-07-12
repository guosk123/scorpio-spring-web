package com.scorpio.metric.system.data;

public class MonitorSysTime {

  private String timeZone;
  private String dateTime;
  private boolean ntpEnabled;
  private String ntpServer;


  @Override
  public String toString() {
    return "MonitorSysTime [timeZone=" + timeZone + ", dateTime=" + dateTime + ", ntpEnabled="
        + ntpEnabled + ", ntpServer=" + ntpServer + "]";
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public String getDateTime() {
    return dateTime;
  }

  public void setDateTime(String dateTime) {
    this.dateTime = dateTime;
  }

  public boolean isNtpEnabled() {
    return ntpEnabled;
  }


  public void setNtpEnabled(boolean ntpEnabled) {
    this.ntpEnabled = ntpEnabled;
  }


  public String getNtpServer() {
    return ntpServer;
  }

  public void setNtpServer(String ntpServer) {
    this.ntpServer = ntpServer;
  }


}
