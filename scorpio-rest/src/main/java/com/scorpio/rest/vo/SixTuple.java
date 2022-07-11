package com.scorpio.rest.vo;

public class SixTuple {

  private String sourceIp;
  private Integer sourcePort;
  private String destIp;
  private Integer destPort;
  private String protocol;
  private Integer vlanId;
  
  

  public SixTuple() {
    super();
    // TODO Auto-generated constructor stub
  }

  public SixTuple(String sourceIp, Integer sourcePort, String destIp, Integer destPort,
      String protocol, Integer vlanId) {
    super();
    this.sourceIp = sourceIp;
    this.sourcePort = sourcePort;
    this.destIp = destIp;
    this.destPort = destPort;
    this.protocol = protocol;
    this.vlanId = vlanId;
  }

  @Override
  public String toString() {
    return "SixTuple [sourceIp=" + sourceIp + ", sourcePort=" + sourcePort + ", destIp=" + destIp
        + ", destPort=" + destPort + ", protocol=" + protocol + ", vlanId=" + vlanId + "]";
  }
  
  

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((destIp == null) ? 0 : destIp.hashCode());
    result = prime * result + destPort;
    result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
    result = prime * result + ((sourceIp == null) ? 0 : sourceIp.hashCode());
    result = prime * result + sourcePort;
    result = prime * result + vlanId;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SixTuple other = (SixTuple) obj;
    if (destIp == null) {
      if (other.destIp != null) return false;
    } else if (!destIp.equals(other.destIp)) return false;
    if (!destPort.equals(other.destPort)) return false;
    if (protocol == null) {
      if (other.protocol != null) return false;
    } else if (!protocol.equals(other.protocol)) return false;
    if (sourceIp == null) {
      if (other.sourceIp != null) return false;
    } else if (!sourceIp.equals(other.sourceIp)) return false;
    if (!sourcePort.equals(other.sourcePort)) return false;
    if (!vlanId.equals(other.vlanId)) return false;
    return true;
  }

  public boolean isEmpty() {
    if(this.sourceIp == null && this.sourcePort == null && this.destIp == null &&
        this.destPort == null && this.protocol == null && this.vlanId == null) {
      return true;
    }else {
      return false;
    }
  }


  public String getSourceIp() {
    return sourceIp;
  }

  public void setSourceIp(String sourceIp) {
    this.sourceIp = sourceIp;
  }

  public Integer getSourcePort() {
    return sourcePort;
  }

  public void setSourcePort(Integer sourcePort) {
    this.sourcePort = sourcePort;
  }

  public String getDestIp() {
    return destIp;
  }

  public void setDestIp(String destIp) {
    this.destIp = destIp;
  }

  public Integer getDestPort() {
    return destPort;
  }

  public void setDestPort(Integer destPort) {
    this.destPort = destPort;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public Integer getVlanId() {
    return vlanId;
  }

  public void setVlanId(Integer vlanId) {
    this.vlanId = vlanId;
  }

}
