package com.machloop.fpc.manager.appliance.service;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.appliance.bo.ExternalReceiverBO;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
public interface ExternalReceiverService {
  Map<String, List<Map<String, Object>>> queryExternalReceivers();

  Map<String, Object> queryExternalReceiver(String id);

  ExternalReceiverBO saveExternalReceiver(ExternalReceiverBO externalReceiverBO, String operatorId);

  ExternalReceiverBO updateExternalReceiver(ExternalReceiverBO externalReceiverBO, String id,
      String operatorId);

  ExternalReceiverBO deleteExternalReceiver(String id, String operatorId, boolean forceDelete);

  List<Map<String, Object>> queryExternalReceiversByType(String receiverType);
}
