package com.machloop.fpc.manager.appliance.service.impl;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.dao.MailAlertDao;
import com.machloop.fpc.manager.appliance.service.MailAlertService;
import com.machloop.fpc.manager.appliance.vo.MailAlertQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年10月31日, fpc-manager
 */
@Service
public class MailAlertServiceImpl implements MailAlertService {

  @Autowired
  private MailAlertDao mailAlertDao;

  /**
   * @see com.machloop.fpc.manager.appliance.service.MailAlertService#queryMailAlerts(com.machloop.fpc.manager.appliance.vo.MailAlertQueryVO, com.machloop.alpha.common.base.page.Pageable)
   */
  @Override
  public Page<Map<String, Object>> queryMailAlerts(MailAlertQueryVO queryVO, Pageable page) {

    Page<Map<String, Object>> mailAlertPage = mailAlertDao.queryMailAlerts(queryVO, page);

    mailAlertPage.forEach(item -> {
      String srcIp = MapUtils.getString(item, "src_ip");
      item.put("srcIp",
          StringUtils.startsWithIgnoreCase(srcIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)
              ? StringUtils.substringAfterLast(srcIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)
              : srcIp);
      item.remove("src_ip");

      String destIp = MapUtils.getString(item, "dest_ip");
      item.put("destIp",
          StringUtils.startsWithIgnoreCase(destIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)
              ? StringUtils.substringAfterLast(destIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)
              : destIp);
      item.remove("dest_ip");

      item.put("cityId", MapUtils.getString(item, "city_id"));
      item.remove("city_id");

      item.put("provinceId", MapUtils.getString(item, "province_id"));
      item.remove("province_id");

      item.put("countryId", MapUtils.getString(item, "country_id"));
      item.remove("country_id");

      item.put("srcPort", MapUtils.getString(item, "src_port"));
      item.remove("src_port");

      item.put("destPort", MapUtils.getString(item, "dest_port"));
      item.remove("dest_port");

      item.put("flowId", MapUtils.getString(item, "flow_id"));
      item.remove("flow_id");

      item.put("loginTimestamp", MapUtils.getString(item, "login_timestamp"));
      item.remove("login_timestamp");

      item.put("mailAddress", MapUtils.getString(item, "mail_address"));
      item.remove("mail_address");

      item.put("networkId", MapUtils.getString(item, "network_id"));
      item.remove("network_id");

      item.put("ruleId", MapUtils.getString(item, "rule_id"));
      item.remove("rule_id");
    });
    return mailAlertPage;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.MailAlertService#queryMailAlertStatistics(com.machloop.fpc.manager.appliance.vo.MailAlertQueryVO)
   */
  @Override
  public Map<String, Object> queryMailAlertStatistics(MailAlertQueryVO queryVO) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("total", mailAlertDao.countMailAlerts(queryVO));

    return result;
  }

}
