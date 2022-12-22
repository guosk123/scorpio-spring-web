package com.machloop.fpc.cms.center.appliance.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FilterTupleBO {

  private Object ip;
  private Object sourceIp;
  private Object destIp;
  private Object port;
  private Object sourcePort;
  private Object destPort;
  private Object ipProtocol;
  private Object l7ProtocolId;
  private Object vlanId;
  private Object applicationId;
  private Object countryId;
  private Object sourceCountryId;
  private Object destCountryId;
  private Object provinceId;
  private Object sourceProvinceId;
  private Object destProvinceId;
  private Object cityId;
  private Object sourceCityId;
  private Object destCityId;
  private Object macAddress;
  private Object sourceMacAddress;
  private Object destMacAddress;

  @Override public String toString() {
    return "FilterTupleBO{" + "ip=" + ip + ", sourceIp=" + sourceIp + ", destIp=" + destIp
        + ", port=" + port + ", sourcePort=" + sourcePort + ", destPort=" + destPort
        + ", ipProtocol=" + ipProtocol + ", l7ProtocolId=" + l7ProtocolId + ", vlanId=" + vlanId
        + ", applicationId=" + applicationId + ", countryId=" + countryId + ", sourceCountryId="
        + sourceCountryId + ", destCountryId=" + destCountryId + ", provinceId=" + provinceId
        + ", sourceProvinceId=" + sourceProvinceId + ", destProvinceId=" + destProvinceId
        + ", cityId=" + cityId + ", sourceCityId=" + sourceCityId + ", destCityId=" + destCityId
        + ", macAddress=" + macAddress + ", sourceMacAddress=" + sourceMacAddress
        + ", destMacAddress=" + destMacAddress + '}';
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
    result = prime * result + ((cityId == null) ? 0 : cityId.hashCode());
    result = prime * result + ((countryId == null) ? 0 : countryId.hashCode());
    result = prime * result + ((destCityId == null) ? 0 : destCityId.hashCode());
    result = prime * result + ((destCountryId == null) ? 0 : destCountryId.hashCode());
    result = prime * result + ((destIp == null) ? 0 : destIp.hashCode());
    result = prime * result + ((destPort == null) ? 0 : destPort.hashCode());
    result = prime * result + ((destProvinceId == null) ? 0 : destProvinceId.hashCode());
    result = prime * result + ((ip == null) ? 0 : ip.hashCode());
    result = prime * result + ((ipProtocol == null) ? 0 : ipProtocol.hashCode());
    result = prime * result + ((l7ProtocolId == null) ? 0 : l7ProtocolId.hashCode());
    result = prime * result + ((port == null) ? 0 : port.hashCode());
    result = prime * result + ((provinceId == null) ? 0 : provinceId.hashCode());
    result = prime * result + ((sourceCityId == null) ? 0 : sourceCityId.hashCode());
    result = prime * result + ((sourceCountryId == null) ? 0 : sourceCountryId.hashCode());
    result = prime * result + ((sourceIp == null) ? 0 : sourceIp.hashCode());
    result = prime * result + ((sourcePort == null) ? 0 : sourcePort.hashCode());
    result = prime * result + ((sourceProvinceId == null) ? 0 : sourceProvinceId.hashCode());
    result = prime * result + ((vlanId == null) ? 0 : vlanId.hashCode());
    result = prime * result + ((macAddress == null) ? 0 : macAddress.hashCode());
    result = prime * result + ((sourceMacAddress == null) ? 0 : sourceMacAddress.hashCode());
    result = prime * result + ((destMacAddress == null) ? 0 : destMacAddress.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    FilterTupleBO other = (FilterTupleBO) obj;
    if (applicationId == null) {
      if (other.applicationId != null) return false;
    } else if (!applicationId.equals(other.applicationId)) return false;
    if (cityId == null) {
      if (other.cityId != null) return false;
    } else if (!cityId.equals(other.cityId)) return false;
    if (countryId == null) {
      if (other.countryId != null) return false;
    } else if (!countryId.equals(other.countryId)) return false;
    if (destCityId == null) {
      if (other.destCityId != null) return false;
    } else if (!destCityId.equals(other.destCityId)) return false;
    if (destCountryId == null) {
      if (other.destCountryId != null) return false;
    } else if (!destCountryId.equals(other.destCountryId)) return false;
    if (destIp == null) {
      if (other.destIp != null) return false;
    } else if (!destIp.equals(other.destIp)) return false;
    if (destPort == null) {
      if (other.destPort != null) return false;
    } else if (!destPort.equals(other.destPort)) return false;
    if (destProvinceId == null) {
      if (other.destProvinceId != null) return false;
    } else if (!destProvinceId.equals(other.destProvinceId)) return false;
    if (ip == null) {
      if (other.ip != null) return false;
    } else if (!ip.equals(other.ip)) return false;
    if (ipProtocol == null) {
      if (other.ipProtocol != null) return false;
    } else if (!ipProtocol.equals(other.ipProtocol)) return false;
    if (l7ProtocolId == null) {
      if (other.l7ProtocolId != null) return false;
    } else if (!l7ProtocolId.equals(other.l7ProtocolId)) return false;
    if (port == null) {
      if (other.port != null) return false;
    } else if (!port.equals(other.port)) return false;
    if (provinceId == null) {
      if (other.provinceId != null) return false;
    } else if (!provinceId.equals(other.provinceId)) return false;
    if (sourceCityId == null) {
      if (other.sourceCityId != null) return false;
    } else if (!sourceCityId.equals(other.sourceCityId)) return false;
    if (sourceCountryId == null) {
      if (other.sourceCountryId != null) return false;
    } else if (!sourceCountryId.equals(other.sourceCountryId)) return false;
    if (sourceIp == null) {
      if (other.sourceIp != null) return false;
    } else if (!sourceIp.equals(other.sourceIp)) return false;
    if (sourcePort == null) {
      if (other.sourcePort != null) return false;
    } else if (!sourcePort.equals(other.sourcePort)) return false;
    if (sourceProvinceId == null) {
      if (other.sourceProvinceId != null) return false;
    } else if (!sourceProvinceId.equals(other.sourceProvinceId)) return false;
    if (vlanId == null) {
      if (other.vlanId != null) return false;
    } else if (!vlanId.equals(other.vlanId)) return false;
    if (macAddress == null) {
      if (other.macAddress != null) return false;
    } else if (!macAddress.equals(other.macAddress)) return false;
    if (sourceMacAddress == null) {
      if (other.sourceMacAddress != null) return false;
    } else if (!sourceMacAddress.equals(other.sourceMacAddress)) return false;
    if (destMacAddress == null) {
      if (other.destMacAddress != null) return false;
    } else if (!destMacAddress.equals(other.destMacAddress)) return false;
    return true;
  }

  @JsonIgnore
  public boolean isEmpty() {
    if (this.sourceIp == null && this.sourcePort == null && this.destIp == null
            && this.destPort == null && this.ipProtocol == null && this.l7ProtocolId == null
            && this.vlanId == null && this.ip == null && this.port == null && this.applicationId == null
            && this.countryId == null && this.sourceCountryId == null && this.destCountryId == null
            && this.provinceId == null && this.sourceProvinceId == null && this.destProvinceId == null
            && this.cityId == null && this.sourceCityId == null && this.destCityId == null
            && this.macAddress == null &&sourceMacAddress ==null && destMacAddress ==null) {
      return true;
    } else {
      return false;
    }
  }

  public Object getSourceMacAddress() {
    return sourceMacAddress;
  }

  public void setSourceMacAddress(Object sourceMacAddress) {
    this.sourceMacAddress = sourceMacAddress;
  }

  public Object getDestMacAddress() {
    return destMacAddress;
  }

  public void setDestMacAddress(Object destMacAddress) {
    this.destMacAddress = destMacAddress;
  }

  public Object getIp() {
    return ip;
  }

  public void setIp(Object ip) {
    this.ip = ip;
  }

  public Object getSourceIp() {
    return sourceIp;
  }

  public void setSourceIp(Object sourceIp) {
    this.sourceIp = sourceIp;
  }

  public Object getDestIp() {
    return destIp;
  }

  public void setDestIp(Object destIp) {
    this.destIp = destIp;
  }

  public Object getPort() {
    return port;
  }

  public void setPort(Object port) {
    this.port = port;
  }

  public Object getSourcePort() {
    return sourcePort;
  }

  public void setSourcePort(Object sourcePort) {
    this.sourcePort = sourcePort;
  }

  public Object getDestPort() {
    return destPort;
  }

  public void setDestPort(Object destPort) {
    this.destPort = destPort;
  }

  public Object getIpProtocol() {
    return ipProtocol;
  }

  public void setIpProtocol(Object ipProtocol) {
    this.ipProtocol = ipProtocol;
  }

  public Object getL7ProtocolId() {
    return l7ProtocolId;
  }

  public void setL7ProtocolId(Object l7ProtocolId) {
    this.l7ProtocolId = l7ProtocolId;
  }

  public Object getVlanId() {
    return vlanId;
  }

  public void setVlanId(Object vlanId) {
    this.vlanId = vlanId;
  }

  public Object getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(Object applicationId) {
    this.applicationId = applicationId;
  }

  public Object getCountryId() {
    return countryId;
  }

  public void setCountryId(Object countryId) {
    this.countryId = countryId;
  }

  public Object getSourceCountryId() {
    return sourceCountryId;
  }

  public void setSourceCountryId(Object sourceCountryId) {
    this.sourceCountryId = sourceCountryId;
  }

  public Object getDestCountryId() {
    return destCountryId;
  }

  public void setDestCountryId(Object destCountryId) {
    this.destCountryId = destCountryId;
  }

  public Object getProvinceId() {
    return provinceId;
  }

  public void setProvinceId(Object provinceId) {
    this.provinceId = provinceId;
  }

  public Object getSourceProvinceId() {
    return sourceProvinceId;
  }

  public void setSourceProvinceId(Object sourceProvinceId) {
    this.sourceProvinceId = sourceProvinceId;
  }

  public Object getDestProvinceId() {
    return destProvinceId;
  }

  public void setDestProvinceId(Object destProvinceId) {
    this.destProvinceId = destProvinceId;
  }

  public Object getCityId() {
    return cityId;
  }

  public void setCityId(Object cityId) {
    this.cityId = cityId;
  }

  public Object getSourceCityId() {
    return sourceCityId;
  }

  public void setSourceCityId(Object sourceCityId) {
    this.sourceCityId = sourceCityId;
  }

  public Object getDestCityId() {
    return destCityId;
  }

  public void setDestCityId(Object destCityId) {
    this.destCityId = destCityId;
  }

  public Object getMacAddress() {
    return macAddress;
  }

  public void setMacAddress(Object macAddress) {
    this.macAddress = macAddress;
  }

}
