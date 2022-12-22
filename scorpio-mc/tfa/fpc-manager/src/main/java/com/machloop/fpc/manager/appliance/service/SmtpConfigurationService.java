package com.machloop.fpc.manager.appliance.service;

import com.machloop.fpc.manager.appliance.bo.SmtpConfigurationBO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年11月11日, fpc-manager
 */
public interface SmtpConfigurationService {

  SmtpConfigurationBO querySmtpConfigurations();

  SmtpConfigurationBO updateSmtpConfiguration(SmtpConfigurationBO smtpConfigurationBO,
      String operatorId);
  
  void smtpTestConnection(SmtpConfigurationBO smtpConfigurationBO);

}
