package com.machloop.fpc.cms.center.metadata.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.ExportUtils;
import com.machloop.alpha.common.util.ExportUtils.FetchData;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.cms.center.global.dao.CounterDao;
import com.machloop.fpc.cms.center.global.data.CounterQuery;
import com.machloop.fpc.cms.center.metadata.dao.LogRecordDao;
import com.machloop.fpc.cms.center.metadata.data.AbstractLogRecordDO;
import com.machloop.fpc.cms.center.metadata.vo.AbstractLogRecordVO;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author liyongjun
 *
 * create at 2019年9月24日, fpc-manager
 */
public abstract class AbstractLogRecordServiceImpl<VO extends AbstractLogRecordVO, DO extends AbstractLogRecordDO> {

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
  private SensorNetworkGroupService sensorNetworkGroupService;

  @Autowired
  private ServletContext servletContext;

  /**
   * @param queryVO
   * @param page
   * @return
   */
  public Page<VO> queryLogRecords(LogRecordQueryVO queryVO, Pageable page) {
    List<String> flowIds = StringUtils.isNotBlank(queryVO.getId())
        ? CsvUtils.convertCSVToList(queryVO.getId())
        : Lists.newArrayListWithCapacity(0);

    fillAdditionalCondition(queryVO);

    // id查询条件不为空的话，使用startTime_flowId查询
    queryVO.setColumns(columnMapping(queryVO.getColumns()));
    Page<DO> logDOPage = getLogRecordDao().queryLogRecords(queryVO, flowIds, page);
    List<VO> logVOList = Lists.newArrayListWithCapacity(logDOPage.getSize());

    for (DO logDO : logDOPage.getContent()) {
      logVOList.add(convertLogDO2LogVO(logDO));
    }
    long totalElem = logDOPage.getTotalElements();

    return new PageImpl<VO>(logVOList, page, totalElem);
  }

  public List<Map<String, Object>> queryLogRecords(String startTime, String endTime,
      List<String> flowIds) {
    return getLogRecordDao().queryLogRecords(startTime, endTime, flowIds,
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

    fillAdditionalCondition(queryVO);

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

    fillAdditionalCondition(queryVO);

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
   * @param sort
   * @param fileType
   * @param count
   * @param out
   * @throws IOException
   */
  public void exportLogRecords(LogRecordQueryVO queryVO, Sort sort, String fileType, int count,
      OutputStream out) throws IOException {
    List<String> flowIds = StringUtils.isNotBlank(queryVO.getId())
        ? CsvUtils.convertCSVToList(queryVO.getId())
        : Lists.newArrayListWithCapacity(0);

    fillAdditionalCondition(queryVO);

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
    Tuple2<String,
        List<String>> idTuples = getLogRecordDao().queryLogRecord(queryVO, flowIds, sort, count);
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

  private void fillAdditionalCondition(LogRecordQueryVO queryVO) {
    // 查询对象为多个网络，单个或多个网络组时，需要在改方法内解析实际查询的对象(多维检索可 选多个网络和网络组)
    if (StringUtils.isNotBlank(queryVO.getNetworkGroupId())) {
      List<String> list = CsvUtils.convertCSVToList(queryVO.getNetworkGroupId());

      if (StringUtils.isNotBlank(queryVO.getServiceId())) {
        List<Tuple2<String, String>> serviceNetworkIds = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        list.forEach(networkGroupId -> {
          serviceNetworkIds.addAll(CsvUtils
              .convertCSVToList(sensorNetworkGroupService.querySensorNetworkGroup(networkGroupId)
                  .getNetworkInSensorIds())
              .stream().map(networkId -> Tuples.of(queryVO.getServiceId(), networkId))
              .collect(Collectors.toList()));
        });

        queryVO.setServiceNetworkIds(serviceNetworkIds);
      } else {
        List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        list.forEach(networkGroupId -> {
          networkIds.addAll(CsvUtils.convertCSVToList(sensorNetworkGroupService
              .querySensorNetworkGroup(networkGroupId).getNetworkInSensorIds()));
        });

        queryVO.setNetworkIds(networkIds);
      }
    } else {
      List<String> list = CsvUtils.convertCSVToList(queryVO.getNetworkId());
      if (CollectionUtils.isNotEmpty(list)) {
        if (StringUtils.isNotBlank(queryVO.getServiceId())) {
          List<Tuple2<String, String>> serviceNetworkIds = list.stream()
              .map(networkId -> Tuples.of(queryVO.getServiceId(), networkId))
              .collect(Collectors.toList());
          queryVO.setServiceNetworkIds(serviceNetworkIds);
        } else {
          queryVO.setNetworkIds(list);
        }
      }
    }
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
      Map<String, String> networkDict, Map<String, String> serviceDict) {
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
            .map(networkId -> MapUtils.getString(networkDict, networkId, ""))
            .collect(Collectors.toList()), "|");
        break;
      case "serviceId":
        value = StringUtils.join(logDO.getServiceId().stream()
            .map(serviceId -> MapUtils.getString(serviceDict, serviceId, ""))
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
