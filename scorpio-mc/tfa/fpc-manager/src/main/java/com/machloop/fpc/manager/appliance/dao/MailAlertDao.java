package com.machloop.fpc.manager.appliance.dao;

import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.vo.MailAlertQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年10月31日, fpc-manager
 */
public interface MailAlertDao {

  Page<Map<String, Object>> queryMailAlerts(MailAlertQueryVO queryVO, Pageable page);
  
  long countMailAlerts(MailAlertQueryVO queryVO);
}
