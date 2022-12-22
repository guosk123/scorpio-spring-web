package com.machloop.fpc.manager.metadata.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.ExportUtils;
import com.machloop.alpha.common.util.ExportUtils.FetchData;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.common.util.TokenUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.global.dao.CounterDao;
import com.machloop.fpc.manager.global.data.CounterQuery;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.AbstractLogRecordDO;
import com.machloop.fpc.manager.metadata.vo.AbstractLogRecordVO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

/**
 * @author liyongjun
 *
 * create at 2019年9月24日, fpc-manager
 */
public abstract class AbstractLogRecordServiceImpl<VO extends AbstractLogRecordVO, DO extends AbstractLogRecordDO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLogRecordServiceImpl.class);

  private Map<String, Tuple4<String, String, Integer, Date>> packetMap = Maps.newConcurrentMap();

  public static final Map<String,
      String> kpiFields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    kpiFields.put("startTime", "采集时间");
    kpiFields.put("endTime", "结束时间");
    kpiFields.put("networkId", "所属网络");
    kpiFields.put("serviceId", "所属业务");
    kpiFields.put("flowId", "会话ID");
    kpiFields.put("srcIp", "源IP");
    kpiFields.put("srcPort", "源端口");
    kpiFields.put("destIp", "目的IP");
    kpiFields.put("destPort", "目的端口");
    kpiFields.put("policyName", "采集策略");
    kpiFields.put("level", "级别");
  }

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private ServletContext servletContext;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  @Autowired
  private ServiceService serviceService;

  /**
   * @param queryVO
   * @param page
   * @return
   */
  public Page<VO> queryLogRecords(LogRecordQueryVO queryVO, Pageable page) {
    List<String> ids = StringUtils.isNotBlank(queryVO.getId())
        ? CsvUtils.convertCSVToList(queryVO.getId())
        : Lists.newArrayListWithCapacity(0);

    // id查询条件不为空的话，使用startTime_flowId查询
    queryVO.setColumns(columnMapping(queryVO.getColumns()));
    Page<DO> logDOPage = getLogRecordDao().queryLogRecords(queryVO, ids, page);

    // 获取网络、业务字典
    Map<String, String> networkDict = networkService.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkBO::getId, NetworkBO::getName));
    networkDict.putAll(logicalSubnetService.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetBO::getId, LogicalSubnetBO::getName)));
    Map<String, String> serviceDict = serviceService.queryServices().stream()
        .collect(Collectors.toMap(ServiceBO::getId, ServiceBO::getName));

    List<VO> logVOList = Lists.newArrayListWithCapacity(logDOPage.getSize());
    for (DO logDO : logDOPage.getContent()) {
      VO logVO = convertLogDO2LogVO(logDO);
      logVO.setNetworkText(logDO.getNetworkId().stream()
          .map(networkId -> MapUtils.getString(networkDict, networkId, ""))
          .collect(Collectors.toList()));
      logVO.setServiceText(logDO.getServiceId().stream()
          .map(serviceId -> MapUtils.getString(serviceDict, serviceId, ""))
          .collect(Collectors.toList()));

      logVOList.add(logVO);
    }
    long totalElem = logDOPage.getTotalElements();

    return new PageImpl<VO>(logVOList, page, totalElem);
  }

  public List<Map<String, Object>> queryLogRecords(LogRecordQueryVO queryVO, List<String> flowIds) {
    return getLogRecordDao().queryLogRecords(queryVO, flowIds,
        Integer.parseInt(HotPropertiesHelper.getProperty("rest.metadata.result.query.max.count")));
  }

  /**
   * @param dsl
   * @return
   */
  public String queryLogRecordsViaDsl(String dsl) {
    return getLogRecordDao().queryLogRecordsViaDsl(dsl);
  }

  /**
   * @param queryVO
   * @param id
   * @return
   */
  public VO queryLogRecord(LogRecordQueryVO queryVO, String id) {
    queryVO.setColumns(columnMapping(queryVO.getColumns()));
    DO logDO = getLogRecordDao().queryLogRecord(queryVO, id);

    return convertLogDO2LogVO(logDO);
  }

  /**
   * @param queryVO
   * @return
   */
  public Map<String, Object> queryLogRecordStatistics(LogRecordQueryVO queryVO) {
    List<String> flowIds = StringUtils.isNotBlank(queryVO.getId())
        ? CsvUtils.convertCSVToList(queryVO.getId())
        : Lists.newArrayListWithCapacity(0);

    long total = 0;
    String protocol = StringUtils.upperCase(StringUtils
        .substringBetween(this.getClass().getSimpleName(), "Protocol", "LogServiceImpl"));
    if (flowIds.isEmpty()
        && getCounterDao().onlyBaseFilter(queryVO.getSourceType(), queryVO.getDsl(), protocol)) {
      CounterQuery counterQuery = new CounterQuery();
      BeanUtils.copyProperties(queryVO, counterQuery);
      total = getCounterDao().countProtocolLogRecord(counterQuery, protocol);
    } else {
      total = getLogRecordDao().countLogRecords(queryVO, flowIds);
    }

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("total", total);
    return result;
  }

  /**
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @return
   */
  public void exportLogRecords(LogRecordQueryVO queryVO, Sort sort, String fileType, int count,
      OutputStream out) throws IOException {
    List<String> analysisResultIds = StringUtils.isNotBlank(queryVO.getId())
        ? CsvUtils.convertCSVToList(queryVO.getId())
        : Lists.newArrayListWithCapacity(0);

    // 前端传递的驼峰字段
    String columns = queryVO.getColumns();

    // 标题
    List<String> titles = convertLogDOList2LineList(Lists.newArrayListWithCapacity(0), columns)
        .get(0);

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID()).toFile();
    FileUtils.touch(tempFile);

    // 获取id集合
    int maxCount = Integer
        .parseInt(HotPropertiesHelper.getProperty("export.protocol.log.max.count"));
    count = (count <= 0 || count > maxCount) ? maxCount : count;
    Tuple2<String, List<String>> idTuples = getLogRecordDao().queryLogRecords(queryVO,
        analysisResultIds, sort, count);
    String tableName = idTuples.getT1();
    List<String> ids = idTuples.getT2();

    // 单次查询数量
    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;
      private String dataColumns = columnMapping(columns);

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {
        // 获取数据
        List<String> tmpIds = ids.stream().skip(offset).limit(batchSize)
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tmpIds)) {
          offset = -1;
          return Lists.newArrayListWithCapacity(0);
        }

        List<DO> tmp = getLogRecordDao().queryLogRecordByIds(tableName, dataColumns, tmpIds, sort);
        List<List<String>> dataset = convertLogDOList2LineList(tmp, columns);
        // 去除标题
        dataset.remove(0);

        // 避免死循环
        if (dataset.size() == 0) {
          offset = -1;
          return Lists.newArrayListWithCapacity(0);
        }

        offset += dataset.size();

        return dataset;
      }

    };

    // 导出数据
    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /**
   * 
   * @param queryId
   * @param queryVO
   * @param sort
   * @return
   */
  public Map<String, Object> fetchLogPacketFileUrls(String queryId, LogRecordQueryVO queryVO,
      Sort sort) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (packetMap.size() >= Constants.BLOCK_DEFAULT_SIZE) {
      packetMap = Maps.newConcurrentMap();
    }

    String id = Base64Utils.encode(queryVO.getFileType() + queryVO.getDsl());

    // 近期相同过滤条件已有数据包文件，可直接下载
    if (packetMap.containsKey(id) && !StringUtils.equals(packetMap.get(id).getT1(), queryId)
        && StringUtils.equals(packetMap.get(id).getT2(), "success") && packetMap.get(id).getT4()
            .after(DateUtils.beforeSecondDate(DateUtils.now(), Constants.ONE_MINUTE_SECONDS))) {
      // 构建下载链接
      result.put("queryId", queryId);
      result.put("status", packetMap.get(id).getT2());
      result.put("progress", 100);
      result.put("truncate", packetMap.get(id).getT3());
      result.put("result", packetDownloadUrl(packetMap.get(id).getT1()));

      return result;
    }

    // 第一次心跳，查询流日志，获取本次下载包含的flowId
    List<Object> flowIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (!packetMap.containsKey(id) || !StringUtils.equals(packetMap.get(id).getT1(), queryId)) {
      List<String> ids = StringUtils.isNotBlank(queryVO.getId())
          ? CsvUtils.convertCSVToList(queryVO.getId())
          : Lists.newArrayListWithCapacity(0);

      String maxSize = HotPropertiesHelper.getProperty("flow.packet.download.max.session");
      int size = StringUtils.isBlank(maxSize) ? ManagerConstants.REST_ENGINE_DOWNLOAD_MAX_SESSIONS
          : Integer.parseInt(maxSize);
      flowIds = getLogRecordDao().queryFlowIds(queryId, queryVO, ids, sort, size);

      if (CollectionUtils.isEmpty(flowIds)) {
        // 未查询到流
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "未查询到数据");
      }
    }

    // 调用数据包生成接口
    String requestUrl = "";
    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = ManagerConstants.REST_ENGINE_MULTIFLOW_PACKET_QUERY;

      // 拼接地址
      StringBuilder url = new StringBuilder();
      url.append(HotPropertiesHelper.getProperty("fpc.engine.rest.server.protocol"));
      url.append("://");
      url.append(HotPropertiesHelper.getProperty("fpc.engine.rest.server.host"));
      url.append(":");
      url.append(HotPropertiesHelper.getProperty("fpc.engine.rest.server.port"));
      url.append(path);
      url.append("?X-Machloop-Date=");
      url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      url.append("&X-Machloop-Credential=");
      url.append(credential);
      url.append("&X-Machloop-Signature=");
      url.append(TokenUtils.makeSignature(token, credential, "POST", date, path));
      requestUrl = url.toString();

      Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      params.put("queryId", queryId);
      params.put("startTime", queryVO.getStartTimeDate().getTime());
      params.put("endTime", queryVO.getEndTimeDate().getTime());
      String networkId = StringUtils.defaultIfBlank(queryVO.getNetworkId(), "");
      if (StringUtils.isAllBlank(queryVO.getNetworkId(), queryVO.getPacketFileId())) {
        networkId = "ALL";
      }
      params.put("networkId", networkId);
      params.put("packetFileId", StringUtils.defaultIfBlank(queryVO.getPacketFileId(), ""));
      params.put("fileType", queryVO.getFileType());
      params.put("flowIds", flowIds);

      LOGGER.info("invoke rest api:{}, params: {}.", requestUrl, JsonHelper.serialize(params));

      HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params);
      ResponseEntity<
          String> resultStr = restTemplate.postForEntity(url.toString(), httpEntity, String.class);

      if (StringUtils.isBlank(resultStr.getBody())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "引擎异常，未获取到数据包文件");
      }

      LOGGER.info("invoke rest api queryId:{}, result:{}", queryId, resultStr.getBody());

      Map<String, Object> response = JsonHelper.deserialize(resultStr.getBody(),
          new TypeReference<Map<String, Object>>() {
          }, false);

      result.put("queryId", queryId);
      String status = "";
      int truncate = 0;
      if (response.containsKey("code")) {
        status = "fail";
        result.put("status", status);
        result.put("progress", 0);
        result.put("result", MapUtils.getString(response, "msg", ""));
      } else {
        status = MapUtils.getString(response, "status");
        result.put("status", status);
        result.put("progress", MapUtils.getIntValue(response, "progress", 0));
        truncate = MapUtils.getIntValue(response, "truncate", 0);
        result.put("truncate", truncate);
        result.put("result",
            StringUtils.equals(MapUtils.getString(response, "status"), "success")
                ? packetDownloadUrl(queryId)
                : "");
      }

      packetMap.put(id, Tuples.of(queryId, status, truncate, DateUtils.now()));
    } catch (Exception e) {
      LOGGER.warn("failed to download flow packets [" + requestUrl + "].", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "获取数据包异常");
    }

    return result;
  }

  private String packetDownloadUrl(String queryId) {
    // 使用UUID作为凭证，并取token进行签名
    String credential = IdGenerator.generateUUID();
    String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
    String date = DateUtils.toStringISO8601(DateUtils.now());
    String path = ManagerConstants.REST_ENGINE_MULTIFLOW_PACKET_DOWNLOAD;

    StringBuilder url = new StringBuilder();
    try {
      url.append(path);
      url.append("?queryId=").append(queryId);
      url.append("&X-Machloop-Date=");
      url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      url.append("&X-Machloop-Credential=");
      url.append(credential);
      url.append("&X-Machloop-Signature=");
      url.append(TokenUtils.makeSignature(token, credential, "GET", date, path));
    } catch (UnsupportedEncodingException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "系统异常，数据包下载异常");
    }

    return url.toString();
  }

  // 注意：重写该方法时，需要添加必查参数：network_id、service_id
  protected String columnMapping(String columns) {
    if (StringUtils.equals(columns, "*")) {
      return columns;
    }

    Set<String> columnSets = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    columnSets.add("network_id");
    columnSets.add("service_id");

    CsvUtils.convertCSVToList(columns).forEach(item -> {
      switch (item) {
        case "srcIp":
          columnSets.add("src_ipv4");
          columnSets.add("src_ipv6");
          break;
        case "destIp":
          columnSets.add("dest_ipv4");
          columnSets.add("dest_ipv6");
          break;
        case "domainAddress":
          columnSets.add("domain_ipv4");
          columnSets.add("domain_ipv6");
          break;
        default:
          columnSets.add(TextUtils.camelToUnderLine(item));
          break;
      }
    });

    return CsvUtils.convertCollectionToCSV(columnSets);
  }

  protected String getFieldValue(DO logDO, String field, Map<String, String> policyLevelDict,
      Map<String, String> networkIdAndName, Map<String, String> serviceIdAndName) {
    String value = "";
    switch (field) {
      case "startTime":
        value = logDO.getStartTime();
        break;
      case "endTime":
        value = logDO.getEndTime();
        break;
      case "srcIp":
        value = logDO.getSrcIp();
        break;
      case "srcPort":
        value = String.valueOf(logDO.getSrcPort());
        break;
      case "destIp":
        value = logDO.getDestIp();
        break;
      case "destPort":
        value = String.valueOf(logDO.getDestPort());
        break;
      case "policyName":
        value = StringUtils.isBlank(logDO.getPolicyName()) ? "默认" : logDO.getPolicyName();
        break;
      case "level":
        value = policyLevelDict.getOrDefault(logDO.getLevel(), "低");
        break;
      case "flowId":
        value = logDO.getFlowId();
        break;
      case "networkId":
        value = StringUtils.join(logDO.getNetworkId().stream()
            .map(networkId -> MapUtils.getString(networkIdAndName, networkId, ""))
            .collect(Collectors.toList()), "|");
        break;
      case "serviceId":
        value = StringUtils.join(logDO.getServiceId().stream()
            .map(serviceId -> MapUtils.getString(serviceIdAndName, serviceId, ""))
            .collect(Collectors.toList()), "|");
        break;
      default:
        value = "";
        break;
    }

    return value;
  }

  protected abstract LogRecordDao<DO> getLogRecordDao();

  protected abstract CounterDao getCounterDao();

  protected abstract VO convertLogDO2LogVO(DO logDO);

  protected abstract List<List<String>> convertLogDOList2LineList(List<DO> logDOList,
      String columns);

}
