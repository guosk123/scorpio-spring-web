package com.machloop.fpc.manager.appliance.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6AddressRange;
import com.googlecode.ipv6.IPv6Network;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.bo.IpLabelBO;
import com.machloop.fpc.manager.appliance.dao.IpLabelDao;
import com.machloop.fpc.manager.appliance.data.IpLabelDO;
import com.machloop.fpc.manager.appliance.service.IpLabelService;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author chenshimiao
 *
 * create at 2022/9/6 10:45 AM,cms
 * @version 1.0
 */
@Service
public class IpLabelServiceImpl implements IpLabelService {

  @Autowired
  private IpLabelDao ipLabelDao;

  @Override
  public Page<IpLabelBO> queryIpLabels(PageRequest page, String name, String category) {

    Page<IpLabelDO> ipLabelDOPage = ipLabelDao.queryIpLabels(page, name, category);
    long totalElements = ipLabelDOPage.getTotalElements();

    List<IpLabelBO> ipLabelBOList = Lists.newArrayListWithCapacity(ipLabelDOPage.getSize());
    for (IpLabelDO ipLabelDO : ipLabelDOPage) {
      IpLabelBO ipLabelBO = new IpLabelBO();
      BeanUtils.copyProperties(ipLabelDO, ipLabelBO);

      ipLabelBO.setCreateTime(DateUtils.toStringISO8601(ipLabelDO.getCreateTime()));
      ipLabelBO.setUpdateTime(DateUtils.toStringISO8601(ipLabelDO.getUpdateTime()));

      ipLabelBOList.add(ipLabelBO);
    }

    return new PageImpl<>(ipLabelBOList, page, totalElements);
  }

  @Override
  public List<IpLabelBO> queryIpLabels() {

    List<IpLabelDO> ipLabelDOList = ipLabelDao.queryIpLabels();
    List<IpLabelBO> ipLabelBOList = Lists.newArrayListWithCapacity(ipLabelDOList.size());

    for (IpLabelDO ipLabelDO : ipLabelDOList) {
      IpLabelBO ipLabelBO = new IpLabelBO();
      BeanUtils.copyProperties(ipLabelDO, ipLabelBO);
      ipLabelBO.setCreateTime(DateUtils.toStringISO8601(ipLabelDO.getCreateTime()));
      ipLabelBO.setUpdateTime(DateUtils.toStringISO8601(ipLabelDO.getUpdateTime()));

      ipLabelBOList.add(ipLabelBO);
    }

    return ipLabelBOList;
  }

  @Override
  public IpLabelBO queryIpLabel(String id) {
    IpLabelBO ipLabelBO = new IpLabelBO();

    IpLabelDO ipLabelDO = ipLabelDao.queryIpLabel(id);
    BeanUtils.copyProperties(ipLabelDO, ipLabelBO);

    ipLabelBO.setCreateTime(DateUtils.toStringISO8601(ipLabelDO.getCreateTime()));
    ipLabelBO.setUpdateTime(DateUtils.toStringISO8601(ipLabelDO.getUpdateTime()));

    return ipLabelBO;
  }

