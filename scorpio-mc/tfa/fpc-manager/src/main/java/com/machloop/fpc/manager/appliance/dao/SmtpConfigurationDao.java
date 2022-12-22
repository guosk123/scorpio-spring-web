package com.machloop.fpc.manager.appliance.dao;

import com.machloop.fpc.manager.appliance.data.SmtpConfigurationDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年11月11日, fpc-manager
 */
public interface SmtpConfigurationDao {
  
  SmtpConfigurationDO querySmtpConfigurations();
  
  int saveOrUpdateSmtpConfiguration(SmtpConfigurationDO smtpConfigurationDO);
}
