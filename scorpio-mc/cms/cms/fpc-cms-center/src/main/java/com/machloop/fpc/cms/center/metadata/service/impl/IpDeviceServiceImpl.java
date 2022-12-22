package com.machloop.fpc.cms.center.metadata.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.cms.center.metadata.dao.IpDeviceDao;
import com.machloop.fpc.cms.center.metadata.service.IpDeviceService;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;

/**
 * @author ChenXiao
 * create at 2022/12/9
 */
@Service
public class IpDeviceServiceImpl implements IpDeviceService {


  @Autowired
  private IpDeviceDao ipDeviceDao;


  @Override
  public Page<Map<String, Object>> queryIpDeviceList(LogRecordQueryVO queryVO, PageRequest page) {

    queryVO.setColumns(columnMapping(queryVO.getColumns()));
    return ipDeviceDao.queryIpDeviceList(queryVO, page);
  }

  protected String columnMapping(String columns) {

    if (StringUtils.equals(columns, "*")) {
      return columns;
    }

    Set<String> columnSets = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    columnSets.add("rtp_total_packets");
    columnSets.add("rtp_loss_packets");

    CsvUtils.convertCSVToList(columns).forEach(item -> {
      columnSets.add(TextUtils.camelToUnderLine(item));
    });

    return CsvUtils.convertCollectionToCSV(columnSets);
  }

  @Override
  public List<Map<String, Object>> queryRtpNetworkSegmentationHistograms(LogRecordQueryVO queryVO) {
    List<Map<String, Object>> list = ipDeviceDao.queryRtpNetworkSegmentationHistograms(queryVO);
    List<Map<String, Object>> res = new ArrayList<>();
    list.forEach(map -> {
      Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      temp.put("reportTime", DateUtils.toStringYYYYMMDDHHMMSS(
          (OffsetDateTime) map.get("temp_report_time"), ZoneId.systemDefault()));
      long rtpLossPackets = MapUtils.getLongValue(map, "rtp_loss_packets");
      long rtpTotalPackets = MapUtils.getLongValue(map, "rtp_total_packets");
      double rtpLossPacketsRate = rtpTotalPackets == 0 ? 0
          : new BigDecimal((double) rtpLossPackets / rtpTotalPackets)
              .setScale(4, RoundingMode.HALF_UP).doubleValue();
      temp.put("rtpLossPacketsRate", rtpLossPacketsRate);
      temp.put("rtpTotalPackets", rtpTotalPackets);
      temp.put("rtpLossPackets", rtpLossPackets);
      temp.put("jitterMax", MapUtils.getLongValue(map, "jitter_max"));
      temp.put("jitterMean", MapUtils.getLongValue(map, "jitter_mean"));
      res.add(temp);
    });
    return res;
  }


}
