package com.machloop.fpc.manager.metadata.service.impl;

import java.util.List;

import com.google.common.collect.Range;
import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6AddressRange;
import com.googlecode.ipv6.IPv6Network;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.fpc.manager.metadata.dao.CollectPolicyDao;
import com.machloop.fpc.manager.metadata.data.CollectPolicyDO;
import com.machloop.fpc.manager.metadata.service.CollectPolicyService;
import com.machloop.fpc.manager.metadata.vo.CollectPolicyVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
public class CollectPolicyServiceImpl implements CollectPolicyService {

  @Autowired
  private CollectPolicyDao collectPolicyDao;

  /**
   * @see com.machloop.fpc.manager.metadata.service.CollectPolicyService#queryCollectPolicys()
   */
  @Override
  public List<CollectPolicyVO> queryCollectPolicys() {
    List<CollectPolicyDO> collectPolicyDOList = collectPolicyDao.queryCollectPolicys();

    List<CollectPolicyVO> collectPolicyVOList = Lists
        .newArrayListWithCapacity(collectPolicyDOList.size());
    for (CollectPolicyDO collectPolicyDO : collectPolicyDOList) {
      CollectPolicyVO collectPolicyVO = new CollectPolicyVO();
      BeanUtils.copyProperties(collectPolicyDO, collectPolicyVO);
      collectPolicyVOList.add(collectPolicyVO);
    }

    return collectPolicyVOList;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.CollectPolicyService#queryCollectPolicy(java.lang.String)
   */
  @Override
  public CollectPolicyVO queryCollectPolicy(String id) {
    CollectPolicyDO collectPolicyDO = collectPolicyDao.queryCollectPolicy(id);
    CollectPolicyVO collectPolicyVO = new CollectPolicyVO();
    BeanUtils.copyProperties(collectPolicyDO, collectPolicyVO);
    return collectPolicyVO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.CollectPolicyService#saveCollectPolicy(com.machloop.fpc.manager.metadata.vo.CollectPolicyVO)
   */
  @Override
  @Transactional
  public CollectPolicyVO saveCollectPolicy(CollectPolicyVO collectPolicyVO) {
    CollectPolicyDO existName = collectPolicyDao
        .queryCollectPolicyByName(collectPolicyVO.getName());
    if (StringUtils.isNotBlank(existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "名称已经存在");
    }

    CollectPolicyDO collectPolicyDO = new CollectPolicyDO();
    BeanUtils.copyProperties(collectPolicyVO, collectPolicyDO);
    // 提取起始IP地址和结束IP地址
    String ipAddress = collectPolicyVO.getIpAddress();
    List<CollectPolicyDO> collectPolicyDOS = collectPolicyDao.queryCollectAllPolicy();
    checkIpAddresssDuplicate(collectPolicyVO.getIpAddress(), collectPolicyDOS);
    if (StringUtils.isNotBlank(ipAddress)) {
      if (NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ipAddress, "/"),
          NetworkUtils.IpVersion.V4)) {
        Tuple2<Long, Long> ipRange = NetworkUtils.ip2Range(ipAddress);
        long ipStart = ipRange.getT1();
        if (ipStart <= 0) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "IP/IP段录入错误，请检查后重新录入。");
        }

        // 判断IP地址（段）是否与数据库中已配置的IP地址（段）冲突
        long ipEnd = ipRange.getT2();
        CollectPolicyDO conflictDO = collectPolicyDao.queryCollectPolicyWithIpBetween(ipStart,
            ipEnd);
        if (StringUtils.isNotBlank(conflictDO.getId())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE,
              "IP地址段与已配置地址段冲突，请检查后重新录入。");
        }
        collectPolicyDO.setIpStart(ipStart);
        collectPolicyDO.setIpEnd(ipEnd);
      } else if (NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ipAddress, "/"),
          NetworkUtils.IpVersion.V6)) {
        CollectPolicyDO collectPolicyDOByIpv6 = collectPolicyDao
            .queryCollectPolicyWithIpv6(ipAddress);
        if (StringUtils.isNotBlank(collectPolicyDOByIpv6.getId())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "IPV6地址已经存在，请重新录入");
        }
      } else {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "IP地址不正确，清检查后重新输入");
      }
    }

    CollectPolicyDO result = collectPolicyDao.saveCollectPolicy(collectPolicyDO);

    return queryCollectPolicy(result.getId());
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.CollectPolicyService#updateCollectPolicy(com.machloop.fpc.manager.metadata.vo.CollectPolicyVO)
   */
  @Override
  @Transactional
  public CollectPolicyVO updateCollectPolicy(CollectPolicyVO collectPolicyVO) {
    CollectPolicyDO exist = collectPolicyDao.queryCollectPolicy(collectPolicyVO.getId());
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "对象不存在");
    }

    CollectPolicyDO existName = collectPolicyDao
        .queryCollectPolicyByName(collectPolicyVO.getName());
    if (StringUtils.isNotBlank(existName.getId())
        && !StringUtils.equals(existName.getId(), collectPolicyVO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "名称已经存在");
    }

    CollectPolicyDO collectPolicyDO = new CollectPolicyDO();
    BeanUtils.copyProperties(collectPolicyVO, collectPolicyDO);
    // 提取起始IP地址和结束IP地址
    long ipStart = 0;
    long ipEnd = 0;
    String ipAddress = collectPolicyVO.getIpAddress();
    if (StringUtils.isNotBlank(ipAddress)) {
      Tuple2<Long, Long> ipRange = NetworkUtils.ip2Range(collectPolicyVO.getIpAddress());
      ipStart = ipRange.getT1();
      if (ipStart <= 0) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "IP/IP段录入错误，请检查后重新录入。");
      }

      // 判断IP地址（段）是否与数据库中已配置的IP地址（段）冲突
      ipEnd = ipRange.getT2();
      CollectPolicyDO conflictDO = collectPolicyDao.queryCollectPolicyWithIpBetween(ipStart, ipEnd);
      if (StringUtils.isNotBlank(conflictDO.getId())
          && !StringUtils.equalsIgnoreCase(collectPolicyVO.getId(), conflictDO.getId())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE,
            "IP地址段与已配置地址段冲突，请检查后重新录入。");
      }
    }
    collectPolicyDO.setIpStart(ipStart);
    collectPolicyDO.setIpEnd(ipEnd);

    collectPolicyDao.updateCollectPolicy(collectPolicyDO);

    return queryCollectPolicy(collectPolicyVO.getId());
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.CollectPolicyService#changeCollectPolicyState(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @Transactional
  public CollectPolicyVO changeCollectPolicyState(String id, String state, String operatorId) {
    CollectPolicyDO exist = collectPolicyDao.queryCollectPolicy(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "对象不存在");
    }

    collectPolicyDao.changeCollectPolicyState(id, state, operatorId);
    return queryCollectPolicy(id);
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.CollectPolicyService#deleteCollectPolicy(java.lang.String, java.lang.String)
   */
  @Override
  @Transactional
  public CollectPolicyVO deleteCollectPolicy(String id, String operatorId) {
    CollectPolicyDO exist = collectPolicyDao.queryCollectPolicy(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "对象不存在");
    }

    CollectPolicyVO collectPolicyVO = queryCollectPolicy(id);
    if (StringUtils.isNotBlank(collectPolicyVO.getId())) {
      collectPolicyDao.deleteCollectPolicy(id, operatorId);
    }

    return collectPolicyVO;
  }

  private void checkIpAddresssDuplicate(String ipAddress, List<CollectPolicyDO> collectPolicyDOS) {
    if (collectPolicyDOS.isEmpty()) {
      return;
    }

    List<Tuple2<String, Range<Long>>> existIpv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, IPv6AddressRange>> existIpv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    collectPolicyDOS.forEach(policy -> {
      CsvUtils.convertCSVToList(policy.getIpAddress()).forEach(ip -> {
        if (NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "/"),
            NetworkUtils.IpVersion.V4)) {
          Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ip);
          existIpv4RangeList
              .add(Tuples.of(policy.getName(), Range.closed(ip2Range.getT1(), ip2Range.getT2())));
        } else {
          existIpv6RangeList.add(Tuples.of(policy.getName(), ipv6ToRange(ip)));
        }
      });
    });

    List<Tuple2<String, Range<Long>>> pendingIpv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, IPv6AddressRange>> pendingIpv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    CsvUtils.convertCSVToList(ipAddress).forEach(ip -> {
      if (NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "/"),
          NetworkUtils.IpVersion.V4)) {
        Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ip);
        pendingIpv4RangeList.add(Tuples.of(ip, Range.closed(ip2Range.getT1(), ip2Range.getT2())));
      } else {
        pendingIpv6RangeList.add(Tuples.of(ip, ipv6ToRange(ip)));
      }
    });

    // 校验IPV4是否重复
    for (Tuple2<String, Range<Long>> pendingIpRange : pendingIpv4RangeList) {
      for (Tuple2<String, Range<Long>> existIpRange : existIpv4RangeList) {
        if (pendingIpRange.getT2().isConnected(existIpRange.getT2())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, String.format(
              "IP地址/段 [%s] 与已有的应用层采集协议[%s]重复", pendingIpRange.getT1(), existIpRange.getT1()));
        }
      }
    }

    // 校验IPV6是否重复
    for (Tuple2<String, IPv6AddressRange> pendingIpRange : pendingIpv6RangeList) {
      for (Tuple2<String, IPv6AddressRange> existIpRange : existIpv6RangeList) {
        if (pendingIpRange.getT2().contains(existIpRange.getT2())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, String.format(
              "IP地址/段 [%s] 与已有的应用层采集协议[%s]重复", pendingIpRange.getT1(), existIpRange.getT1()));
        }
      }
    }
  }

  private static IPv6AddressRange ipv6ToRange(String ipv6) {
    if (StringUtils.contains(ipv6, "/")) {
      return IPv6Network.fromString(ipv6);
    } else {
      return IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString(ipv6),
          IPv6Address.fromString(ipv6));
    }
  }
}
