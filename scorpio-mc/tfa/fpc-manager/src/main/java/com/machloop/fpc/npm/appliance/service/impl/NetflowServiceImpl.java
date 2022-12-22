package com.machloop.fpc.npm.appliance.service.impl;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort.Direction;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.npm.appliance.bo.NetflowConfigBO;
import com.machloop.fpc.npm.appliance.bo.NetflowNetifBO;
import com.machloop.fpc.npm.appliance.bo.NetflowSourceBO;
import com.machloop.fpc.npm.appliance.dao.NetflowConfigDao;
import com.machloop.fpc.npm.appliance.dao.NetflowStatisticDao;
import com.machloop.fpc.npm.appliance.data.NetflowConfigDO;
import com.machloop.fpc.npm.appliance.data.NetflowStatisticDO;
import com.machloop.fpc.npm.appliance.service.NetflowService;
import com.machloop.fpc.npm.appliance.vo.NetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年8月12日, fpc-manager
 */
@Transactional
@Service
public class NetflowServiceImpl implements NetflowService {

  private static final ImmutableMap<String,
      Comparator<NetflowSourceBO>> fieldComparators = ImmutableMap
          .<String, Comparator<NetflowSourceBO>>builder()
          .put("device_name", Comparator.comparing(NetflowSourceBO::getDeviceName))
          .put("total_bytes", Comparator.comparing(NetflowSourceBO::getTotalBandwidth)).build();

  @Autowired
  private NetflowConfigDao netflowConfigDao;

  @Autowired
  private NetflowStatisticDao netflowStatisticDao;

  @Override
  public List<NetflowConfigBO> queryNetflowConfigs(String keywords) {
    List<NetflowConfigBO> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<NetflowConfigDO> fuzzyNetflows = netflowConfigDao.queryNetflowConfigs(keywords);
    if (fuzzyNetflows.isEmpty()) {
      return result;
    }
    List<String> netflowNameList = fuzzyNetflows.stream().map(e -> e.getDeviceName())
        .collect(Collectors.toList());
    List<NetflowConfigDO> netflowList = netflowConfigDao.queryNetflowConfigsByName(netflowNameList);

    for (NetflowConfigDO netflowDO : netflowList) {
      NetflowConfigBO netflowConfigBO = new NetflowConfigBO();
      BeanUtils.copyProperties(netflowDO, netflowConfigBO);
      result.add(netflowConfigBO);
    }
    return result;
  }

  @Override
  public Page<NetflowSourceBO> queryNetflowSources(NetflowQueryVO queryVO, Pageable page) {

    if (queryVO.getStartTimeDate() == null || queryVO.getEndTimeDate() == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }
    long timeInterval = (queryVO.getEndTimeDate().getTime() - queryVO.getStartTimeDate().getTime())
        / Constants.NUM_1000;
    if (timeInterval < Constants.FIVE_MINUTE_SECONDS) {
      timeInterval = Constants.FIVE_MINUTE_SECONDS;
    }

    List<NetflowStatisticDO> netflowStatList = netflowStatisticDao
        .queryNetflowStatsGroupByDevAndNif(queryVO);

    List<NetflowConfigDO> netflowConfigList = netflowConfigDao
        .queryNetflowConfigsGroupByDevAndNif(queryVO);

    Map<String, NetflowStatisticDO> netflowStatSource = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    for (NetflowStatisticDO netflowStatData : netflowStatList) {
      netflowStatSource.put(netflowStatData.getDeviceName() + "_" + netflowStatData.getNetifNo(),
          netflowStatData);
    }

    Map<String,
        NetflowSourceBO> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    for (NetflowConfigDO netflowConfigData : netflowConfigList) {
      NetflowNetifBO netflowNetifBO = new NetflowNetifBO();
      String key = netflowConfigData.getDeviceName() + "_" + netflowConfigData.getNetifNo();
      NetflowSourceBO netflowSourceBO = resultMap.get(netflowConfigData.getDeviceName());
      if (netflowSourceBO == null) {
        netflowSourceBO = new NetflowSourceBO();
        netflowSourceBO.setNetif(Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
        resultMap.put(netflowConfigData.getDeviceName(), netflowSourceBO);
      }
      NetflowStatisticDO stat = netflowStatSource.get(key);
      if (StringUtils.isNotBlank(netflowConfigData.getNetifNo())) {
        BeanUtils.copyProperties(netflowConfigData, netflowNetifBO);
        netflowNetifBO.setIngestBandwidth(
            stat == null ? 0 : calcuBandwidth(stat.getIngestBytes(), timeInterval));
        netflowNetifBO.setTransmitBandwidth(
            stat == null ? 0 : calcuBandwidth(stat.getTransmitBytes(), timeInterval));
        netflowNetifBO.setTotalBandwidth(
            stat == null ? 0 : calcuBandwidth(stat.getTotalBytes(), timeInterval));
        netflowSourceBO.getNetif().add(netflowNetifBO);
      } else {
        BeanUtils.copyProperties(netflowConfigData, netflowSourceBO);
        netflowSourceBO.setTotalBandwidth(
            stat == null ? 0 : calcuBandwidth(stat.getTotalBytes(), timeInterval));
      }
    }

    List<NetflowSourceBO> resultList = new ArrayList<NetflowSourceBO>(resultMap.values());
    int total = resultList.size();
    // 分页
    List<NetflowSourceBO> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    Comparator<NetflowSourceBO> comparator = null;
    Iterator<Order> it = page.getSort().iterator();
    while (it.hasNext()) {
      Order order = it.next();
      Comparator<NetflowSourceBO> tmp = fieldComparators.get(order.getProperty());
      tmp = order.getDirection().equals(Direction.ASC) ? tmp : tmp.reversed();
      comparator = (comparator == null) ? tmp : comparator.thenComparing(tmp);
    }
    // 默认方式
    if (comparator == null) {
      comparator = Comparator.comparing(NetflowSourceBO::getTotalBandwidth);
    }

    result = resultList.stream().sorted(comparator).skip(page.getOffset()).limit(page.getPageSize())
        .collect(Collectors.toList());

    return new PageImpl<>(result, page, total);
  }

  private static double calcuBandwidth(Long currentBytes, Long timeInterval) {
    BigDecimal bg = new BigDecimal(currentBytes * Constants.BYTE_BITS / timeInterval);
    return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
  }

  @Override
  public List<NetflowConfigBO> batchUpdateNetflows(List<NetflowConfigBO> netflowConfigBOList,
      String operatorId) {
    List<NetflowConfigDO> netflowDeviceDOList = Lists
        .newArrayListWithCapacity(netflowConfigBOList.size());
    List<NetflowConfigDO> netflowNetifDOList = Lists
        .newArrayListWithCapacity(netflowConfigBOList.size());

    for (NetflowConfigBO netflowConfigBO : netflowConfigBOList) {

      NetflowConfigDO netflowConfigDO = new NetflowConfigDO();
      BeanUtils.copyProperties(netflowConfigBO, netflowConfigDO);

      netflowConfigDO.setOperatorId(operatorId);
      if (StringUtils.equals(FpcConstants.DEVICE_TYPE_DEVICE, netflowConfigDO.getDeviceType())) {
        netflowNetifDOList.add(netflowConfigDO);
        netflowConfigDao.updateNetflowDevice(netflowNetifDOList);
      } else {
        netflowDeviceDOList.add(netflowConfigDO);
        netflowConfigDao.updateNetflowNetif(netflowDeviceDOList);
      }
    }
    return null;
  }
}
