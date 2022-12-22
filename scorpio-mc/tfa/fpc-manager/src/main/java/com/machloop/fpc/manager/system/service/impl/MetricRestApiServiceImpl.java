package com.machloop.fpc.manager.system.service.impl;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.system.service.MetricRestApiService;
import com.machloop.fpc.manager.system.dao.MetricRestApiDao;
import com.machloop.fpc.manager.system.data.MetricRestApiRecordDO;
import com.machloop.fpc.manager.system.service.MetricRestApiRecordService;
import com.machloop.fpc.manager.system.vo.MetricRestApiQueryVO;

/**
 * @author guosk
 *
 * create at 2022年5月31日, fpc-manager
 */
@Primary
@Service
public class MetricRestApiServiceImpl implements MetricRestApiService, MetricRestApiRecordService {


  @Autowired
  private DictManager dictManager;


  @Autowired
  private MetricRestApiDao metricRestApiDao;

  private static final List<
      Map<String, Object>> batchList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);


  @Override
  public void auditOperate(String servletPath, String method, String ip, int status, String userId,
      int contentSize) {

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("method", method);
    params.put("userIp", ip);
    params.put("userId", userId);
    params.put("status", status == 200 ? 1 : 0);
    params.put("response", contentSize == 0 ? (status + "") : ("返回的字节数为" + contentSize));
    Map<String, String> restapiDict = dictManager.getBaseDict().getItemMap("system_restapi");
    String apiName = restapiDict.get(servletPath);
    if (servletPath.endsWith("/status")) {
      apiName = "启用/禁用" + apiName;
    } else if (!StringUtils.equalsAny(servletPath, "/restapi/fpc-v1/appliance/sa/knowledges",
        "/restapi/fpc-v1/appliance/geolocation/knowledges",
        "/restapi/fpc-v1/appliance/packet-analysis-tasks/upload-urls",
        "/restapi/fpc-v1/appliance/packets/file-urls")) {
      switch (method) {
        case "GET":
          if (servletPath.endsWith("/xxx")) {
            apiName = "查询" + apiName + "详情";
          } else {
            apiName = "查询" + apiName;
          }
          break;
        case "PUT":
          apiName = "编辑" + apiName;
          break;
        case "POST":
          apiName = "新建" + apiName;
          break;
        default:
          apiName = "删除" + apiName;
          break;
      }
    }

    params.put("uri", servletPath);
    params.put("apiName", apiName);
    params.put("timestamp",
        DateUtils.toStringFormat(DateUtils.now(), "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    batchList.add(params);
  }

  @Transactional
  @Scheduled(cron = "${task.system.restapi.schedule.cron}")
  void saveMetricDataRecord() {
    if (!batchList.isEmpty()) {
      metricRestApiDao.saveMetricRestApiRecord(batchList);
      batchList.clear();
    }
  }


  @Override
  public Page<Map<String, Object>> queryMetricRestApiRecords(MetricRestApiQueryVO queryVO,
      PageRequest page, String sortProperty, String sortDirection) {
    Page<MetricRestApiRecordDO> metricRestApiRecordDOS = metricRestApiDao
        .queryMetricRestApiRecords(queryVO, page, sortProperty, sortDirection);
    List<MetricRestApiRecordDO> contents = Lists.newArrayList(metricRestApiRecordDOS.getContent());
    int totalSize = (int) metricRestApiRecordDOS.getTotalElements();
    return new PageImpl<>(
        contents.stream().map(temp -> metricRestApiRecordDO2Map(temp)).collect(Collectors.toList()),
        page, totalSize);
  }

  private Map<String, Object> metricRestApiRecordDO2Map(
      MetricRestApiRecordDO metricRestApiRecordDO) {
    Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    item.put("timestamp", metricRestApiRecordDO.getTimestamp());
    item.put("apiName", metricRestApiRecordDO.getApiName());
    item.put("uri", metricRestApiRecordDO.getUri());
    item.put("method", metricRestApiRecordDO.getMethod());
    item.put("userIp", metricRestApiRecordDO.getUserIp());
    item.put("userId", metricRestApiRecordDO.getUserId());
    item.put("status", metricRestApiRecordDO.getStatus());
    item.put("response", metricRestApiRecordDO.getResponse());
    return item;
  }

  @Override
  public List<Map<String, Object>> queryUserTop(MetricRestApiQueryVO queryVO) {
    return metricRestApiDao.queryUserTop(queryVO);
  }

  @Override
  public List<Map<String, Object>> queryApiTop(MetricRestApiQueryVO queryVO) {
    return metricRestApiDao.queryApiTop(queryVO);
  }

  @Override
  public List<Map<String, Object>> queryUserList(MetricRestApiQueryVO queryVO) {
    return metricRestApiDao.queryUserList(queryVO);
  }

  @Override
  public List<Map<String, Object>> queryApiList(MetricRestApiQueryVO queryVO) {
    return metricRestApiDao.queryApiList(queryVO);
  }


}
