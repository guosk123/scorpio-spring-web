package com.machloop.fpc.manager.metadata.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.metadata.dao.ReceiverKafkaDao;
import com.machloop.fpc.manager.metadata.dao.ReceiverSettingDao;
import com.machloop.fpc.manager.metadata.data.ReceiverKafkaDO;
import com.machloop.fpc.manager.metadata.data.ReceiverSettingDO;
import com.machloop.fpc.manager.metadata.service.ReceiverSettingService;
import com.machloop.fpc.manager.metadata.vo.ReceiverSettingVO;

@Service
public class ReceiverSettingServiceImpl implements ReceiverSettingService {

  @Autowired
  private ReceiverSettingDao receiverSettingDao;

  @Autowired
  private ReceiverKafkaDao receiverKafkaDao;

  /**
   * @see com.machloop.fpc.manager.metadata.service.ReceiverSettingService#queryReceiverSetting()
   */
  @Override
  public ReceiverSettingVO queryReceiverSetting() {
    ReceiverSettingVO receiverSettingVO = new ReceiverSettingVO();

    ReceiverSettingDO receiverSettingDO = receiverSettingDao.queryReceiverSetting();
    ReceiverKafkaDO receiverKafkaDO = receiverKafkaDao.queryReceiverKafka();

    if (StringUtils.isNotBlank(receiverSettingDO.getId())
        && StringUtils.isNotBlank(receiverKafkaDO.getId())) {
      BeanUtils.copyProperties(receiverKafkaDO, receiverSettingVO);
      BeanUtils.copyProperties(receiverSettingDO, receiverSettingVO);
    }

    return receiverSettingVO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.ReceiverSettingService#saveOrUpdateReceiverSetting(com.machloop.fpc.manager.metadata.vo.ReceiverSettingVO)
   */
  @Override
  @Transactional
  public ReceiverSettingVO saveOrUpdateReceiverSetting(ReceiverSettingVO receiverSettingVO) {

    // 封装kafka配置
    ReceiverKafkaDO receiverKafkaDO = new ReceiverKafkaDO();
    BeanUtils.copyProperties(receiverSettingVO, receiverKafkaDO);
    ReceiverKafkaDO result = receiverKafkaDao.saveOrUpdateReceiverKafka(receiverKafkaDO);

    // 封装发送配置
    ReceiverSettingDO receiverSettingDO = new ReceiverSettingDO();
    BeanUtils.copyProperties(receiverSettingVO, receiverSettingDO);
    receiverSettingDO.setReceiverId(result.getId());
    receiverSettingDO.setReceiverType(ManagerConstants.RECEIVER_TYPE_KAFKA);
    receiverSettingDao.saveOrUpdateReceiverSetting(receiverSettingDO);

    return receiverSettingVO;
  }

}
