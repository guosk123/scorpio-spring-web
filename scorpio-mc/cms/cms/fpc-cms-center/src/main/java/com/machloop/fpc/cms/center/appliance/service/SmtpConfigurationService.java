package com.machloop.fpc.cms.center.appliance.service;

import com.machloop.fpc.cms.center.appliance.bo.SmtpConfigurationBO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年11月11日, fpc-cms-center
 */
public interface SmtpConfigurationService {

  SmtpConfigurationBO querySmtpConfigurations();

  SmtpConfigurationBO updateSmtpConfiguration(SmtpConfigurationBO smtpConfigurationBO,
      String operatorId);
  
  void smtpTestConnection(SmtpConfigurationBO smtpConfigurationBO);

}
