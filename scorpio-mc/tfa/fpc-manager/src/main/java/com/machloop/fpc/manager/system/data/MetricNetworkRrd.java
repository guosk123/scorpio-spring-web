package com.machloop.fpc.manager.system.data;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.machloop.fpc.common.FpcConstants;

/**
 * @author liumeng
 *
 * create at 2018年12月18日, fpc-manager
 */
public class MetricNetworkRrd {

  private MetricNetworkTraffic bytesRx;
  private MetricNetworkTraffic packetsRx;
  private MetricNetworkTraffic bytesTx;
  private MetricNetworkTraffic packetsTx;

  public MetricNetworkRrd(Date metricTime, long bytesRx, long packetsRx, long bytesTx,
      long packetsTx) {
    super();
    this.bytesRx = new MetricNetworkTraffic(bytesRx, metricTime, 0);
    this.packetsRx = new MetricNetworkTraffic(packetsRx, metricTime, 0);
    this.bytesTx = new MetricNetworkTraffic(bytesTx, metricTime, 0);
    this.packetsTx = new MetricNetworkTraffic(packetsTx, metricTime, 0);
  }

  public MetricNetworkRrd(MetricNetworkTraffic bytesRx, MetricNetworkTraffic packetsRx,
      MetricNetworkTraffic bytesTx, MetricNetworkTraffic packetsTx) {
    super();
    this.bytesRx = bytesRx;
    this.packetsRx = packetsRx;
    this.bytesTx = bytesTx;
    this.packetsTx = packetsTx;
  }

  public Long getValue(MetricNetworkRrd previous, String rrdName, int deltaSecond) {

    long value = 0L;

    if (StringUtils.endsWith(rrdName, FpcConstants.STAT_NETIF_RRD_RX_BYTEPS)) {
      if (previous.getBytesRx().getValue() < bytesRx.getValue()) {
        value = (bytesRx.getValue() - previous.bytesRx.getValue()) / deltaSecond;
      }
    } else if (StringUtils.endsWith(rrdName, FpcConstants.STAT_NETIF_RRD_RX_PPS)) {
      if (previous.getPacketsRx().getValue() < packetsRx.getValue()) {
        value = (packetsRx.getValue() - previous.packetsRx.getValue()) / deltaSecond;
      }
    } else if (StringUtils.endsWith(rrdName, FpcConstants.STAT_NETIF_RRD_TX_BYTEPS)) {
      if (previous.getBytesTx().getValue() < bytesTx.getValue()) {
        value = (bytesTx.getValue() - previous.bytesTx.getValue()) / deltaSecond;
      }
    } else if (StringUtils.endsWith(rrdName, FpcConstants.STAT_NETIF_RRD_TX_PPS)) {
      if (previous.getPacketsTx().getValue() < packetsTx.getValue()) {
        value = (packetsTx.getValue() - previous.packetsTx.getValue()) / deltaSecond;
      }
    } else {
      value = 0;
    }
    return value;
  }

  public Date getLastTime(String rrdName) {
    Date lastTime = null;

    MetricNetworkTraffic element = getElement(rrdName);
    if (element != null) {
      lastTime = element.getLastTime();
    }

    return lastTime;
  }

  public int getLastPosition(String rrdName) {
    int lastPosition = 0;

    MetricNetworkTraffic element = getElement(rrdName);
    if (element != null) {
      lastPosition = element.getLastPosition();
    }
    return lastPosition;
  }

  public void setLastPosition(String rrdName, int lastPosition) {
    MetricNetworkTraffic element = getElement(rrdName);
    if (element != null) {
      element.setLastPosition(lastPosition);
    }
  }

  public MetricNetworkTraffic getElement(String rrdName) {
    MetricNetworkTraffic element = null;
    if (StringUtils.endsWith(rrdName, FpcConstants.STAT_NETIF_RRD_RX_BYTEPS)) {
      element = bytesRx;
    } else if (StringUtils.endsWith(rrdName, FpcConstants.STAT_NETIF_RRD_RX_PPS)) {
      element = packetsRx;
    } else if (StringUtils.endsWith(rrdName, FpcConstants.STAT_NETIF_RRD_TX_BYTEPS)) {
      element = bytesTx;
    } else if (StringUtils.endsWith(rrdName, FpcConstants.STAT_NETIF_RRD_TX_PPS)) {
      element = packetsTx;
    }
    return element;
  }

  public MetricNetworkTraffic getBytesRx() {
    return bytesRx;
  }

  public void setBytesRx(MetricNetworkTraffic bytesRx) {
    this.bytesRx = bytesRx;
  }

  public MetricNetworkTraffic getPacketsRx() {
    return packetsRx;
  }

  public void setPacketsRx(MetricNetworkTraffic packetsRx) {
    this.packetsRx = packetsRx;
  }

  public MetricNetworkTraffic getBytesTx() {
    return bytesTx;
  }

  public void setBytesTx(MetricNetworkTraffic bytesTx) {
    this.bytesTx = bytesTx;
  }

  public MetricNetworkTraffic getPacketsTx() {
    return packetsTx;
  }

  public void setPacketsTx(MetricNetworkTraffic packetsTx) {
    this.packetsTx = packetsTx;
  }


}
