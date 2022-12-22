package com.machloop.fpc.cms.center.broker.endpoint;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.center.broker.service.local.MQReceiveService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.center.helper.RemoteRocketMQHelper;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author guosk
 *
 * create at 2021年12月8日, fpc-cms-center
 */
@Component
@WebEndpoint(id = "syncConfiguration")
public class SyncConfigurationEndpoint {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncConfigurationEndpoint.class);

  @Autowired
  private MQReceiveService receiveService;

  @Autowired
  private LicenseService licenseService;

  @Autowired
  private RemoteRocketMQHelper rocketMQHelper;

  @WriteOperation
  public String assignConfiguration() {
    Date signatureTime = DateUtils.now();

    // 当前已包含的下发配置签名
    Map<String,
        List<String>> assignConfiguration = receiveService.getAssignConfiguration(signatureTime);

    Map<String, Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    messageBody.put("deviceType", FpcCmsConstants.DEVICE_TYPE_TFA);
    messageBody.put("serialNumber", licenseService.queryDeviceSerialNumber());
    messageBody.put("signatureTime", signatureTime.getTime());
    messageBody.put("signature", JsonHelper.serialize(assignConfiguration));

    Message message = MQMessageHelper.convertToMessage(messageBody,
        FpcCmsConstants.MQ_TOPIC_CMS_SENDUP, FpcCmsConstants.MQ_TAG_SIGNATURE);
    SendResult sendMsg = rocketMQHelper.sendMsg(message);
    LOGGER.info("send signature result: " + sendMsg);

    return "success";
  }

}
