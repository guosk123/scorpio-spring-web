package com.machloop.fpc.manager.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.manager.appliance.data.ExternalReceiverDO;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
public interface ExternalReceiverDao {
  List<ExternalReceiverDO> queryExternalReceivers();

  ExternalReceiverDO queryExternalReceiver(String id);

  ExternalReceiverDO queryExternalReceiverByName(String name);

  List<ExternalReceiverDO> queryMailExternalReceivers();

  void saveExternalReceiver(ExternalReceiverDO externalReceiverDO);

  void updateExternalReceiver(ExternalReceiverDO externalReceiverDO);

  void deleteExternalReceiver(String id, String operatorId);

  List<ExternalReceiverDO> queryExternalReceiversByType(String receiverType);

  ExternalReceiverDO queryExternalReceiverByExternalReceiverInCmsId(String externalReceiverInCmsId);

  List<String> queryExternalReceiverIds(boolean onlyLocal);

  List<String> queryAssignExternalReceivers(Date beforeTime);
}