  @Override
  public IpLabelBO queryIpLabelByIp(String queryIp) {

    // 先查询标签id和标签ip范围
    List<IpLabelDO> ipLabelDOList = ipLabelDao.queryIdAndIp();
    // 判断当前ip在哪个分区内
    List<Tuple2<String, Range<Long>>> existIpv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, IPv6AddressRange>> existIpv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    ipLabelDOList.forEach(ipLabel -> {
      CsvUtils.convertCSVToList(ipLabel.getIpAddress()).forEach(ip -> {
        if (NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "/"),
            NetworkUtils.IpVersion.V4)
            || NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "-"),
                NetworkUtils.IpVersion.V4)) {
          Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ip);
          existIpv4RangeList
              .add(Tuples.of(ipLabel.getName(), Range.closed(ip2Range.getT1(), ip2Range.getT2())));
        } else {
          existIpv6RangeList.add(Tuples.of(ipLabel.getName(), ipv6ToRange(ip)));
        }
      });
    });

    IpLabelBO ipLabelBO = new IpLabelBO();
    // 判断为ipV4 还是 ipV6
    if (NetworkUtils.isInetAddress(queryIp, NetworkUtils.IpVersion.V4)) {
      for (Tuple2<String, Range<Long>> item : existIpv4RangeList) {
        if (item.getT2().contains(NetworkUtils.ip2Long(queryIp))) {
          IpLabelDO ipLabelDO = ipLabelDao.queryIpLabelByName(item.getT1());
          ipLabelBO = new IpLabelBO();
          BeanUtils.copyProperties(ipLabelDO, ipLabelBO);
          return ipLabelBO;
        }
      }
    } else {
      IPv6AddressRange iPv6Addresses = ipv6ToRange(queryIp);
      for (Tuple2<String, IPv6AddressRange> item : existIpv6RangeList) {
        if (item.getT2().contains(iPv6Addresses)) {
          IpLabelDO ipLabelDO = ipLabelDao.queryIpLabelByName(item.getT1());
          ipLabelBO = new IpLabelBO();
          BeanUtils.copyProperties(ipLabelDO, ipLabelBO);
          return ipLabelBO;
        }
      }
    }
    return ipLabelBO;
  }

  @Override
  public Map<String, Object> queryIpLabelCategory() {

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    for (Map<String, Object> m : ipLabelDao.queryIpLabelByCategory()) {
      result.put(m.get("category").toString(), m.get("countCategory"));
    }
    return result;
  }

  @Override
  public IpLabelBO saveIpLabel(IpLabelBO iplabelBO, String operatorId) {
    if (StringUtils.isBlank(iplabelBO.getName())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "标签名称为空。");
    }
    IpLabelDO exist = ipLabelDao.queryIpLabelByName(iplabelBO.getName());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "名称已存在");
    }

    checkIpAddressThrowException(iplabelBO.getIpAddress());

    IpLabelDO ipLabelDO = new IpLabelDO();
    BeanUtils.copyProperties(iplabelBO, ipLabelDO);
    ipLabelDO.setDescription(StringUtils.defaultIfBlank(ipLabelDO.getDescription(), ""));
    ipLabelDO.setOperatorId(operatorId);
    ipLabelDO = ipLabelDao.saveIpLabel(ipLabelDO);

    return queryIpLabel(ipLabelDO.getId());
  }

  @Override
  public IpLabelBO updateIpLabel(String id, IpLabelBO ipLabelBO, String operatorId) {
    IpLabelDO exist = ipLabelDao.queryIpLabelByName(ipLabelBO.getName());
    if (StringUtils.isNotBlank(exist.getId()) && !StringUtils.equals(id, exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "名称已存在");
    }

    IpLabelDO existIpLabel = ipLabelDao.queryIpLabel(id);
    if (StringUtils.isBlank(existIpLabel.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "标签不存在");
    }

    checkIpAddressThrowException(ipLabelBO.getIpAddress());

    IpLabelDO ipLabelDO = new IpLabelDO();
    BeanUtils.copyProperties(existIpLabel, ipLabelDO);
    ipLabelDO.setName(ipLabelBO.getName());
    ipLabelDO.setIpAddress(ipLabelBO.getIpAddress());
    ipLabelDO.setDescription(StringUtils.defaultIfBlank(ipLabelBO.getDescription(), ""));
    ipLabelDO.setOperatorId(operatorId);
    ipLabelDao.updateIpLabel(ipLabelDO);

    return queryIpLabel(id);
  }

  @Override
  public IpLabelBO deleteIpLabel(String id, String operatorId, boolean forceDelete) {
    IpLabelBO ipLabelBO = queryIpLabel(id);

    if (!forceDelete && StringUtils.isBlank(ipLabelBO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "标签不存在");
    }

    ipLabelDao.deleteIpLabel(id, operatorId);

    return ipLabelBO;
  }

  private boolean checkIpAddressThrowException(String ipAddress) {
    if (StringUtils.isBlank(ipAddress)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "IP地址不能为空");
    }

    List<String> ips = CsvUtils.convertCSVToList(ipAddress);
    if (ips.size() > FpcConstants.HOSTGROUP_MAX_IP_COUNT) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "IP地址过多");
    }

    if (Sets.newHashSet(ips).size() < ips.size()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "存在重复的IP地址");
    }

    for (String ip : ips) {
      if (StringUtils.contains(ip, "-")) {
        String[] ipRange = StringUtils.split(ip, "-");
        // 起止都是正确的ip
        if (ipRange.length != 2 || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]))
            || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }
        if (!(NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), NetworkUtils.IpVersion.V4)
            && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]), NetworkUtils.IpVersion.V4)
            || (NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), NetworkUtils.IpVersion.V6)
                && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]),
                    NetworkUtils.IpVersion.V6)))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }
      } else if (!NetworkUtils.isInetAddress(ip) && !NetworkUtils.isCidr(ip)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, ip + "格式非法, 请输入正确的IP地址");
      }
    }
    return true;
  }

  private static IPv6AddressRange ipv6ToRange(String ipv6) {
    if (StringUtils.contains(ipv6, "-")) {
      String[] ipRange = StringUtils.split(ipv6, "-");
      return IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString(ipRange[0]),
          IPv6Address.fromString(ipRange[1]));
    } else if (StringUtils.contains(ipv6, "/")) {
      return IPv6Network.fromString(ipv6);
    } else {
      return IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString(ipv6),
          IPv6Address.fromString(ipv6));
    }
  }

}
