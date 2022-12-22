package com.machloop.fpc.cms.center.appliance.dao;

import com.machloop.fpc.cms.center.appliance.data.SmtpConfigurationDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年11月11日, fpc-cms-center
 */
public interface SmtpConfigurationDao {
  
  SmtpConfigurationDO querySmtpConfigurations();
  
  int saveOrUpdateSmtpConfiguration(SmtpConfigurationDO smtpConfigurationDO);
}
