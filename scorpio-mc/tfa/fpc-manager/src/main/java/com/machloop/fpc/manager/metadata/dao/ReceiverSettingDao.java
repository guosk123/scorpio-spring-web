package com.machloop.fpc.manager.metadata.dao;

import com.machloop.fpc.manager.metadata.data.ReceiverSettingDO;

public interface ReceiverSettingDao {

  ReceiverSettingDO queryReceiverSetting();

  int saveOrUpdateReceiverSetting(ReceiverSettingDO receiverSettingDO);

}
