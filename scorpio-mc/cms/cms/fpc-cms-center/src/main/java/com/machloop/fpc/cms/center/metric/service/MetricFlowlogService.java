package com.machloop.fpc.cms.center.metric.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2021年6月1日, fpc-manager
 */
public interface MetricFlowlogService {

  List<Map<String, Object>> queryMetricNetworks(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricNetworkHistograms(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricLocations(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricLocationHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String countryId, String provinceId,
      String cityId);

  void exportLocations(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricApplications(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, int type);

  List<Map<String, Object>> queryMetricApplicationHistograms(MetricQueryVO queryVO, int type,
      String sortProperty, String sortDirection, String id);

  void exportApplications(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      int type, String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricL7Protocols(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricL7ProtocolHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String l7ProtocolId);

  void exportL7Protocols(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String l7ProtocolId, String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricPorts(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  void exportPorts(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricPortHistograms(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, String port);

  List<Map<String, Object>> queryMetricHostGroups(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricHostGroupHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String hostgroupId);

  void exportHostGroups(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricL2Devices(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricL2DeviceHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String macAddress);

  void exportL2Devices(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricL3Devices(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricL3DeviceHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String ipAddress, String ipLocality);

  void exportL3Devices(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricIpConversations(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricIpConversationHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String ipAAddress, String ipBAddress);

  List<Map<String, Object>> graphMetricIpConversations(MetricQueryVO queryVO,
      Integer minEstablishedSessions, Integer minTotalBytes, String sortProperty,
      String sortDirection);

  void exportIpConversations(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;
}
