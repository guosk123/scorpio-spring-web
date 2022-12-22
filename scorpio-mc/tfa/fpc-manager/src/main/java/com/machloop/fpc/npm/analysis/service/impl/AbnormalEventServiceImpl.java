package com.machloop.fpc.npm.analysis.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.npm.analysis.bo.AbnormalEventBO;
import com.machloop.fpc.npm.analysis.dao.AbnormalEventDao;
import com.machloop.fpc.npm.analysis.data.AbnormalEventDO;
import com.machloop.fpc.npm.analysis.service.AbnormalEventService;
import com.machloop.fpc.npm.analysis.vo.AbnormalEventQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月15日, fpc-manager
 */
@Service
public class AbnormalEventServiceImpl implements AbnormalEventService {

  @Autowired
  private AbnormalEventDao abnormalEventDao;

  /**
   * @see com.machloop.fpc.npm.analysis.service.AbnormalEventService#queryAbnormalEvents(com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.npm.analysis.vo.AbnormalEventQueryVO)
   */
  @Override
  public Page<AbnormalEventBO> queryAbnormalEvents(Pageable page, AbnormalEventQueryVO queryVO) {
    Page<AbnormalEventDO> abnormalEvents = abnormalEventDao.queryAbnormalEvents(page, queryVO);

    List<AbnormalEventBO> result = abnormalEvents.getContent().stream().map(abnormalEventDO -> {
      AbnormalEventBO abnormalEventBO = new AbnormalEventBO();
      BeanUtils.copyProperties(abnormalEventDO, abnormalEventBO);
      abnormalEventBO.setSrcIp(
          StringUtils.isNotBlank(abnormalEventDO.getSrcIpv4()) ? abnormalEventDO.getSrcIpv4()
              : abnormalEventDO.getSrcIpv6());
      abnormalEventBO.setDestIp(
          StringUtils.isNotBlank(abnormalEventDO.getDestIpv4()) ? abnormalEventDO.getDestIpv4()
              : abnormalEventDO.getDestIpv6());

      return abnormalEventBO;
    }).collect(Collectors.toList());

    return new PageImpl<>(result, page, abnormalEvents.getTotalElements());
  }

  /**
   * @see com.machloop.fpc.npm.analysis.service.AbnormalEventService#queryAbnormalEvents(com.machloop.fpc.npm.analysis.vo.AbnormalEventQueryVO)
   */
  @Override
  public List<AbnormalEventBO> queryAbnormalEvents(AbnormalEventQueryVO queryVO) {
    List<AbnormalEventDO> abnormalEvents = abnormalEventDao.queryAbnormalEvents(queryVO,
        Integer.parseInt(HotPropertiesHelper.getProperty("rest.metric.result.query.max.count")));

    List<AbnormalEventBO> result = abnormalEvents.stream().map(abnormalEventDO -> {
      AbnormalEventBO abnormalEventBO = new AbnormalEventBO();
      BeanUtils.copyProperties(abnormalEventDO, abnormalEventBO);
      abnormalEventBO.setSrcIp(
          StringUtils.isNotBlank(abnormalEventDO.getSrcIpv4()) ? abnormalEventDO.getSrcIpv4()
              : abnormalEventDO.getSrcIpv6());
      abnormalEventBO.setDestIp(
          StringUtils.isNotBlank(abnormalEventDO.getDestIpv4()) ? abnormalEventDO.getDestIpv4()
              : abnormalEventDO.getDestIpv6());

      return abnormalEventBO;
    }).collect(Collectors.toList());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.analysis.service.AbnormalEventService#countAbnormalEvent(java.util.Date, java.util.Date, java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> countAbnormalEvent(Date startTime, Date endTime,
      String metricType, int count) {
    List<Map<String, Object>> countAbnormalEvent = abnormalEventDao.countAbnormalEvent(startTime,
        endTime, metricType, count);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(countAbnormalEvent.size());
    countAbnormalEvent.forEach(oneItem -> {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      oneItem.entrySet().forEach(entry -> {
        map.put(TextUtils.underLineToCamel(entry.getKey()), entry.getValue());
      });
      result.add(map);
    });

    return result;
  }

}
