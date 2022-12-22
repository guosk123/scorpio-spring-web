package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.appliance.data.ExternalReceiverDO;

/**
 * @author ChenXiao
 * create at 2022/9/22
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

    List<String> queryExternalReceiverIds(boolean onlyLocal);

    ExternalReceiverDO queryExternalReceiverByAssignId(String assignId);

    List<ExternalReceiverDO> queryAssignExternalReceiverIds(Date beforeTime);
}
