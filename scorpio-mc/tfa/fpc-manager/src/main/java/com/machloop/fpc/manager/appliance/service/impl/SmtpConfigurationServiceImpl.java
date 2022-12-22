package com.machloop.fpc.manager.appliance.service.impl;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.fpc.manager.appliance.bo.SmtpConfigurationBO;
import com.machloop.fpc.manager.appliance.dao.SmtpConfigurationDao;
import com.machloop.fpc.manager.appliance.data.SmtpConfigurationDO;
import com.machloop.fpc.manager.appliance.service.SmtpConfigurationService;
import com.sun.mail.util.MailConnectException;

/**
 * @author "Minjiajun"
 *
 * create at 2021年11月11日, fpc-manager
 */
@Service
public class SmtpConfigurationServiceImpl implements SmtpConfigurationService {

  @Autowired
  SmtpConfigurationDao smtpConfigurationDao;

  /**
   * @see com.machloop.fpc.manager.appliance.service.SmtpConfigurationService#querySmtpConfigurations()
   */
  @Override
  public SmtpConfigurationBO querySmtpConfigurations() {

    SmtpConfigurationBO smtpConfigurationBO = new SmtpConfigurationBO();
    SmtpConfigurationDO smtpConfigurationDO = smtpConfigurationDao.querySmtpConfigurations();
    if (StringUtils.isBlank(smtpConfigurationDO.getId())) {
      return new SmtpConfigurationBO();
    }
    BeanUtils.copyProperties(smtpConfigurationDO, smtpConfigurationBO);

    return smtpConfigurationBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.SmtpConfigurationService#updateSmtpConfiguration(com.machloop.fpc.manager.appliance.bo.SmtpConfigurationBO, java.lang.String)
   */
  @Override
  public SmtpConfigurationBO updateSmtpConfiguration(SmtpConfigurationBO smtpConfigurationBO,
      String operatorId) {

    SmtpConfigurationDO smtpConfigurationDO = new SmtpConfigurationDO();
    BeanUtils.copyProperties(smtpConfigurationBO, smtpConfigurationDO);
    smtpConfigurationDO.setOperatorId(operatorId);
    smtpConfigurationDao.saveOrUpdateSmtpConfiguration(smtpConfigurationDO);

    return querySmtpConfigurations();
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.SmtpConfigurationService#smtpTestConnection(com.machloop.fpc.manager.appliance.bo.SmtpConfigurationBO)
   */
  @Override
  public void smtpTestConnection(SmtpConfigurationBO smtpConfigurationBO) {

    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
    javaMailSender.setUsername(smtpConfigurationBO.getLoginUser());
    javaMailSender.setPassword(smtpConfigurationBO.getLoginPassword());
    javaMailSender.setHost(smtpConfigurationBO.getSmtpServer());
    try {
      javaMailSender.testConnection();
    } catch (MailConnectException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "无法连接到邮箱服务器，请检查邮箱服务器地址是否正确");
    } catch (AuthenticationFailedException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "认证失败，请检查用户名或密码是否正确");
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }

}
