package com.machloop.fpc.cms.center.metric.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.ExportUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.bo.HostGroupBO;
import com.machloop.fpc.cms.center.appliance.service.HostGroupService;
import com.machloop.fpc.cms.center.global.data.AbstractDataRecordDO;
import com.machloop.fpc.cms.center.knowledge.bo.*;
import com.machloop.fpc.cms.center.knowledge.service.GeoService;
import com.machloop.fpc.cms.center.knowledge.service.SaProtocolService;
import com.machloop.fpc.cms.center.knowledge.service.SaService;
import com.machloop.fpc.cms.center.metric.dao.*;
import com.machloop.fpc.cms.center.metric.data.*;
import com.machloop.fpc.cms.center.metric.service.MetricFlowlogService;
import com.machloop.fpc.cms.center.metric.service.MetricService;
import com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author mazhiyuan
 * <p>
 * create at 2020年8月27日, fpc-manager
 */
@Service
public class MetricServiceImpl implements MetricService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricServiceImpl.class);

  @Autowired
  private GlobalSettingService globalSetting;
  @Autowired
  private MetricNetifDataRecordDao netifDao;
  @Autowired
  private MetricLocationDataRecordDao locationDao;
  @Autowired
  private MetricApplicationDataRecordDao applicationDao;
  @Autowired
  private MetricL7ProtocolDataRecordDao l7ProtocolDao;
  @Autowired
  private MetricPortDataRecordDao portDao;
  @Autowired
  private MetricHostgroupDataRecordDao hostgroupDao;
  @Autowired
  private MetricL2DeviceDataRecordDao l2DeviceDao;
  @Autowired
  private MetricL3DeviceDataRecordDao l3DeviceDao;
  @Autowired
  private MetricIpConversationDataRecordDao ippairsDao;
  @Autowired
  private MetricDscpDataRecordDao dscpDao;
  @Autowired
  private MetricDhcpDataRecordDao dhcpDao;
  @Autowired
  private MetricHttpAnalysisDataRecordDao httpAnalysisDao;
  @Autowired
  private MetricHttpRequestDataRecordDao httpRequestDao;
  @Autowired
  private MetricOsDataRecordDao metricOsDao;
  @Autowired
  private SaProtocolService saProtocolService;
  @Autowired
  private DictManager dictManager;
  @Autowired
  private SensorNetworkService networkService;
  @Autowired
  private SaService saService;
  @Autowired
  private GeoService geoService;
  @Autowired
  private HostGroupService hostGroupService;
  @Autowired
  private ServletContext servletContext;
  @Autowired
  private MetricFlowlogService metricFlowlogService;

  private static final String HTTP_ANALYSIS_TYPE_METHOD = "request_method";
  private static final String HTTP_ANALYSIS_TYPE_CODE = "status_code";

  private static final String CSV_TITLE = "`IP_A`,`IP_B`,`总流量`,`会话数`\n";

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
  public static final Map<String,
      String> graphFields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.put("interface", "接口编号");
    fields.put("networkId", "所属网络");
    fields.put("serviceId", "所属业务");
    fields.put("startTime", "开始时间");
    fields.put("reportTime", "记录时间");
    fields.put("duration", "持续时间(s)");
    fields.put("upstreamBytes", "上行字节数");
    fields.put("downstreamBytes", "下行字节数");
    fields.put("totalBytes", "总字节数");
    fields.put("upstreamPackets", "正向包数");
    fields.put("downstreamPackets", "反向包数");
    fields.put("totalPackets", "总数据包");
    fields.put("upstreamPayloadBytes", "上行负载字节数");
    fields.put("downstreamPayloadBytes", "下行负载字节数");
    fields.put("totalPayloadBytes", "总负载字节数");
    fields.put("upstreamPayloadPackets", "上行负载数据包数");
    fields.put("downstreamPayloadPackets", "下行负载数据包数");
    fields.put("totalPayloadPackets", "总负载包数");
    fields.put("tcpClientNetworkLatency", "客户端网络时延");
    fields.put("tcpServerNetworkLatency", "服务器网络时延");
    fields.put("tcpClientNetworkLatencyAvg", "客户端网络平均时延");
    fields.put("tcpServerNetworkLatencyAvg", "服务器网络平均时延");
    fields.put("serverResponseLatency", "服务器响应时延");
    fields.put("serverResponseLatencyAvg", "服务器响应平均时延");
    fields.put("tcpClientLossBytes", "TCP客户端丢包字节数");
    fields.put("tcpServerLossBytes", "TCP服务端丢包字节数");
    fields.put("tcpClientZeroWindowPackets", "客户端零窗口包数");
    fields.put("tcpServerZeroWindowPackets", "服务器零窗口包数");
    fields.put("tcpSessionState", "tcp会话状态");
    fields.put("tcpEstablishedSuccessCounts", "TCP建立成功数");
    fields.put("tcpEstablishedFailCounts", "TCP建立失败数");
    fields.put("establishedSessions", "新建会话数");
    fields.put("tcpSynPackets", "TCP同步数据包数");
    fields.put("tcpSynAckPackets", "TCP同步确认数据包数");
    fields.put("tcpSynRstPackets", "TCP同步重置数据包");
    fields.put("tcpClientRetransmissionPackets", "TCP客户端重传包数");
    fields.put("tcpClientPackets", "TCP客户端总包数");
    fields.put("tcpClientRetransmissionRate", "客户端重传率");
    fields.put("tcpServerRetransmissionPackets", "TCP服务端重传包数");
    fields.put("tcpServerRetransmissionRate", "服务端重传率");
    fields.put("tcpServerPackets", "TCP服务端总包数");
    fields.put("macAddress", "MAC地址");
    fields.put("ethernetType", "三层协议类型");
    fields.put("ethernetProtocol", "网络层协议");
    fields.put("hostgroupId", "地址组");
    fields.put("hostgroupIdInitiator", "源IP所属地址组");
    fields.put("hostgroupIdResponder", "目的IP所属地址组");
    fields.put("ipLocality", "IP所在位置");
    fields.put("vlanId", "VLANID");
    fields.put("port", "端口号");
    fields.put("ipProtocol", "传输层协议");
    fields.put("l7ProtocolId", "应用层协议");
    fields.put("categoryId", "应用分类");
    fields.put("subcategoryId", "应用子分类");
    fields.put("applicationId", "应用名称");
    fields.put("provinceId", "省份");
    fields.put("countryId", "国家");
    fields.put("ipAddress", "IP地址");
    fields.put("ipAAddress", "IP_A");
    fields.put("ipBAddress", "IP_B");
    fields.put("totalBytes", "总字节数");

    graphFields.put("ipAAddress", "IP_A");
    graphFields.put("ipBAddress", "IP_B");
    graphFields.put("totalBytes", "总流量");
    graphFields.put("establishedSessions", "会话数");
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricNetifHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, boolean)
   */
  @Override
  public List<Map<String, Object>> queryMetricNetifHistograms(MetricQueryVO queryVO,
      String netifName, boolean extendedBound) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    List<MetricNetifDataRecordDO> histogram = netifDao.queryMetricNetifHistograms(queryVO,
        netifName, extendedBound);
    for (MetricNetifDataRecordDO tmp : histogram) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      item.put("timestamp", tmp.getTimestamp());
      item.put("networkId", tmp.getNetworkId());
      item.put("netifName", tmp.getNetifName());

      item.put("totalBytes", tmp.getTotalBytes());
      item.put("totalPackets", tmp.getTotalPackets());
      item.put("downstreamBytes", tmp.getDownstreamBytes());
      item.put("downstreamPackets", tmp.getDownstreamPackets());
      item.put("upstreamBytes", tmp.getUpstreamBytes());
      item.put("upstreamPackets", tmp.getUpstreamPackets());
      item.put("transmitBytes", tmp.getTransmitBytes());
      item.put("transmitPackets", tmp.getTransmitPackets());
      result.add(item);
    }
    return result;
  }

  /**
   * location
   */
  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricLocationRawdatas(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricLocationRawdatas(MetricQueryVO queryVO) {
    return locationDao.queryMetricLocationRawdatas(queryVO);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricLocations(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricLocations(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    List<MetricLocationDataRecordDO> recordList = locationDao.queryMetricLocations(queryVO,
        sortProperty, sortDirection);

    Map<String, String> locations = geoService.queryAllLocationIdNameMapping();
    // 获取地区字典
    Tuple3<Map<String, String>, Map<String, String>,
        Map<String, String>> locationDict = queryGeoIpDict();

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (MetricLocationDataRecordDO tmp : recordList) {
      String countryId = tmp.getCountryId();
      if (!locations.containsKey(countryId)) {
        continue;
      }

      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      fillingKPI(tmp, item);
      item.put("countryId", countryId);
      String countryText = "";
      if (StringUtils.isNotEmpty(countryId)) {
        countryText = MapUtils.getString(locationDict.getT1(), countryId, "");
      }
      item.put("countryText", countryText);
      item.put("provinceId", tmp.getProvinceId());
      String provinceText = "";
      if (StringUtils.isNotEmpty(tmp.getProvinceId())) {
        provinceText = MapUtils.getString(locationDict.getT2(), tmp.getProvinceId(), "");
      }
      item.put("provinceText", provinceText);
      item.put("cityId", tmp.getCityId());
      String cityText = "";
      if (StringUtils.isNotEmpty(tmp.getCityId())) {
        cityText = MapUtils.getString(locationDict.getT3(), tmp.getCityId(), "");
      }
      item.put("cityText", cityText);


      item.put("bytepsPeak", tmp.getBytepsPeak());
      item.put("downstreamBytes", tmp.getDownstreamBytes());
      item.put("downstreamPackets", tmp.getDownstreamPackets());
      item.put("upstreamBytes", tmp.getUpstreamBytes());
      item.put("upstreamPackets", tmp.getUpstreamPackets());
      item.put("totalPayloadBytes", tmp.getTotalPayloadBytes());
      item.put("totalPayloadPackets", tmp.getTotalPayloadPackets());
      item.put("downstreamPayloadBytes", tmp.getDownstreamPayloadBytes());
      item.put("downstreamPayloadPackets", tmp.getDownstreamPayloadPackets());
      item.put("upstreamPayloadBytes", tmp.getUpstreamPayloadBytes());
      item.put("upstreamPayloadPackets", tmp.getUpstreamPayloadPackets());
      item.put("tcpSynPackets", tmp.getTcpSynPackets());
      item.put("tcpSynAckPackets", tmp.getTcpSynAckPackets());
      item.put("tcpSynRstPackets", tmp.getTcpSynRstPackets());
      result.add(item);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricLocationHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricLocationHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String countryId, String provinceId,
      String cityId) {
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isAllBlank(countryId, provinceId, cityId)) {
      List<Map<String, Object>> metricList = queryMetricLocations(queryVO, sortProperty,
          sortDirection);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }

      metricList.forEach(metric -> {
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        map.put("country_id", metric.get("countryId"));
        map.put("province_id", metric.get("provinceId"));
        combinationConditions.add(map);
      });
    } else {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      map.put("country_id", StringUtils.defaultIfBlank(countryId, null));
      map.put("province_id", StringUtils.defaultIfBlank(provinceId, null));

      combinationConditions.add(map);
    }

    if (combinationConditions.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }
    // 获取地区字典
    Tuple3<Map<String, String>, Map<String, String>,
        Map<String, String>> locationDict = queryGeoIpDict();

    return locationDao.queryMetricLocationHistograms(queryVO, sortProperty, combinationConditions)
        .stream().map(map -> {
          String tempCountryId = MapUtils.getString(map, "countryId", "");
          String tempProvinceId = MapUtils.getString(map, "provinceId", "");
          String tempCityId = MapUtils.getString(map, "cityId", "");
          String countryText = "";
          String provinceText = "";
          String cityText = "";
          if (StringUtils.isNotEmpty(tempCountryId)) {
            countryText = MapUtils.getString(locationDict.getT1(), tempCountryId, "");
          }
          map.put("countryText", countryText);
          if (StringUtils.isNotEmpty(tempProvinceId)) {
            provinceText = MapUtils.getString(locationDict.getT2(), tempProvinceId, "");
          }
          map.put("provinceText", provinceText);
          if (StringUtils.isNotEmpty(tempCityId)) {
            cityText = MapUtils.getString(locationDict.getT3(), tempCityId, "");
          }
          map.put("cityText", cityText);
          return map;
        }).collect(Collectors.toList());
  }

  @Override
  public void exportLocations(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException {

    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    if (!StringUtils.equalsAnyIgnoreCase(fileType, Constants.EXPORT_FILE_TYPE_CSV,
        Constants.EXPORT_FILE_TYPE_EXCEL)) {
      LOGGER.warn("Unsupported export file style: {}", fileType);
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的导出样式");
    }

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    List<MetricLocationDataRecordDO> recordList = locationDao.queryMetricLocations(queryVO,
        sortProperty, sortDirection);

    Map<String, String> locations = geoService.queryAllLocationIdNameMapping();

    // 单次查询数量
    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {
        List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
        for (MetricLocationDataRecordDO tmp : recordList) {
          String countryId = tmp.getCountryId();
          if (!locations.containsKey(countryId)) {
            continue;
          }

          Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          fillingKPI(tmp, item);
          item.put("countryId", countryId);
          item.put("provinceId", tmp.getProvinceId());
          item.put("cityId", tmp.getCityId());

          item.put("bytepsPeak", tmp.getBytepsPeak());
          item.put("downstreamBytes", tmp.getDownstreamBytes());
          item.put("downstreamPackets", tmp.getDownstreamPackets());
          item.put("upstreamBytes", tmp.getUpstreamBytes());
          item.put("upstreamPackets", tmp.getUpstreamPackets());
          item.put("totalPayloadBytes", tmp.getTotalPayloadBytes());
          item.put("totalPayloadPackets", tmp.getTotalPayloadPackets());
          item.put("downstreamPayloadBytes", tmp.getDownstreamPayloadBytes());
          item.put("downstreamPayloadPackets", tmp.getDownstreamPayloadPackets());
          item.put("upstreamPayloadBytes", tmp.getUpstreamPayloadBytes());
          item.put("upstreamPayloadPackets", tmp.getUpstreamPayloadPackets());
          item.put("tcpSynPackets", tmp.getTcpSynPackets());
          item.put("tcpSynAckPackets", tmp.getTcpSynAckPackets());
          item.put("tcpSynRstPackets", tmp.getTcpSynRstPackets());
          result.add(item);
        }

        if (result.size() == 0) {
          offset = -1;
          return Lists.newArrayListWithCapacity(0);
        }

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();

        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /*
   * application
   */

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricApplicationRawdatas(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricApplicationRawdatas(MetricQueryVO queryVO) {
    return applicationDao.queryMetricApplicationRawdatas(queryVO);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricApplications(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> queryMetricApplications(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, int type) {
    List<MetricApplicationDataRecordDO> recordList = applicationDao.queryMetricApplications(queryVO,
        sortProperty, sortDirection, type);

    List<String> validIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();
    String key = "";
    switch (type) {
      case FpcCmsConstants.METRIC_TYPE_APPLICATION_CATEGORY:
        key = "categoryId";
        validIds.addAll(knowledgeRules.getT1().stream().map(SaCategoryBO::getCategoryId)
            .collect(Collectors.toList()));
        validIds.addAll(saService.queryCustomCategorys().stream()
            .map(SaCustomCategoryBO::getCategoryId).collect(Collectors.toList()));
        break;
      case FpcCmsConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY:
        key = "subcategoryId";
        validIds.addAll(knowledgeRules.getT2().stream().map(SaSubCategoryBO::getSubCategoryId)
            .collect(Collectors.toList()));
        validIds.addAll(saService.queryCustomSubCategorys().stream()
            .map(SaCustomSubCategoryBO::getSubCategoryId).collect(Collectors.toList()));
        break;
      case FpcCmsConstants.METRIC_TYPE_APPLICATION_APP:
        key = "applicationId";
        validIds.addAll(knowledgeRules.getT3().stream().map(SaApplicationBO::getApplicationId)
            .collect(Collectors.toList()));
        validIds.addAll(saService.queryCustomApps().stream()
            .map(SaCustomApplicationBO::getApplicationId).collect(Collectors.toList()));
        break;
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "类型不存在");
    }
    // sa应用 大类 小类 名称字典
    Map<String, Tuple3<String, String, String>> saAppDict = querySaApplicationDict();
    Map<String, Tuple3<String, String, String>> categoryDict = queryCategoryDict();
    Map<String, Tuple3<String, String, String>> subCateGoryDict = querysubCategoryDict();

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (MetricApplicationDataRecordDO tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      item.put("applicationId", String.valueOf(tmp.getApplicationId()));
      item.put("categoryId", String.valueOf(tmp.getCategoryId()));
      item.put("subcategoryId", String.valueOf(tmp.getSubcategoryId()));
      String applicationText = "";
      String categoryText = "";
      String subcategoryText = "";
      if (saAppDict.get(String.valueOf(tmp.getApplicationId())) != null) {
        applicationText = saAppDict.get(String.valueOf(tmp.getApplicationId())).getT1();
      }
      if (categoryDict.get(String.valueOf(tmp.getCategoryId())) != null) {
        categoryText = categoryDict.get(String.valueOf(tmp.getCategoryId())).getT2();
      }
      if (subCateGoryDict.get(String.valueOf(tmp.getSubcategoryId())) != null) {
        subcategoryText = subCateGoryDict.get(String.valueOf(tmp.getSubcategoryId())).getT3();
      }
      item.put("applicationText", applicationText);
      item.put("categoryText", categoryText);
      item.put("subcategoryText", subcategoryText);
      if (!validIds.contains(MapUtils.getString(item, key))) {
        continue;
      }

      fillingKPI(tmp, item);
      item.put("bytepsPeak", tmp.getBytepsPeak());
      item.put("downstreamBytes", tmp.getDownstreamBytes());
      item.put("downstreamPackets", tmp.getDownstreamPackets());
      item.put("upstreamBytes", tmp.getUpstreamBytes());
      item.put("upstreamPackets", tmp.getUpstreamPackets());
      item.put("totalPayloadBytes", tmp.getTotalPayloadBytes());
      item.put("totalPayloadPackets", tmp.getTotalPayloadPackets());
      item.put("downstreamPayloadBytes", tmp.getDownstreamPayloadBytes());
      item.put("downstreamPayloadPackets", tmp.getDownstreamPayloadPackets());
      item.put("upstreamPayloadBytes", tmp.getUpstreamPayloadBytes());
      item.put("upstreamPayloadPackets", tmp.getUpstreamPayloadPackets());
      item.put("tcpSynPackets", tmp.getTcpSynPackets());
      item.put("tcpSynAckPackets", tmp.getTcpSynAckPackets());
      item.put("tcpSynRstPackets", tmp.getTcpSynRstPackets());
      result.add(item);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricApplicationHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, int, java.lang.String, java.lang.String, int, java.lang.String, boolean)
   */
  @Override
  public List<Map<String, Object>> queryMetricApplicationHistograms(MetricQueryVO queryVO, int type,
      String sortProperty, String sortDirection, String id, boolean isDetail) {
    String termField = "application_id";
    switch (type) {
      case FpcCmsConstants.METRIC_TYPE_APPLICATION_CATEGORY:
        termField = "category_id";
        break;
      case FpcCmsConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY:
        termField = "subcategory_id";
        break;
      case FpcCmsConstants.METRIC_TYPE_APPLICATION_APP:
        termField = "application_id";
        break;
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "类型不存在");
    }

    List<String> ids = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(id)) {
      List<Map<String, Object>> metricList = queryMetricApplications(queryVO, sortProperty,
          sortDirection, type);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }
      final String fterm = termField;
      ids = metricList.stream()
          .map(metric -> (String) metric.get(TextUtils.underLineToCamel(fterm)))
          .collect(Collectors.toList());
    } else {
      ids.add(id);
    }

    if (ids.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }
    // sa应用 大类 小类 名称字典
    Map<String, Tuple3<String, String, String>> saAppDict = querySaApplicationDict();
    Map<String, Tuple3<String, String, String>> categoryDict = queryCategoryDict();
    Map<String, Tuple3<String, String, String>> subCateGoryDict = querysubCategoryDict();

    return applicationDao
        .queryMetricApplicationHistograms(queryVO, termField,
            isDetail ? CenterConstants.METRIC_NPM_ALL_AGGSFILED : sortProperty, ids)
        .stream().map(map -> {
          switch (type) {
            case FpcCmsConstants.METRIC_TYPE_APPLICATION_CATEGORY:
              String categoryId = MapUtils.getString(map, "categoryId", "");
              String categoryText = "";
              if (categoryDict.get(categoryId) != null) {
                categoryText = categoryDict.get(categoryId).getT2();
              }
              map.put("categoryText", categoryText);
              break;
            case FpcCmsConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY:
              String subcategoryId = MapUtils.getString(map, "subcategoryId", "");
              String subcategoryText = "";
              if (subCateGoryDict.get(subcategoryId) != null) {
                subcategoryText = subCateGoryDict.get(subcategoryId).getT3();
              }
              map.put("subcategoryText", subcategoryText);
              break;
            case FpcCmsConstants.METRIC_TYPE_APPLICATION_APP:
              String applicationId = MapUtils.getString(map, "applicationId", "");
              String applicationText = "";
              if (saAppDict.get(applicationId) != null) {
                applicationText = saAppDict.get(applicationId).getT1();
              }
              map.put("applicationText", applicationText);
              break;
          }
          return map;
        }).collect(Collectors.toList());

  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#countMetricApplications(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> countMetricApplications(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 如果查询条件不指定具体的网络或业务，则查询所有主网络的应用统计
    if (StringUtils.isAllBlank(queryVO.getNetworkGroupId(), queryVO.getNetworkId(),
        queryVO.getServiceId(), queryVO.getPacketFileId())) {
      queryVO.setNetworkIds(networkService.querySensorNetworks().stream()
          .map(SensorNetworkBO::getNetworkInSensorId).collect(Collectors.toList()));

      if (CollectionUtils.isEmpty(queryVO.getNetworkIds())) {
        return result;
      }
    }

    result = applicationDao.countMetricApplications(queryVO, sortProperty, sortProperty,
        sortDirection);

    if (result.size() > queryVO.getCount()) {
      result = result.subList(0, queryVO.getCount());
    }

    return result;
  }

  @Override
  public void exportApplications(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      int type, String fileType, OutputStream out) throws IOException {

    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {
        // 写入内容
        List<MetricApplicationDataRecordDO> recordList = applicationDao
            .queryMetricApplications(queryVO, sortProperty, sortDirection, type);
        List<String> validIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
            List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();
        String key = "";
        switch (type) {
          case FpcCmsConstants.METRIC_TYPE_APPLICATION_CATEGORY:
            key = "categoryId";
            validIds.addAll(knowledgeRules.getT1().stream().map(SaCategoryBO::getCategoryId)
                .collect(Collectors.toList()));
            validIds.addAll(saService.queryCustomCategorys().stream()
                .map(SaCustomCategoryBO::getCategoryId).collect(Collectors.toList()));
            break;
          case FpcCmsConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY:
            key = "subcategoryId";
            validIds.addAll(knowledgeRules.getT2().stream().map(SaSubCategoryBO::getSubCategoryId)
                .collect(Collectors.toList()));
            validIds.addAll(saService.queryCustomSubCategorys().stream()
                .map(SaCustomSubCategoryBO::getSubCategoryId).collect(Collectors.toList()));
            break;
          case FpcCmsConstants.METRIC_TYPE_APPLICATION_APP:
            key = "applicationId";
            validIds.addAll(knowledgeRules.getT3().stream().map(SaApplicationBO::getApplicationId)
                .collect(Collectors.toList()));
            validIds.addAll(saService.queryCustomApps().stream()
                .map(SaCustomApplicationBO::getApplicationId).collect(Collectors.toList()));
            break;
          default:
            throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "类型不存在");
        }
        List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
        for (MetricApplicationDataRecordDO tmp : recordList) {
          Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          item.put("applicationId", String.valueOf(tmp.getApplicationId()));
          item.put("categoryId", String.valueOf(tmp.getCategoryId()));
          item.put("subcategoryId", String.valueOf(tmp.getSubcategoryId()));
          if (!validIds.contains(MapUtils.getString(item, key))) {
            continue;
          }
          fillingKPI(tmp, item);
          item.put("bytepsPeak", tmp.getBytepsPeak());
          item.put("downstreamBytes", tmp.getDownstreamBytes());
          item.put("downstreamPackets", tmp.getDownstreamPackets());
          item.put("upstreamBytes", tmp.getUpstreamBytes());
          item.put("upstreamPackets", tmp.getUpstreamPackets());
          item.put("totalPayloadBytes", tmp.getTotalPayloadBytes());
          item.put("totalPayloadPackets", tmp.getTotalPayloadPackets());
          item.put("downstreamPayloadBytes", tmp.getDownstreamPayloadBytes());
          item.put("downstreamPayloadPackets", tmp.getDownstreamPayloadPackets());
          item.put("upstreamPayloadBytes", tmp.getUpstreamPayloadBytes());
          item.put("upstreamPayloadPackets", tmp.getUpstreamPayloadPackets());
          item.put("tcpSynPackets", tmp.getTcpSynPackets());
          item.put("tcpSynAckPackets", tmp.getTcpSynAckPackets());
          item.put("tcpSynRstPackets", tmp.getTcpSynRstPackets());
          result.add(item);
        }

        if (result.size() == 0) {
          offset = -1;
          result = Lists.newArrayListWithCapacity(0);
        }

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();
        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /**
   * l7protocol
   */
  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricL7ProtocolRawdatas(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricL7ProtocolRawdatas(MetricQueryVO queryVO) {
    return l7ProtocolDao.queryMetricL7ProtocolRawdatas(queryVO);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricL7Protocols(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricL7Protocols(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<MetricL7ProtocolDataRecordDO> recordList = l7ProtocolDao.queryMetricL7Protocols(queryVO,
        sortProperty, sortDirection);
    Map<String,
        String> protocolDict = saProtocolService.queryProtocols().stream()
            .collect(Collectors.toMap(protocolMap -> MapUtils.getString(protocolMap, "protocolId"),
                protocolMap -> MapUtils.getString(protocolMap, "nameText")));

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (MetricL7ProtocolDataRecordDO tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      fillingKPI(tmp, item);
      item.put("l7ProtocolId", tmp.getL7ProtocolId());
      String l7ProtocolText = "";
      if (StringUtils.isNotEmpty(MapUtils.getString(protocolDict, tmp.getL7ProtocolId(), ""))) {
        l7ProtocolText = MapUtils.getString(protocolDict, tmp.getL7ProtocolId(), "");
      }
      item.put("l7ProtocolText", l7ProtocolText);

      item.put("downstreamBytes", tmp.getDownstreamBytes());
      item.put("downstreamPackets", tmp.getDownstreamPackets());
      item.put("upstreamBytes", tmp.getUpstreamBytes());
      item.put("upstreamPackets", tmp.getUpstreamPackets());
      item.put("totalPayloadBytes", tmp.getTotalPayloadBytes());
      item.put("totalPayloadPackets", tmp.getTotalPayloadPackets());
      item.put("downstreamPayloadBytes", tmp.getDownstreamPayloadBytes());
      item.put("downstreamPayloadPackets", tmp.getDownstreamPayloadPackets());
      item.put("upstreamPayloadBytes", tmp.getUpstreamPayloadBytes());
      item.put("upstreamPayloadPackets", tmp.getUpstreamPayloadPackets());
      item.put("tcpSynPackets", tmp.getTcpSynPackets());
      item.put("tcpSynAckPackets", tmp.getTcpSynAckPackets());
      item.put("tcpSynRstPackets", tmp.getTcpSynRstPackets());
      result.add(item);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricL7ProtocolHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricL7ProtocolHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String l7ProtocolId) {
    List<String> l7ProtocolIds = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(l7ProtocolId)) {
      List<Map<String, Object>> metricList = queryMetricL7Protocols(queryVO, sortProperty,
          sortDirection);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }
      l7ProtocolIds = metricList.stream().map(metric -> (String) metric.get("l7ProtocolId"))
          .collect(Collectors.toList());
    } else {
      l7ProtocolIds.add(l7ProtocolId);
    }

    if (l7ProtocolIds.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }
    Map<String,
        String> protocolDict = saProtocolService.queryProtocols().stream()
            .collect(Collectors.toMap(protocolMap -> MapUtils.getString(protocolMap, "protocolId"),
                protocolMap -> MapUtils.getString(protocolMap, "nameText")));

    return l7ProtocolDao.queryMetricL7ProtocolHistograms(queryVO, sortProperty, l7ProtocolIds)
        .stream().map(map -> {
          String tempL7ProtocolId = MapUtils.getString(map, "l7ProtocolId", "");
          String l7ProtocolText = "";
          if (StringUtils.isNotEmpty(MapUtils.getString(protocolDict, tempL7ProtocolId, ""))) {
            l7ProtocolText = MapUtils.getString(protocolDict, tempL7ProtocolId, "");
          }
          map.put("l7ProtocolText", l7ProtocolText);

          return map;
        }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#countMetricL7Protocols(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> countMetricL7Protocols(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 如果查询条件不指定具体的网络或业务，则查询所有主网络的应用层协议统计
    if (StringUtils.isAllBlank(queryVO.getNetworkGroupId(), queryVO.getNetworkId(),
        queryVO.getServiceId(), queryVO.getPacketFileId())) {
      queryVO.setNetworkIds(networkService.querySensorNetworks().stream()
          .map(SensorNetworkBO::getNetworkInSensorId).collect(Collectors.toList()));

      if (CollectionUtils.isEmpty(queryVO.getNetworkIds())) {
        return result;
      }
    }

    result = l7ProtocolDao.countMetricL7Protocols(queryVO, sortProperty, sortProperty,
        sortDirection);

    if (result.size() > queryVO.getCount()) {
      result = result.subList(0, queryVO.getCount());
    }

    return result;
  }

  @Override
  public void exportL7Protocols(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException {

    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {

        List<MetricL7ProtocolDataRecordDO> recordList = l7ProtocolDao
            .queryMetricL7Protocols(queryVO, sortProperty, sortDirection);

        List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
        for (MetricL7ProtocolDataRecordDO tmp : recordList) {
          Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          fillingKPI(tmp, item);
          item.put("l7ProtocolId", tmp.getL7ProtocolId());

          item.put("downstreamBytes", tmp.getDownstreamBytes());
          item.put("downstreamPackets", tmp.getDownstreamPackets());
          item.put("upstreamBytes", tmp.getUpstreamBytes());
          item.put("upstreamPackets", tmp.getUpstreamPackets());
          item.put("totalPayloadBytes", tmp.getTotalPayloadBytes());
          item.put("totalPayloadPackets", tmp.getTotalPayloadPackets());
          item.put("downstreamPayloadBytes", tmp.getDownstreamPayloadBytes());
          item.put("downstreamPayloadPackets", tmp.getDownstreamPayloadPackets());
          item.put("upstreamPayloadBytes", tmp.getUpstreamPayloadBytes());
          item.put("upstreamPayloadPackets", tmp.getUpstreamPayloadPackets());
          item.put("tcpSynPackets", tmp.getTcpSynPackets());
          item.put("tcpSynAckPackets", tmp.getTcpSynAckPackets());
          item.put("tcpSynRstPackets", tmp.getTcpSynRstPackets());
          result.add(item);
        }

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();

        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /*
   * port
   */

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricPortRawdatas(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricPortRawdatas(MetricQueryVO queryVO) {
    return portDao.queryMetricPortRawdatas(queryVO);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricPorts(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricPorts(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    List<MetricPortDataRecordDO> recordList = portDao.queryMetricPorts(queryVO, sortProperty,
        sortDirection);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (MetricPortDataRecordDO tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      fillingKPI(tmp, item);
      item.put("port", String.valueOf(tmp.getPort()));
      item.put("ipProtocol", StringUtils.defaultIfBlank(tmp.getIpProtocol(), "ALL"));

      item.put("downstreamBytes", tmp.getDownstreamBytes());
      item.put("downstreamPackets", tmp.getDownstreamPackets());
      item.put("upstreamBytes", tmp.getUpstreamBytes());
      item.put("upstreamPackets", tmp.getUpstreamPackets());
      item.put("totalPayloadBytes", tmp.getTotalPayloadBytes());
      item.put("totalPayloadPackets", tmp.getTotalPayloadPackets());
      item.put("downstreamPayloadBytes", tmp.getDownstreamPayloadBytes());
      item.put("downstreamPayloadPackets", tmp.getDownstreamPayloadPackets());
      item.put("upstreamPayloadBytes", tmp.getUpstreamPayloadBytes());
      item.put("upstreamPayloadPackets", tmp.getUpstreamPayloadPackets());
      item.put("tcpSynPackets", tmp.getTcpSynPackets());
      item.put("tcpSynAckPackets", tmp.getTcpSynAckPackets());
      item.put("tcpSynRstPackets", tmp.getTcpSynRstPackets());
      result.add(item);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricPortHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricPortHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String port) {
    List<String> ports = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(port)) {
      List<Map<String, Object>> metricList = queryMetricPorts(queryVO, sortProperty, sortDirection);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }
      ports = metricList.stream().map(metric -> (String) metric.get("port"))
          .collect(Collectors.toList());
    } else {
      ports.add(port);
    }

    if (ports.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    return portDao.queryMetricPortHistograms(queryVO, sortProperty, ports);
  }

  @Override
  public void exportPorts(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException {

    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {

        List<MetricPortDataRecordDO> recordList = portDao.queryMetricPorts(queryVO, sortProperty,
            sortDirection);

        List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
        for (MetricPortDataRecordDO tmp : recordList) {
          Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          fillingKPI(tmp, item);
          item.put("port", String.valueOf(tmp.getPort()));
          item.put("ipProtocol", StringUtils.defaultIfBlank(tmp.getIpProtocol(), "ALL"));

          item.put("downstreamBytes", tmp.getDownstreamBytes());
          item.put("downstreamPackets", tmp.getDownstreamPackets());
          item.put("upstreamBytes", tmp.getUpstreamBytes());
          item.put("upstreamPackets", tmp.getUpstreamPackets());
          item.put("totalPayloadBytes", tmp.getTotalPayloadBytes());
          item.put("totalPayloadPackets", tmp.getTotalPayloadPackets());
          item.put("downstreamPayloadBytes", tmp.getDownstreamPayloadBytes());
          item.put("downstreamPayloadPackets", tmp.getDownstreamPayloadPackets());
          item.put("upstreamPayloadBytes", tmp.getUpstreamPayloadBytes());
          item.put("upstreamPayloadPackets", tmp.getUpstreamPayloadPackets());
          item.put("tcpSynPackets", tmp.getTcpSynPackets());
          item.put("tcpSynAckPackets", tmp.getTcpSynAckPackets());
          item.put("tcpSynRstPackets", tmp.getTcpSynRstPackets());
          result.add(item);
        }

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();

        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }


  /*
   * hostgroup
   */

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricHostGroupRawdatas(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricHostGroupRawdatas(MetricQueryVO queryVO) {
    return hostgroupDao.queryMetricHostGroupRawdatas(queryVO);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricHostGroups(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricHostGroups(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    List<MetricHostgroupDataRecordDO> recordList = hostgroupDao.queryMetricHostgroups(queryVO,
        sortProperty, sortDirection);

    List<String> hostGroupIds = hostGroupService.queryHostGroups().stream().map(HostGroupBO::getId)
        .collect(Collectors.toList());
    Map<String, String> hostGroupDict = hostGroupService.queryHostGroups().stream()
        .collect(Collectors.toMap(HostGroupBO::getId, HostGroupBO::getName));

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (MetricHostgroupDataRecordDO tmp : recordList) {
      String hostgroupId = tmp.getHostgroupId();
      if (!hostGroupIds.contains(hostgroupId)) {
        continue;
      }

      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      fillingKPI(tmp, item);
      item.put("hostgroupId", tmp.getHostgroupId());
      String hostgroupText = "";
      if (StringUtils.isNotEmpty(MapUtils.getString(hostGroupDict, hostgroupId, ""))) {
        hostgroupText = MapUtils.getString(hostGroupDict, hostgroupId, "");
      }
      item.put("hostgroupText", hostgroupText);

      item.put("downstreamBytes", tmp.getDownstreamBytes());
      item.put("downstreamPackets", tmp.getDownstreamPackets());
      item.put("upstreamBytes", tmp.getUpstreamBytes());
      item.put("upstreamPackets", tmp.getUpstreamPackets());
      result.add(item);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricHostGroupHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricHostGroupHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String hostgroupId) {
    List<String> hostgroupIds = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(hostgroupId)) {
      List<Map<String, Object>> metricList = queryMetricHostGroups(queryVO, sortProperty,
          sortDirection);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }
      hostgroupIds = metricList.stream().map(metric -> (String) metric.get("hostgroupId"))
          .collect(Collectors.toList());
    } else {
      hostgroupIds.add(hostgroupId);
    }

    if (hostgroupIds.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }
    Map<String, String> hostGroupDict = hostGroupService.queryHostGroups().stream()
        .collect(Collectors.toMap(HostGroupBO::getId, HostGroupBO::getName));

    return hostgroupDao.queryMetricHostgroupHistograms(queryVO, sortProperty, hostgroupIds).stream()
        .map(map -> {
          String tempHostgroupId = MapUtils.getString(map, "hostgroupId", "");
          String hostgroupText = "";
          if (StringUtils.isNotEmpty(MapUtils.getString(hostGroupDict, tempHostgroupId, ""))) {
            hostgroupText = MapUtils.getString(hostGroupDict, tempHostgroupId, "");
          }
          map.put("hostgroupText", hostgroupText);

          return map;
        }).collect(Collectors.toList());
  }

  @Override
  public void exportHostGroups(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException {

    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {

        List<MetricHostgroupDataRecordDO> recordList = hostgroupDao.queryMetricHostgroups(queryVO,
            sortProperty, sortDirection);

        List<String> hostGroupIds = hostGroupService.queryHostGroups().stream()
            .map(HostGroupBO::getId).collect(Collectors.toList());

        List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
        for (MetricHostgroupDataRecordDO tmp : recordList) {
          String hostgroupId = tmp.getHostgroupId();
          if (!hostGroupIds.contains(hostgroupId)) {
            continue;
          }

          Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          item.put("hostgroupId", hostgroupId);
          fillingKPI(tmp, item);

          item.put("downstreamBytes", tmp.getDownstreamBytes());
          item.put("downstreamPackets", tmp.getDownstreamPackets());
          item.put("upstreamBytes", tmp.getUpstreamBytes());
          item.put("upstreamPackets", tmp.getUpstreamPackets());
          result.add(item);
        }

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();

        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /*
   * l2 device
   */

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricL2DeviceRawdatas(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricL2DeviceRawdatas(MetricQueryVO queryVO) {
    return l2DeviceDao.queryMetricL2DeviceRawdatas(queryVO);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricL2Devices(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricL2Devices(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    List<MetricL2DeviceDataRecordDO> recordList = l2DeviceDao.queryMetricL2Devices(queryVO,
        sortProperty, sortDirection);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (MetricL2DeviceDataRecordDO tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      fillingKPI(tmp, item);
      item.put("macAddress", tmp.getMacAddress());
      item.put("ethernetType", StringUtils.defaultIfBlank(tmp.getEthernetType(), "ALL"));

      item.put("downstreamBytes", tmp.getDownstreamBytes());
      item.put("downstreamPackets", tmp.getDownstreamPackets());
      item.put("upstreamBytes", tmp.getUpstreamBytes());
      item.put("upstreamPackets", tmp.getUpstreamPackets());
      result.add(item);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricL2DeviceHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricL2DeviceHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String macAddress) {
    List<String> macAddressList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(macAddress)) {
      List<Map<String, Object>> metricList = queryMetricL2Devices(queryVO, sortProperty,
          sortDirection);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }
      macAddressList = metricList.stream().map(metric -> (String) metric.get("macAddress"))
          .collect(Collectors.toList());
    } else {
      macAddressList.add(macAddress);
    }

    if (macAddressList.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    return l2DeviceDao.queryMetricL2DeviceHistograms(queryVO, sortProperty, macAddressList);
  }

  @Override
  public void exportL2Devices(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException {

    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {

        List<MetricL2DeviceDataRecordDO> recordList = l2DeviceDao.queryMetricL2Devices(queryVO,
            sortProperty, sortDirection);

        List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
        for (MetricL2DeviceDataRecordDO tmp : recordList) {
          Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          fillingKPI(tmp, item);
          item.put("macAddress", tmp.getMacAddress());
          item.put("ethernetType", StringUtils.defaultIfBlank(tmp.getEthernetType(), "ALL"));

          item.put("downstreamBytes", tmp.getDownstreamBytes());
          item.put("downstreamPackets", tmp.getDownstreamPackets());
          item.put("upstreamBytes", tmp.getUpstreamBytes());
          item.put("upstreamPackets", tmp.getUpstreamPackets());
          result.add(item);
        }

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();

        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /**
   * l3 device
   */
  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricL3DeviceRawdatas(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricL3DeviceRawdatas(MetricQueryVO queryVO) {
    return l3DeviceDao.queryMetricL3DeviceRawdatas(queryVO);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricL3Devices(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricL3Devices(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    List<MetricL3DeviceDataRecordDO> recordList = l3DeviceDao.queryMetricL3Devices(queryVO,
        sortProperty, sortDirection);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (MetricL3DeviceDataRecordDO tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      fillingKPI(tmp, item);
      item.put("ipAddress", tmp.getIpAddress());
      item.put("ipLocality", tmp.getIpLocality());
      item.put("macAddress", StringUtils.defaultIfBlank(tmp.getMacAddress(), "ALL"));

      item.put("downstreamBytes", tmp.getDownstreamBytes());
      item.put("downstreamPackets", tmp.getDownstreamPackets());
      item.put("upstreamBytes", tmp.getUpstreamBytes());
      item.put("upstreamPackets", tmp.getUpstreamPackets());
      item.put("activeEstablishedSessions", tmp.getActiveEstablishedSessions());
      item.put("passiveEstablishedSessions", tmp.getPassiveEstablishedSessions());
      result.add(item);
    }

    return result;
  }

  @Override
  public List<Map<String, Object>> queryMetricL3DevicesEstablishedFail(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    return l3DeviceDao.queryMetricL3DevicesEstablishedFail(queryVO, sortProperty, sortDirection);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricL3DeviceHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricL3DeviceHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String ipAddress, String ipLocality) {
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isAllBlank(ipAddress, ipLocality)) {
      List<Map<String, Object>> metricList = queryMetricL3Devices(queryVO, sortProperty,
          sortDirection);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }
      metricList.forEach(metric -> {
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        map.put("ip_address", metric.get("ipAddress"));
        map.put("ip_locality", metric.get("ipLocality"));
        combinationConditions.add(map);
      });
    } else {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      map.put("ip_address", ipAddress);
      map.put("ip_locality", ipLocality);
      combinationConditions.add(map);
    }

    if (combinationConditions.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    return l3DeviceDao.queryMetricL3DeviceHistograms(queryVO, sortProperty, combinationConditions);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#countMetricL3Devices(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> countMetricL3Devices(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, String compareProperty, int count) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 如果查询条件不指定具体的网络或业务，则查询所有主网络的应用层协议统计
    if (StringUtils.isAllBlank(queryVO.getNetworkGroupId(), queryVO.getNetworkId(),
        queryVO.getServiceId(), queryVO.getPacketFileId())) {
      queryVO.setNetworkIds(networkService.querySensorNetworks().stream()
          .map(SensorNetworkBO::getNetworkInSensorId).collect(Collectors.toList()));
      if (CollectionUtils.isEmpty(queryVO.getNetworkIds())) {
        return result;
      }
    }

    if (StringUtils.isBlank(compareProperty)) {

      result = l3DeviceDao.countMetricL3Devices(queryVO, sortProperty, sortProperty, sortDirection);
    } else {
      if (queryVO.getServiceType().equals("totalService")) {
        if (compareProperty.equals("rate")) {
          sortProperty = sortProperty + "_rate";
        }
      } else if (queryVO.getServiceType().equals("intranetService")) {
        sortProperty = sortProperty + "_inside_service";
        if (compareProperty.equals("rate")) {
          sortProperty = sortProperty + "_rate";
        }
      } else {
        sortProperty = sortProperty + "_outside_service";
        if (compareProperty.equals("rate")) {
          sortProperty = sortProperty + "_rate";
        }
      }

      result = l3DeviceDao.countMetricL3Devices(queryVO, sortProperty, sortProperty, sortDirection)
          .stream()
          .map(map -> map.entrySet().stream()
              .filter(x -> (map.size() == 4
                  && (x.getKey().contains("Address") || !x.getKey().contains("Bytes")))
                  || (map.size() == 6 && StringUtils.containsAny(x.getKey(), "Address", "Rate")))
              .<TreeMap<String, Object>>collect(TreeMap::new,
                  (m, n) -> m.put(n.getKey(), n.getValue()), TreeMap::putAll))
          .collect(Collectors.toList());
    }
    if (result.size() > count) {
      result = result.subList(0, count);
    }

    return result;
  }

  @Override
  public void exportL3Devices(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException {

    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {

        List<MetricL3DeviceDataRecordDO> recordList = l3DeviceDao.queryMetricL3Devices(queryVO,
            sortProperty, sortDirection);

        List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
        for (MetricL3DeviceDataRecordDO tmp : recordList) {
          Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          fillingKPI(tmp, item);
          item.put("ipAddress", tmp.getIpAddress());
          item.put("ipLocality", tmp.getIpLocality());
          item.put("macAddress", StringUtils.defaultIfBlank(tmp.getMacAddress(), "ALL"));

          item.put("downstreamBytes", tmp.getDownstreamBytes());
          item.put("downstreamPackets", tmp.getDownstreamPackets());
          item.put("upstreamBytes", tmp.getUpstreamBytes());
          item.put("upstreamPackets", tmp.getUpstreamPackets());
          item.put("activeEstablishedSessions", tmp.getActiveEstablishedSessions());
          item.put("passiveEstablishedSessions", tmp.getPassiveEstablishedSessions());
          result.add(item);
        }

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();

        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  protected void sortMetricResult(List<Map<String, Object>> result, String sortProperty,
      String sortDirection) {
    result.sort(new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        Long o1Value = MapUtils.getLongValue(o1, TextUtils.underLineToCamel(sortProperty), 0);
        Long o2Value = MapUtils.getLongValue(o2, TextUtils.underLineToCamel(sortProperty), 0);

        return StringUtils.equalsIgnoreCase(sortDirection, Sort.Direction.ASC.name())
            ? o1Value.compareTo(o2Value)
            : o2Value.compareTo(o1Value);
      }
    });
  }

  /**
   * IpConversation
   */
  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricIpConversationRawdatas(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricIpConversationRawdatas(MetricQueryVO queryVO) {
    return ippairsDao.queryMetricIpConversationRawdatas(queryVO);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricIpConversations(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricIpConversations(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<MetricIpConversationDataRecordDO> recordList = ippairsDao
        .queryMetricIpConversations(queryVO, sortProperty, sortDirection);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (MetricIpConversationDataRecordDO tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      fillingKPI(tmp, item);
      item.put("ipAAddress", tmp.getIpAAddress());
      item.put("ipBAddress", tmp.getIpBAddress());

      item.put("downstreamBytes", tmp.getDownstreamBytes());
      item.put("downstreamPackets", tmp.getDownstreamPackets());
      item.put("upstreamBytes", tmp.getUpstreamBytes());
      item.put("upstreamPackets", tmp.getUpstreamPackets());
      item.put("activeEstablishedSessions", tmp.getActiveEstablishedSessions());
      item.put("passiveEstablishedSessions", tmp.getPassiveEstablishedSessions());
      result.add(item);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricIpConversationHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricIpConversationHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String ipAAddress, String ipBAddress) {
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isAllBlank(ipAAddress, ipBAddress)) {
      List<Map<String, Object>> metricList = queryMetricIpConversations(queryVO, sortProperty,
          sortDirection);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }

      metricList.forEach(metric -> {
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        map.put("ip_a_address", metric.get("ipAAddress"));
        map.put("ip_b_address", metric.get("ipBAddress"));
        combinationConditions.add(map);
      });
    } else {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      map.put("ip_a_address", ipAAddress);
      map.put("ip_b_address", ipBAddress);
      combinationConditions.add(map);
    }

    if (combinationConditions.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    return ippairsDao.queryMetricIpConversationHistograms(queryVO, sortProperty,
        combinationConditions);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#countMetricIpConversations(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> countMetricIpConversations(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 如果查询条件不指定具体的网络或业务，则查询所有主网络的应用层协议统计
    if (StringUtils.isAllBlank(queryVO.getNetworkGroupId(), queryVO.getNetworkId(),
        queryVO.getServiceId(), queryVO.getPacketFileId())) {
      queryVO.setNetworkIds(networkService.querySensorNetworks().stream()
          .map(SensorNetworkBO::getNetworkInSensorId).collect(Collectors.toList()));
      if (CollectionUtils.isEmpty(queryVO.getNetworkIds())) {
        return result;
      }
    }

    result = ippairsDao.countMetricIpConversations(queryVO, sortProperty, sortProperty,
        sortDirection);

    if (result.size() > queryVO.getCount()) {
      result = result.subList(0, queryVO.getCount());
    }

    return result;
  }

  @Override
  public void exportIpConversations(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, String fileType, OutputStream out) throws IOException {

    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {

        List<MetricIpConversationDataRecordDO> recordList = ippairsDao
            .queryMetricIpConversations(queryVO, sortProperty, sortDirection);

        List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
        for (MetricIpConversationDataRecordDO tmp : recordList) {
          Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          fillingKPI(tmp, item);
          item.put("ipAAddress", tmp.getIpAAddress());
          item.put("ipBAddress", tmp.getIpBAddress());

          item.put("downstreamBytes", tmp.getDownstreamBytes());
          item.put("downstreamPackets", tmp.getDownstreamPackets());
          item.put("upstreamBytes", tmp.getUpstreamBytes());
          item.put("upstreamPackets", tmp.getUpstreamPackets());
          item.put("activeEstablishedSessions", tmp.getActiveEstablishedSessions());
          item.put("passiveEstablishedSessions", tmp.getPassiveEstablishedSessions());
          result.add(item);
        }

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();

        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#graphMetricIpConversations(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> graphMetricIpConversations(MetricQueryVO queryVO,
      Integer minEstablishedSessions, Integer minTotalBytes, String sortProperty,
      String sortDirection) {

    return ippairsDao.graphMetricIpConversations(queryVO, minEstablishedSessions, minTotalBytes,
        sortProperty, sortDirection);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#exportGraphMetricIpConversations(javax.servlet.ServletOutputStream, com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer)
   */
  @Override
  public void exportGraphMetricIpConversations(ServletOutputStream outputStream,
      MetricQueryVO queryVO, String fileType, String sortProperty, String sortDirection,
      Integer minEstablishedSessions, Integer minTotalBytes) throws IOException {
    if (!StringUtils.equalsAnyIgnoreCase(fileType, Constants.EXPORT_FILE_TYPE_CSV,
        Constants.EXPORT_FILE_TYPE_EXCEL)) {
      LOGGER.warn("Unsupported export file style: {}", fileType);
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的导出样式");
    }

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.graphMetricIpConversations(queryVO, minEstablishedSessions,
          minTotalBytes, sortProperty, sortDirection);
    } else {
      result = graphMetricIpConversations(queryVO, minEstablishedSessions, minTotalBytes,
          sortProperty, sortDirection);
    }

    if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_EXCEL)) {
      exportAsExcel(result, queryVO, outputStream);
    } else {
      exportAsCsv(result, queryVO, outputStream);
    }
  }

  /**
   * DHCP
   */
  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricDhcpRawdatas(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricDhcpRawdatas(MetricQueryVO queryVO) {
    return dhcpDao.queryMetricDhcpRawdatas(queryVO);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricDhcps(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricDhcps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, String type) {
    List<MetricDhcpDataRecordDO> recordList = dhcpDao.queryMetricDhcps(queryVO, sortProperty,
        sortDirection, type);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (MetricDhcpDataRecordDO tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      item.put("dhcpVersion", tmp.getDhcpVersion());
      item.put("serverIpAddress", tmp.getServerIpAddress());
      item.put("serverMacAddress", tmp.getServerMacAddress());
      item.put("clientIpAddress", tmp.getClientIpAddress());
      item.put("clientMacAddress", tmp.getClientMacAddress());
      item.put("messageType", tmp.getMessageType());

      item.put("totalBytes", tmp.getTotalBytes());
      item.put("totalPackets", tmp.getTotalPackets());
      item.put("sendBytes", tmp.getSendBytes());
      item.put("sendPackets", tmp.getSendPackets());
      item.put("receiveBytes", tmp.getReceiveBytes());
      item.put("receivePackets", tmp.getReceivePackets());
      result.add(item);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricDhcpHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String, int, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricDhcpHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String type, String id) {
    List<Tuple2<String, Boolean>> termFields = Lists.newArrayList();
    switch (type) {
      case FpcCmsConstants.METRIC_TYPE_DHCP_SERVER:
        termFields = Lists.newArrayList(Tuples.of("server_ip_address", false),
            Tuples.of("server_mac_address", false));
        break;
      case FpcCmsConstants.METRIC_TYPE_DHCP_CLIENT:
        termFields = Lists.newArrayList(Tuples.of("client_ip_address", false),
            Tuples.of("client_mac_address", false));
        break;
      case FpcCmsConstants.METRIC_TYPE_DHCP_MESSAGE_TYPE:
        termFields.add(Tuples.of("message_type", false));
        break;
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "查询分类不存在");
    }
    final List<Tuple2<String, Boolean>> ftermFields = termFields;

    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(id)) {
      List<Map<String, Object>> metricList = queryMetricDhcps(queryVO, sortProperty, sortDirection,
          type);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }

      metricList.forEach(metric -> {
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        ftermFields.forEach(
            term -> map.put(term.getT1(), metric.get(TextUtils.underLineToCamel(term.getT1()))));
        combinationConditions.add(map);
      });
    } else {
      String[] values = id.split("_");
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      for (int i = 0; i < values.length; i++) {
        map.put(ftermFields.get(i).getT1(), values[i]);
      }
      combinationConditions.add(map);
    }

    if (combinationConditions.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    return dhcpDao.queryMetricDhcpHistograms(queryVO, termFields, sortProperty,
        combinationConditions);
  }

  /**
   * DSCP
   */
  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricDscps(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricDscps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    List<MetricDscpDataRecordDO> recordList = dscpDao.queryMetricDscps(queryVO, sortProperty,
        sortDirection);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (MetricDscpDataRecordDO tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

      item.put("type", tmp.getType());
      item.put("totalBytes", tmp.getTotalBytes());
      item.put("totalPackets", tmp.getTotalPackets());
      result.add(item);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricDscpHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> queryMetricDscpHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> metricList = queryMetricDscps(queryVO, sortProperty, sortDirection);
    if (metricList.size() > queryVO.getCount()) {
      metricList = metricList.subList(0, queryVO.getCount());
    }
    List<String> dscpTypeList = metricList.stream().map(metric -> (String) metric.get("type"))
        .collect(Collectors.toList());

    if (dscpTypeList.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    return dscpDao.queryMetricDscpHistograms(queryVO, sortProperty, dscpTypeList);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricService#queryMetricHttps(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public Map<String, Object> queryMetricHttps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // http请求
    result.put("httpRequest", httpRequestDao.queryHttpRequestHistograms(queryVO));

    // http方法分布
    result.put("httpMethod", httpAnalysisDao.queryHttpAnalysis(queryVO, HTTP_ANALYSIS_TYPE_METHOD,
        sortProperty, sortDirection));

    // http状态码分布
    result.put("httpCode", httpAnalysisDao.queryHttpAnalysis(queryVO, HTTP_ANALYSIS_TYPE_CODE,
        sortProperty, sortDirection));

    // 终端分布
    result.put("os", metricOsDao.queryOsMetric(queryVO, sortProperty, sortDirection));

    return result;
  }

  private void fillingKPI(AbstractDataRecordDO recordDO, Map<String, Object> item) {
    item.put("totalBytes", recordDO.getTotalBytes());
    item.put("totalPackets", recordDO.getTotalPackets());
    item.put("establishedSessions", recordDO.getEstablishedSessions());
    item.put("tcpEstablishedFailCounts", recordDO.getTcpEstablishedFailCounts());
    item.put("tcpEstablishedSuccessCounts", recordDO.getTcpEstablishedSuccessCounts());
    item.put("tcpClientNetworkLatency", recordDO.getTcpClientNetworkLatency());
    item.put("tcpClientNetworkLatencyAvg", recordDO.getTcpClientNetworkLatencyAvg());
    item.put("tcpServerNetworkLatency", recordDO.getTcpServerNetworkLatency());
    item.put("tcpServerNetworkLatencyAvg", recordDO.getTcpServerNetworkLatencyAvg());
    item.put("serverResponseLatency", recordDO.getServerResponseLatency());
    item.put("serverResponseLatencyAvg", recordDO.getServerResponseLatencyAvg());
    item.put("tcpClientRetransmissionPackets", recordDO.getTcpClientRetransmissionPackets());
    item.put("tcpClientPackets", recordDO.getTcpClientPackets());
    item.put("tcpClientRetransmissionRate", recordDO.getTcpClientRetransmissionRate());
    item.put("tcpServerRetransmissionPackets", recordDO.getTcpServerRetransmissionPackets());
    item.put("tcpServerPackets", recordDO.getTcpServerPackets());
    item.put("tcpServerRetransmissionRate", recordDO.getTcpServerRetransmissionRate());
    item.put("tcpClientZeroWindowPackets", recordDO.getTcpClientZeroWindowPackets());
    item.put("tcpServerZeroWindowPackets", recordDO.getTcpServerZeroWindowPackets());
  }

  /**
   * 当页面不提供查询时间间隔时使用
   * 将查询时间切分为不同精度的多个查询时间段
   *
   * @param startTimeDate
   * @param endTimeDate
   * @return (( 时间段开始时间, 时间段结束时间), 查询精度)
   */
  @SuppressWarnings("unused")
  private List<Tuple2<Tuple2<Date, Date>, Integer>> splitQueryDate(final Date startTimeDate,
      final Date endTimeDate) {

    // 将查询时间切分为不同精度的多个查询时间段
    // |----|----|----|----|----|
    // | 30s| 5m | 1h | 5m | 30s|

    List<Tuple2<Tuple2<Date, Date>, Integer>> dateRanges = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    if (DateUtils.isSameInstant(startTimeDate, endTimeDate)) {
      dateRanges
          .add(Tuples.of(Tuples.of(startTimeDate, endTimeDate), Constants.HALF_MINUTE_SECONDS));
      return dateRanges;
    }

    Date hourFrom = null, hourTo = null;
    Date total5minFrom = null, total5minTo = null;

    Date head5minFrom = null, head5minTo = null;
    Date tail5minFrom = null, tail5minTo = null;

    Date head30sFrom = null, head30sTo = null;
    Date tail30sFrom = null, tail30sTo = null;

    long totalMills = endTimeDate.getTime() - startTimeDate.getTime();

    if (totalMills >= Constants.ONE_HOUR_SECONDS * 1000) {
      // 只保留小时精度
      Date startTimeHourTruncate = DateUtils.truncate(startTimeDate, Calendar.HOUR);
      Date endTimeHourTruncate = DateUtils.truncate(endTimeDate, Calendar.HOUR);

      // 开始结束时间在同一小时，则hourFrom,hourTo为null
      if (!DateUtils.isSameInstant(startTimeHourTruncate, endTimeHourTruncate)) {
        // 小时精度的开始时间为，>=原始查询开始时间的最近的小时时间；小时精度的结束时间为，<=原始查询结束时间的最近的小时时间
        hourFrom = DateUtils.isSameInstant(startTimeHourTruncate, startTimeDate) ? startTimeDate
            : com.machloop.alpha.common.util.DateUtils.afterSecondDate(startTimeHourTruncate,
                (int) TimeUnit.HOURS.toSeconds(1));
        hourTo = endTimeHourTruncate;
      }
    }

    if (totalMills >= Constants.FIVE_MINUTE_SECONDS * 1000) {
      // 只保留分钟精度
      Date startTimeMinuteTruncate = DateUtils.truncate(startTimeDate, Calendar.MINUTE);
      Date endTimeMinuteTruncate = DateUtils.truncate(endTimeDate, Calendar.MINUTE);

      Date startTime5MinuteTruncate = DateUtils.setMinutes(startTimeMinuteTruncate,
          DateUtils.toCalendar(startTimeMinuteTruncate).get(Calendar.MINUTE) / 5 * 5);
      Date endTime5MinuteTruncate = DateUtils.setMinutes(endTimeMinuteTruncate,
          DateUtils.toCalendar(endTimeMinuteTruncate).get(Calendar.MINUTE) / 5 * 5);

      // 开始结束时间在同一5分钟，则total5minFrom,total5minTo为null
      if (!DateUtils.isSameInstant(startTime5MinuteTruncate, endTime5MinuteTruncate)) {
        // 分钟精度的开始时间为，>=原始查询开始时间的最近的整5分钟时间；分钟精度的结束时间为，<=原始查询结束时间的最近的整5分钟时间
        total5minFrom = DateUtils.toCalendar(startTimeMinuteTruncate).get(Calendar.MINUTE) % 5 == 0
            && DateUtils.isSameInstant(startTimeMinuteTruncate, startTimeDate)
                ? startTimeDate
                : DateUtils.addMinutes(startTimeMinuteTruncate,
                    (DateUtils.toCalendar(startTimeMinuteTruncate).get(Calendar.MINUTE) / 5 + 1) * 5
                        - DateUtils.toCalendar(startTimeMinuteTruncate).get(Calendar.MINUTE));
        total5minTo = DateUtils.toCalendar(endTimeMinuteTruncate).get(Calendar.MINUTE) % 5 == 0
            && DateUtils.isSameInstant(endTimeMinuteTruncate, endTimeDate) ? endTimeDate
                : DateUtils.setMinutes(endTimeMinuteTruncate,
                    DateUtils.toCalendar(endTimeMinuteTruncate).get(Calendar.MINUTE) / 5 * 5);
      }
    }

    if (total5minFrom != null && total5minTo != null) {
      head30sFrom = startTimeDate;
      head30sTo = total5minFrom;
      tail30sFrom = total5minTo;
      tail30sTo = endTimeDate;
    } else {
      // 总时间间隔不足5分钟
      head30sFrom = startTimeDate;
      head30sTo = endTimeDate;
    }

    if (hourFrom != null && hourTo != null) {
      head5minFrom = total5minFrom;
      head5minTo = hourFrom;
      tail5minFrom = hourTo;
      tail5minTo = total5minTo;
    } else {
      // 总时间间隔不足1小时
      head5minFrom = total5minFrom;
      head5minTo = total5minTo;
    }

    // 考虑rollup是否聚合到该时间点, 该时间点未完成rollup
    String lastes1hour = globalSetting
        .getValue(CenterConstants.GLOBAL_SETTING_DR_ROLLUP_LATEST_1HOUR);
    String lastes5min = globalSetting
        .getValue(CenterConstants.GLOBAL_SETTING_DR_ROLLUP_LATEST_5MIN);

    Date lastes1hDate = StringUtils.isNotBlank(lastes1hour)
        ? com.machloop.alpha.common.util.DateUtils.parseISO8601Date(lastes1hour)
        : null;
    Date lastes5minDate = StringUtils.isNotBlank(lastes5min)
        ? com.machloop.alpha.common.util.DateUtils.parseISO8601Date(lastes5min)
        : null;

    /*
     * 当拆分后各个间隔的开始和结束时间一致，则此时间段无需查询 当拆分后时间段内有未rollup的时间，则退化为查询30s的索引
     */
    if (hourFrom != null && hourTo != null && !DateUtils.isSameInstant(hourFrom, hourTo)) {
      if (lastes1hDate == null || lastes1hDate.before(hourTo)) {
        dateRanges.add(Tuples.of(Tuples.of(hourFrom, hourTo), Constants.HALF_MINUTE_SECONDS));
      } else {
        dateRanges.add(Tuples.of(Tuples.of(hourFrom, hourTo), Constants.ONE_HOUR_SECONDS));
      }
    }

    if (head5minFrom != null && head5minTo != null
        && !DateUtils.isSameInstant(head5minFrom, head5minTo)) {
      if (lastes5minDate == null || lastes5minDate.before(head5minTo)) {
        dateRanges
            .add(Tuples.of(Tuples.of(head5minFrom, head5minTo), Constants.HALF_MINUTE_SECONDS));
      } else {
        dateRanges
            .add(Tuples.of(Tuples.of(head5minFrom, head5minTo), Constants.FIVE_MINUTE_SECONDS));
      }

    }
    if (tail5minFrom != null && tail5minTo != null
        && !DateUtils.isSameInstant(tail5minFrom, tail5minTo)) {
      if (lastes5minDate == null || lastes5minDate.before(tail5minTo)) {
        dateRanges
            .add(Tuples.of(Tuples.of(tail5minFrom, tail5minTo), Constants.HALF_MINUTE_SECONDS));
      } else {
        dateRanges
            .add(Tuples.of(Tuples.of(tail5minFrom, tail5minTo), Constants.FIVE_MINUTE_SECONDS));
      }
    }

    if (head30sFrom != null && head30sTo != null
        && !DateUtils.isSameInstant(head30sFrom, head30sTo)) {
      dateRanges.add(Tuples.of(Tuples.of(head30sFrom, head30sTo), Constants.HALF_MINUTE_SECONDS));
    }
    if (tail30sFrom != null && tail30sTo != null
        && !DateUtils.isSameInstant(tail30sFrom, tail30sTo)) {
      dateRanges.add(Tuples.of(Tuples.of(tail30sFrom, tail30sTo), Constants.HALF_MINUTE_SECONDS));
    }


    long resultTotalMills = dateRanges.stream()
        .mapToLong(range -> range.getT1().getT2().getTime() - range.getT1().getT1().getTime())
        .sum();
    if (totalMills != resultTotalMills) {
      LOGGER.warn("split query date error.date ranges: {}", dateRanges);
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "split query date, rawStartTime: [{}], rawEndTime: [{}], "
              + "head30sFrom: [{}], head30sTo: [{}], head5minFrom: [{}], head5minTo: [{}], "
              + "hourFrom: [{}], hourTo: [{}], tail5minFrom: [{}], tail5minTo: [{}], "
              + "tail30sFrom: [{}], tail30sTo: [{}]",
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(startTimeDate),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(endTimeDate),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(head30sFrom),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(head30sTo),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(head5minFrom),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(head5minTo),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(hourFrom),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(hourTo),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(tail5minFrom),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(tail5minTo),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(tail30sFrom),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(tail30sTo));
    }
    return dateRanges;
  }

  private Map<String, Tuple3<String, String, String>> querySaApplicationDict() {
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();

    // 大类
    List<SaCustomCategoryBO> customCategorys = saService.queryCustomCategorys();
    Map<String, String> categoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT1().size() + customCategorys.size());
    knowledgeRules.getT1()
        .forEach(category -> categoryDict.put(category.getCategoryId(), category.getNameText()));
    customCategorys.forEach(
        category -> categoryDict.put(category.getCategoryId(), category.getName() + "（自定义）"));

    // 小类
    List<SaCustomSubCategoryBO> customSubCategorys = saService.queryCustomSubCategorys();
    Map<String, String> subCategoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT2().size() + customSubCategorys.size());
    knowledgeRules.getT2().forEach(subCategory -> subCategoryDict
        .put(subCategory.getSubCategoryId(), subCategory.getNameText()));
    customSubCategorys.forEach(subCategory -> subCategoryDict.put(subCategory.getSubCategoryId(),
        subCategory.getName() + "（自定义）"));

    // 应用
    List<SaCustomApplicationBO> customRules = saService.queryCustomApps();
    Map<String, Tuple3<String, String, String>> applicationDict = Maps
        .newHashMapWithExpectedSize(customRules.size() + knowledgeRules.getT3().size());
    knowledgeRules.getT3().forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getApplicationId(),
          Tuples.of(app.getNameText(), StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    customRules.forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getApplicationId(),
          Tuples.of(app.getName() + "（自定义）",
              StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    return applicationDict;
  }

  private Map<String, Tuple3<String, String, String>> queryCategoryDict() {
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();

    // 大类
    List<SaCustomCategoryBO> customCategorys = saService.queryCustomCategorys();
    Map<String, String> categoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT1().size() + customCategorys.size());
    knowledgeRules.getT1()
        .forEach(category -> categoryDict.put(category.getCategoryId(), category.getNameText()));
    customCategorys.forEach(
        category -> categoryDict.put(category.getCategoryId(), category.getName() + "（自定义）"));

    // 小类
    List<SaCustomSubCategoryBO> customSubCategorys = saService.queryCustomSubCategorys();
    Map<String, String> subCategoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT2().size() + customSubCategorys.size());
    knowledgeRules.getT2().forEach(subCategory -> subCategoryDict
        .put(subCategory.getSubCategoryId(), subCategory.getNameText()));
    customSubCategorys.forEach(subCategory -> subCategoryDict.put(subCategory.getSubCategoryId(),
        subCategory.getName() + "（自定义）"));

    // 应用
    List<SaCustomApplicationBO> customRules = saService.queryCustomApps();
    Map<String, Tuple3<String, String, String>> applicationDict = Maps
        .newHashMapWithExpectedSize(customRules.size() + knowledgeRules.getT3().size());
    knowledgeRules.getT3().forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getCategoryId(),
          Tuples.of(app.getNameText(), StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    customRules.forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getApplicationId(),
          Tuples.of(app.getName() + "（自定义）",
              StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    return applicationDict;
  }

  private Map<String, Tuple3<String, String, String>> querysubCategoryDict() {
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();

    // 大类
    List<SaCustomCategoryBO> customCategorys = saService.queryCustomCategorys();
    Map<String, String> categoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT1().size() + customCategorys.size());
    knowledgeRules.getT1()
        .forEach(category -> categoryDict.put(category.getCategoryId(), category.getNameText()));
    customCategorys.forEach(
        category -> categoryDict.put(category.getCategoryId(), category.getName() + "（自定义）"));

    // 小类
    List<SaCustomSubCategoryBO> customSubCategorys = saService.queryCustomSubCategorys();
    Map<String, String> subCategoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT2().size() + customSubCategorys.size());
    knowledgeRules.getT2().forEach(subCategory -> subCategoryDict
        .put(subCategory.getSubCategoryId(), subCategory.getNameText()));
    customSubCategorys.forEach(subCategory -> subCategoryDict.put(subCategory.getSubCategoryId(),
        subCategory.getName() + "（自定义）"));

    // 应用
    List<SaCustomApplicationBO> customRules = saService.queryCustomApps();
    Map<String, Tuple3<String, String, String>> applicationDict = Maps
        .newHashMapWithExpectedSize(customRules.size() + knowledgeRules.getT3().size());
    knowledgeRules.getT3().forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getSubCategoryId(),
          Tuples.of(app.getNameText(), StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    customRules.forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getApplicationId(),
          Tuples.of(app.getName() + "（自定义）",
              StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    return applicationDict;
  }

  private Tuple3<Map<String, String>, Map<String, String>, Map<String, String>> queryGeoIpDict() {
    Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>,
        List<GeoCityBO>> geolocations = geoService.queryGeolocations();

    Map<String, String> countryDict = geolocations.getT1().stream()
        .collect(Collectors.toMap(GeoCountryBO::getCountryId, GeoCountryBO::getNameText));
    countryDict.putAll(geoService.queryCustomCountrys().stream()
        .collect(Collectors.toMap(GeoCustomCountryBO::getCountryId, GeoCustomCountryBO::getName)));

    Map<String, String> provinceDict = geolocations.getT2().stream()
        .collect(Collectors.toMap(GeoProvinceBO::getProvinceId, GeoProvinceBO::getNameText));

    Map<String, String> cityDict = geolocations.getT3().stream()
        .collect(Collectors.toMap(GeoCityBO::getCityId, GeoCityBO::getNameText));

    return Tuples.of(countryDict, provinceDict, cityDict);
  }

  private List<String> metricLocationMapToStr(Map<String, Object> metricResult, List<String> titles,
      Map<String, String> columnNameMap) {

    // sa应用 大类 小类 名称字典
    Map<String, Tuple3<String, String, String>> saAppDict = querySaApplicationDict();
    Map<String, Tuple3<String, String, String>> categoryDict = queryCategoryDict();
    Map<String, Tuple3<String, String, String>> subCateGoryDict = querysubCategoryDict();
    // 地址组名称
    Map<String, String> hostGroupDict = hostGroupService.queryHostGroups().stream()
        .collect(Collectors.toMap(HostGroupBO::getId, HostGroupBO::getName));
    Map<String,
        String> protocolDict = saProtocolService.queryProtocols().stream()
            .collect(Collectors.toMap(protocolMap -> MapUtils.getString(protocolMap, "protocolId"),
                protocolMap -> MapUtils.getString(protocolMap, "nameText")));
    // 获取地区字典
    Tuple3<Map<String, String>, Map<String, String>,
        Map<String, String>> locationDict = queryGeoIpDict();
    // eth类型字典
    Map<String,
        String> ethernetTypeDict = dictManager.getBaseDict().getItemMap("flow_log_ethernet_type");
    // ip内外网位置字典
    Map<String, String> ipLocalityDict = dictManager.getBaseDict()
        .getItemMap("flow_log_ip_address_locality");

    Tuple3<String, String, String> app = null;
    if (saAppDict.get(MapUtils.getString(metricResult, "applicationId", "")) != null) {
      app = saAppDict.get(MapUtils.getString(metricResult, "applicationId", ""));
    } else if (categoryDict.get(MapUtils.getString(metricResult, "categoryId", "")) != null) {
      app = categoryDict.get(MapUtils.getString(metricResult, "categoryId", ""));
    } else {
      app = subCateGoryDict.get(MapUtils.getString(metricResult, "subcategoryId", ""));
    }

    String appName = app != null ? app.getT1() : "";
    String appCategoryName = app != null ? app.getT2() : "";
    String appSubCategoryName = app != null ? app.getT3() : "";

    List<String> values = titles.stream().map(title -> {
      String field = columnNameMap.get(title);

      String value = "";
      switch (field) {
        case "totalBytes":
          value = getPrintSize(MapUtils.getString(metricResult, field, ""));
          break;
        case "tcpClientNetworkLatency":
          value = getDelay(MapUtils.getString(metricResult, field, ""));
          break;
        case "tcpClientNetworkLatencyAvg":
          value = getDelay(MapUtils.getString(metricResult, field, ""));
          break;
        case "tcpServerNetworkLatency":
          value = getDelay(MapUtils.getString(metricResult, field, ""));
          break;
        case "tcpServerNetworkLatencyAvg":
          value = getDelay(MapUtils.getString(metricResult, field, ""));
          break;
        case "serverResponseLatency":
          value = getDelay(MapUtils.getString(metricResult, field, ""));
          break;
        case "serverResponseLatencyAvg":
          value = getDelay(MapUtils.getString(metricResult, field, ""));
          break;
        case "countryId":
          value = MapUtils.getString(locationDict.getT1(),
              MapUtils.getString(metricResult, field, ""), "");
          break;
        case "provinceId":
          value = MapUtils.getString(locationDict.getT2(),
              MapUtils.getString(metricResult, field, ""), "");
          break;
        case "cityId":
          value = MapUtils.getString(locationDict.getT3(),
              MapUtils.getString(metricResult, field, ""), "");
          break;
        case "downstreamBytes":
          value = getPrintSize(MapUtils.getString(metricResult, field, ""));
          break;
        case "upstreamBytes":
          value = getPrintSize(MapUtils.getString(metricResult, field, ""));
          break;
        case "totalPayloadBytes":
          value = getPrintSize(MapUtils.getString(metricResult, field, ""));
          break;
        case "downstreamPayloadBytes":
          value = getPrintSize(MapUtils.getString(metricResult, field, ""));
          break;
        case "upstreamPayloadBytes":
          value = getPrintSize(MapUtils.getString(metricResult, field, ""));
          break;
        case "applicationId":
          value = appName;
          break;
        case "categoryId":
          value = appCategoryName;
          break;
        case "subcategoryId":
          value = appSubCategoryName;
          break;
        case "l7ProtocolId":
          value = MapUtils.getString(protocolDict, MapUtils.getString(metricResult, field, ""), "");
          break;
        case "hostgroupId":
          value = MapUtils.getString(hostGroupDict, MapUtils.getString(metricResult, field, ""),
              "");
          break;
        case "enthernetType":
          value = MapUtils.getString(ethernetTypeDict, MapUtils.getString(metricResult, field, ""),
              "");
          break;
        case "ipLocality":
          value = MapUtils.getString(ipLocalityDict, MapUtils.getString(metricResult, field, ""),
              "");
          break;
        default:
          value = MapUtils.getString(metricResult, field, "");
          break;
      }

      return value;
    }).collect(Collectors.toList());

    return values;
  }

  private String getPrintSize(String bytes) {
    long size = Long.parseLong(bytes);
    // 如果字节数少于1000，则直接以B为单位，否则先除于1000，后3位因太少无意义
    if (size < 1000) {
      return String.valueOf(size) + "B";
    }
    // 如果原字节数除于1000之后，少于1000，则可以直接以KB作为单位
    // 因为还没有到达要使用另一个单位的时候
    // 接下去以此类推
    if (size / 1000 < 1000) {
      String KB = null;
      if (size % 1000 > 100) {
        KB = String.valueOf((size / 1000)) + "." + String.valueOf((size % 1000));
      } else if (size % 1000 > 10) {
        KB = String.valueOf((size / 1000)) + ".0" + String.valueOf((size % 1000));
      } else if (size % 1000 > 1) {
        KB = String.valueOf((size / 1000)) + ".00" + String.valueOf((size % 1000));
      } else {
        KB = String.valueOf((size / 1000)) + ".000" + String.valueOf((size % 1000));
      }
      return String.format("%.3f", Double.parseDouble(KB)) + "KB";
    } else {
      size = size / 100;
    }

    if (size / 10000 < 1000) {
      String MB = null;
      if (size % 10000 > 1000) {
        MB = String.valueOf((size / 10000)) + "." + String.valueOf((size % 10000));
      } else if (size % 10000 > 100) {
        MB = String.valueOf((size / 10000)) + ".0" + String.valueOf((size % 10000));
      } else if (size % 10000 > 10) {
        MB = String.valueOf((size / 10000)) + ".00" + String.valueOf((size % 10000));
      } else if (size % 10000 > 1) {
        MB = String.valueOf((size / 10000)) + ".000" + String.valueOf((size % 10000));
      } else {
        MB = String.valueOf(size / 10000);
      }
      return String.format("%.3f", Double.parseDouble(MB)) + "MB";
    } else {
      size = size / 1000;
      String GB = null;
      if (size % 10000 > 1000) {
        GB = String.valueOf((size / 10000)) + "." + String.valueOf((size % 10000));
      } else if (size % 10000 > 100) {
        GB = String.valueOf((size / 10000)) + ".0" + String.valueOf((size % 10000));
      } else if (size % 10000 > 10) {
        GB = String.valueOf((size / 10000)) + ".00" + String.valueOf((size % 10000));
      } else if (size % 10000 > 1) {
        GB = String.valueOf((size / 10000)) + ".000" + String.valueOf((size % 10000));
      } else {
        GB = String.valueOf(size / 10000);
      }
      return String.format("%.3f", Double.parseDouble(GB)) + "GB";
    }
  }

  private String getDelay(String delay) {
    double doubleDelay = Double.parseDouble(delay);
    return String.format("%.0f", doubleDelay) + "ms";
  }

  private void exportAsExcel(List<Map<String, Object>> result, MetricQueryVO queryVO,
      ServletOutputStream outputStream) throws IOException {
    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID()).toFile();
    FileUtils.touch(tempFile);

    List<List<String>> lines = Lists.newArrayListWithCapacity(result.size() + 1);
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    titles.addAll(graphFields.values());
    List<List<String>> heads = titles.stream().map(item -> Lists.newArrayList(item))
        .collect(Collectors.toList());

    // 如果导出模式为excel时，初始化写对象
    ExcelWriter excelWriter = null;
    WriteSheet writeSheet = null;
    excelWriter = EasyExcel.write(tempFile).head(heads).build();
    writeSheet = EasyExcel.writerSheet("graphIpconversation").build();

    Map<String, String> columnNameMap = graphFields.entrySet().stream()
        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));
    for (Map<String, Object> temp : result) {
      List<String> values = titles.stream().map(title -> {

        String value = "";
        String field = columnNameMap.get(title);

        switch (field) {
          case "ipAAddress":
            value = MapUtils.getString(temp, field);
            break;
          case "ipBAddress":
            value = MapUtils.getString(temp, field);
            break;
          case "totalBytes":
            value = MapUtils.getString(temp, field);
            break;
          case "establishedSessions":
            value = MapUtils.getString(temp, field);
            break;
          default:
            value = "";
            break;
        }

        return value;
      }).collect(Collectors.toList());

      lines.add(values);
    }

    excelWriter.write(lines, writeSheet);

    if (excelWriter != null) {
      excelWriter.finish();
    }

    // 输出文件
    FileUtils.copyFile(tempFile, outputStream);
    FileUtils.deleteQuietly(tempFile);
  }

  private void exportAsCsv(List<Map<String, Object>> result, MetricQueryVO queryVO,
      ServletOutputStream outputStream) {
    List<String> resultValue = Lists.newArrayListWithCapacity(result.size() + 1);
    resultValue.add(CSV_TITLE);
    result.forEach(item -> {
      String outItem = CsvUtils.spliceRowData(MapUtils.getString(item, "ipAAddress"),
          MapUtils.getString(item, "ipBAddress"), MapUtils.getString(item, "totalBytes"),
          MapUtils.getString(item, "establishedSessions"));
      resultValue.add(outItem);
    });

    try {
      outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      for (String line : resultValue) {
        outputStream.write(line.getBytes(StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      LOGGER.warn("export graphIpconversation error", e);
    }
  }
}
