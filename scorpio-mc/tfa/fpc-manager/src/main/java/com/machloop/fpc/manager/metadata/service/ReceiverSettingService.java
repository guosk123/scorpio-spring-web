package com.machloop.fpc.manager.metadata.service;

import com.machloop.fpc.manager.metadata.vo.ReceiverSettingVO;

public interface ReceiverSettingService {

  ReceiverSettingVO queryReceiverSetting();

  ReceiverSettingVO saveOrUpdateReceiverSetting(ReceiverSettingVO receiverSettingVO);

}
