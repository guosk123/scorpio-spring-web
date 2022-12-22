package com.machloop.fpc.cms.center.metric.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author mazhiyuan
 * <p>
 * create at 2020年8月27日, fpc-manager
 */
public interface MetricService {

  List<Map<String, Object>> queryMetricNetifHistograms(MetricQueryVO queryVO, String netifName,
      boolean extendedBound);

  List<Map<String, Object>> queryMetricLocationRawdatas(MetricQueryVO queryVO);

  List<Map<String, Object>> queryMetricLocations(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  /**
   * 统计多条数据：先调用queryMetricLocations获取根据排序字段得到的数据，取出前{count}条记录，作为条件去统计得出topN地区的时间曲线
   * 统计单条数据：根据{location}作为条件统计时间曲线
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @param countryId
   * @param provinceId
   * @param cityId
   * @return
   */
  List<Map<String, Object>> queryMetricLocationHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String countryId, String provinceId,
      String cityId);

  void exportLocations(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricApplicationRawdatas(MetricQueryVO queryVO);

  List<Map<String, Object>> queryMetricApplications(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, int type);

  /**
   * 统计多条数据：先调用queryMetricApplications获取根据排序字段得到的数据，取出前{count}条记录，作为条件去统计得出topN的时间曲线(分类/子分类/应用)
   * 统计单条数据：根据{applicationId}作为条件统计应用的时间曲线
   *
   * @param queryVO
   * @param type
   * @param sortProperty
   * @param sortDirection
   * @param id
   * @param isDetail
   * @return
   */
  List<Map<String, Object>> queryMetricApplicationHistograms(MetricQueryVO queryVO, int type,
      String sortProperty, String sortDirection, String id, boolean isDetail);

  /**
   * 统计所有应用(根据指定的字段进行排序)
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @return
   */
  List<Map<String, Object>> countMetricApplications(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  void exportApplications(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      int type, String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricL7ProtocolRawdatas(MetricQueryVO queryVO);

  List<Map<String, Object>> queryMetricL7Protocols(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  /**
   * 统计多条数据：先调用queryMetricL7Protocols获取根据排序字段得到的数据，取出前{count}条记录，作为条件去统计得出topN协议的时间曲线
   * 统计单条数据：根据{l7Protocol}作为条件统计时间曲线
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @param l7ProtocolId
   * @return
   */
  List<Map<String, Object>> queryMetricL7ProtocolHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String l7ProtocolId);

  /**
   * 统计协议流量排行（根据指定的字段进行排序）
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @return
   */
  List<Map<String, Object>> countMetricL7Protocols(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  void exportL7Protocols(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricPortRawdatas(MetricQueryVO queryVO);

  List<Map<String, Object>> queryMetricPorts(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  /**
   * 统计多条数据：先调用queryMetricPorts获取根据排序字段得到的数据，取出前{count}条记录，作为条件去统计得出topN的端口曲线
   * 统计单条数据：根据{port}作为条件统计时间曲线
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @param port
   * @return
   */
  List<Map<String, Object>> queryMetricPortHistograms(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, String port);

  void exportPorts(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricHostGroupRawdatas(MetricQueryVO queryVO);

  List<Map<String, Object>> queryMetricHostGroups(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  /**
   * 统计多条数据：先调用queryMetricHostGroups获取根据排序字段得到的数据，取出前{count}条记录，作为条件去统计得出topN的地址组曲线
   * 统计单条数据：根据{hostgroupId}作为条件统计时间曲线
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @param hostgroupId
   * @return
   */
  List<Map<String, Object>> queryMetricHostGroupHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String hostgroupId);

  void exportHostGroups(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricL2DeviceRawdatas(MetricQueryVO queryVO);

  List<Map<String, Object>> queryMetricL2Devices(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  /**
   * 统计多条数据：先调用queryMetricL2Devices获取根据排序字段得到的数据，取出前{count}条记录，作为条件去统计得出topN的二层主机曲线
   * 统计单条数据：根据{macAddress}作为条件统计时间曲线
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @param macAddress
   * @return
   */
  List<Map<String, Object>> queryMetricL2DeviceHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String macAddress);

  void exportL2Devices(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricL3DeviceRawdatas(MetricQueryVO queryVO);

  List<Map<String, Object>> queryMetricL3Devices(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricL3DevicesEstablishedFail(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);

  /**
   * 统计多条数据：先调用queryMetricL3Devices获取根据排序字段得到的数据，取出前{count}条记录，作为条件去统计得出topN的三层主机曲线
   * 统计单条数据：根据{ipAddress}作为条件统计时间曲线
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @param ipAddress
   * @param ipLocality
   * @return
   */
  List<Map<String, Object>> queryMetricL3DeviceHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String ipAddress, String ipLocality);


  /**
   * 统计三层主机流量排行(根据指定的字段进行排序)
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @return
   */
  List<Map<String, Object>> countMetricL3Devices(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, String compareProperty, int count);

  void exportL3Devices(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;

  /**
   * IP访问关系统计图
   *
   * @param queryVO
   * @param minEstablishedSessions
   * @param minTotalBytes
   * @param sortProperty
   * @param sortDirection
   * @return
   */
  List<Map<String, Object>> graphMetricIpConversations(MetricQueryVO queryVO,
      Integer minEstablishedSessions, Integer minTotalBytes, String sortProperty,
      String sortDirection);

  void exportGraphMetricIpConversations(ServletOutputStream outputStream, MetricQueryVO queryVO,
      String fileType, String sortProperty, String sortDirection, Integer minEstablishedSessions,
      Integer minTotalBytes) throws IOException;
  
  List<Map<String, Object>> queryMetricIpConversationRawdatas(MetricQueryVO queryVO);

  List<Map<String, Object>> queryMetricIpConversations(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  /**
   * 统计多条数据：先调用queryMetricIpConversations获取根据排序字段得到的数据，取出前{count}条记录，作为条件去统计得出topN的IP对曲线
   * 统计单条数据：根据{id}作为条件统计时间曲线
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @param ipAAddress
   * @param ipBAddress
   * @return
   */
  List<Map<String, Object>> queryMetricIpConversationHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String ipAAddress, String ipBAddress);

  /**
   * 统计三层通讯对流量排行(根据指定的字段进行排序)
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @return
   */
  List<Map<String, Object>> countMetricIpConversations(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  void exportIpConversations(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException;

  List<Map<String, Object>> queryMetricDhcpRawdatas(MetricQueryVO queryVO);

  List<Map<String, Object>> queryMetricDhcps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, String type);

  /**
   * 统计多条数据：先调用queryMetricDhcps获取根据排序字段得到的数据，取出前{count}条记录，作为条件去统计得出topN的DHCP曲线
   * 统计单条数据：根据{id}作为条件统计时间曲线
   * type为服务端时: id > serverIpAddress_serverMacAddress
   * type为客户端时: id > clientIpAddress_clientMacAddress
   * type为消息类型时：id > messageType
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @param type          统计类型
   * @param id
   * @return
   */
  List<Map<String, Object>> queryMetricDhcpHistograms(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, String type, String id);

  List<Map<String, Object>> queryMetricDscps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  /**
   * 先调用queryMetricDscps获取根据排序字段得到的数据，取出前{count}条记录，作为条件去统计得出topN的DSCP曲线
   *
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @return
   */
  List<Map<String, Object>> queryMetricDscpHistograms(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  Map<String, Object> queryMetricHttps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

}
